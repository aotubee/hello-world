package com.edc.erp.disdeliveryorder.job;


import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.enumeration.warning.WarningBusinessTypeEnum;
import com.edc.erp.common.model.in.fund.FrozenOrderIn;
import com.edc.erp.common.model.in.fund.UnFrozenAndFreezeIn;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.common.service.WarningService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPond;
import com.edc.erp.disdeliveryorder.handle.DisDeliveryOrderSalvageHandle;
import com.edc.erp.disdeliveryorder.model.in.ExecuteDisSalvageDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.out.DisNoSalvageDeliveryOrderOut;
import com.edc.erp.disdeliveryorder.model.out.FirstOrderFreezeDeliveryOrderOut;
import com.edc.erp.disdeliveryorder.model.out.OrdDisSalvageDelivPondDetailOut;
import com.edc.erp.disdeliveryorder.model.out.OrderConfigFreezeDeliveryOrderOut;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondService;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.enumeration.FirstOrderFreezeEnum;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstService;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.handle.DisFreezeHandle;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 配销捞单定时器
 */
@Component
@Slf4j
public class DisSalvageDeliveryOrderScheduler {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TaskLockUtils taskLockUtils;

    @Autowired
    private DisOrderHandle disOrderHandle;

    /**
     * 关闭截单定时器的值
     */
    private final static String CLOSE_CUT_ORDER_KEY = "OPEN";

    @Autowired
    private OrdDisSalvageDelivPondDetailService ordDisSalvageDelivPondDetailService;

    @Autowired
    private OrdDisSalvageDelivPondService ordDisSalvageDelivPondService;

    @Autowired
    private DisDeliveryOrderSalvageHandle disDeliveryOrderSalvageHandle;

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    @Autowired
    private WarningService warningService;

    @Value("${redisMq.erpOrdTopic}")
    private String erpOrdTopic;

    @Value("${warningRedisDB}")
    private Integer warningRedisDB;

    @Autowired
    private FundServer fundServer;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private OrdDisOrderFirstService ordDisOrderFirstService;

    @Autowired
    private DisFreezeHandle disFreezeHandle;

    /**
     * 捞单定时器
     */
    @Scheduled(cron = "0 5/10 * * * ?")
    public void salvageDeliveryOrder() {
        String closeCutOrderSwitch = redisService.get(SystemConstant.DIS_CLOSE_SALVAGE_DELIV_POND_SWITCH_KEY);
        if (CLOSE_CUT_ORDER_KEY.equals(closeCutOrderSwitch)) {
            log.info("配销捞单定时器开关未打开......");
            return;
        }
        if (taskLockUtils.lock(SystemConstant.DIS_CLOSE_SALVAGE_DELIV_POND_SWITCH, 60 * 3 * 1000)) {
            log.info("开始执行配销捞单任务");
            try {
                LocalDateTime nowTime = LocalDateTime.now();
                String beginTime = LocalDate.now() + " 00:00:00";
                String endTime = LocalDate.now() + " 23:59:59";
                ExecuteDisSalvageDeliveryOrderIn executeDisSalvageDeliveryOrderIn = new ExecuteDisSalvageDeliveryOrderIn();
                executeDisSalvageDeliveryOrderIn.setBeginTruncationDateTime(beginTime);
                executeDisSalvageDeliveryOrderIn.setEndTruncationDateTime(endTime);
                executeDisSalvageDeliveryOrderIn.setExecuteTime(nowTime);
                List<String> salvageStatusList = Lists.newArrayList();
                salvageStatusList.add(SalvageDelivPondStatusEnum.WAIT_EXECUTION.getKey());
                salvageStatusList.add(SalvageDelivPondStatusEnum.EXECUTION_ING.getKey());
                executeDisSalvageDeliveryOrderIn.setSalvageStatusList(salvageStatusList);
                List<OrdDisSalvageDelivPond> disSalvageDeliveryPondList = ordDisSalvageDelivPondService.findListForAutoSalvage(executeDisSalvageDeliveryOrderIn);
                if (CollectionUtils.isEmpty(disSalvageDeliveryPondList)) {
                    return;
                }
                extracted(disSalvageDeliveryPondList);
            } catch (Exception e) {
                log.error("执行配销捞单任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIS_CLOSE_SALVAGE_DELIV_POND_SWITCH);
            }
        } else {
            log.info("配销捞单定时器定时器锁还未释放：{}", SystemConstant.DIS_CLOSE_SALVAGE_DELIV_POND_SWITCH);
        }
        log.info("配销捞单任务执行结束");
    }

    private void extracted(List<OrdDisSalvageDelivPond> disSalvageDeliveryPondList) {
        Map<String, List<OrdDisSalvageDelivPond>> map = disSalvageDeliveryPondList.stream().collect(Collectors.groupingBy(OrdDisSalvageDelivPond::getBizOrgCode));
        map.forEach((bizOrgCode, ordDisSalvageDelivPonds) -> {
            ordDisSalvageDelivPonds.forEach(ordDisSalvageDelivPond -> {
                // 根据捞单池id查询捞单池明细
                List<OrdDisSalvageDelivPondDetailOut> ordDisSalvageDelivPondDetailOutList = ordDisSalvageDelivPondDetailService.findListBySalvagePondId(ordDisSalvageDelivPond.getId());
                List<Long> deliveryOrderIdList = ordDisSalvageDelivPondDetailOutList.stream().map(OrdDisSalvageDelivPondDetailOut::getDeliveryOrderId).collect(Collectors.toList());
                // 本次捞单待执行配销单的数量
                int pendingStatusCount = ordDisDeliveryService.countByStatusAndIdList(DeliveryOrderEnum.PENDING.getKey(), deliveryOrderIdList);
                // 本次捞单已经作废的数量
                int invalidStatusCount = ordDisDeliveryService.countByStatusAndIdList(DeliveryOrderEnum.INVALID.getKey(), deliveryOrderIdList);
                // 本捞单记录需要执行的总数量
                int maxCount = ordDisSalvageDelivPondDetailOutList.size() - invalidStatusCount;
                // 已执行占库存总数
                int executedCount = maxCount - pendingStatusCount;
                // 更新占库存的配销单，并记录日志，返回本次定时器执行成功的数量
                int newExecutionTotal = pendingStatusCount == 0 ? pendingStatusCount : disDeliveryOrderSalvageHandle.handleSalvageDelivPondDetailList(bizOrgCode, SystemConstant.SYSTEM_USER, true, ordDisSalvageDelivPondDetailOutList);
                // 等待执行占库存的总数
                int waitExecutionTotal = maxCount - (newExecutionTotal + executedCount);
                OrdDisSalvageDelivPond updateOrdDisSalvageDelivPond = this.initUpdateOrdDisSalvageDelivPond(ordDisSalvageDelivPond,
                        newExecutionTotal + executedCount, waitExecutionTotal);
                ordDisSalvageDelivPondService.updateStatus(updateOrdDisSalvageDelivPond);
            });
        });
    }

    private OrdDisSalvageDelivPond initUpdateOrdDisSalvageDelivPond(OrdDisSalvageDelivPond ordDisSalvageDelivPond, int executionTotal, int waitExecutionTotal) {
        String salvageStatus = 0 == waitExecutionTotal ? SalvageDelivPondStatusEnum.COMPLETED.getKey() : SalvageDelivPondStatusEnum.EXECUTION_ING.getKey();
        OrdDisSalvageDelivPond updateOrdDisSalvageDelivPond = new OrdDisSalvageDelivPond();
        updateOrdDisSalvageDelivPond.setId(ordDisSalvageDelivPond.getId());
        updateOrdDisSalvageDelivPond.setWaitExecutionTotal(waitExecutionTotal);
        updateOrdDisSalvageDelivPond.setExecutionTotal(executionTotal);
        updateOrdDisSalvageDelivPond.setSalvageStatus(salvageStatus);
        updateOrdDisSalvageDelivPond.setUpdater(SystemConstant.SYSTEM_USER);
        updateOrdDisSalvageDelivPond.setUpdateTime(LocalDateTime.now());
        return updateOrdDisSalvageDelivPond;
    }

    @Scheduled(cron = "0 0/30 * * * ?")
    public void warningDisNoAutoSalvageDeliveryOrder() {
        String closeCutOrderSwitch = redisService.get(SystemConstant.DIS_CLOSE_WARNING_NO_AUTO_SALVAGE_SWITCH_KEY);
        if (CLOSE_CUT_ORDER_KEY.equals(closeCutOrderSwitch)) {
            log.info("配销预警未执行捞单定时器开关未打开......");
            return;
        }
        if (taskLockUtils.lock(SystemConstant.DIS_WARNING_NO_AUTO_SALVAGE_SWITCH, 60 * 3 * 1000)) {
            log.info("开始执行配销预警未执行捞单任务");
            try {
                for (String bizOrgCode : SystemConstant.BIZORGCODES) {
                    String beginTime = LocalDate.now() + " 00:00:00";
                    List<OrdDisSalvageDelivPond> salvageList = ordDisSalvageDelivPondService.findNeedRepeatExecuteSalvageList(SalvageDelivPondStatusEnum.COMPLETED.getKey(), beginTime, bizOrgCode);
                    if (CollectionUtils.isEmpty(salvageList)) {
                        continue;
                    }
                    salvageList.forEach(ordDisSalvageDelivPond -> {
                        String warningCountKey = SystemConstant.WARNING_COUNT_KEY + bizOrgCode + SystemConstant.COLON
                                + ordDisSalvageDelivPond.getOrderTypeConfigId() + SystemConstant.WAIT + ordDisSalvageDelivPond.getTruncationDateTime();
                        Integer warningCount = NumberUtil.INTEGER_ONE;
                        if (redisService.hasKey(warningCountKey)) {
                            warningCount = Integer.valueOf(redisService.get(warningCountKey));
                        }
                        if (warningCount.compareTo(NumberUtil.INTEGER_FIVE) != -1) {
                            return;
                        }
                        List<Long> deliveryOrderIdList = ordDisSalvageDelivPondDetailService.findDeliveryOrderIdListBySalvagePondId(ordDisSalvageDelivPond.getId());
                        List<DisNoSalvageDeliveryOrderOut> noSalvageDeliveryOrderList = ordDisDeliveryService.findNoSalvageDeliveryOrderList(deliveryOrderIdList, bizOrgCode);
                        if (CollectionUtils.isEmpty(noSalvageDeliveryOrderList)) {
                            return;
                        }
                        String warningContent = noSalvageDeliveryOrderList.stream().map(DisNoSalvageDeliveryOrderOut::getDeliveryOrderNo).collect(Collectors.joining(SystemConstant.COMMA));
                        // 发预警消息
                        Duration duration = Duration.between(ordDisSalvageDelivPond.getTruncationDateTime(), LocalDateTime.now());
                        long minutes = duration.toMinutes();
                        String timeDifference = duration.toHours() < NumberUtil.INTEGER_ONE ? duration.toMinutes() + "分钟" : minutes / 60 + "小时" + minutes % 60 + "分钟";
                        String msg = MessageFormat.format(SalvageDeliveryOrderWarningTypeEnum.NO_AUTO_SALVAGE.getErrorMessage(),
                                OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode), warningContent, DateUtils.format(ordDisSalvageDelivPond.getTruncationDateTime()),
                                timeDifference, warningCount);
                        warningService.pushWarningMessage(WarningBusinessTypeEnum.NO_AUTO_SALVAGE.getBusinessType(), SalvageDeliveryOrderWarningTypeEnum.NO_AUTO_SALVAGE.getType(),
                                msg, null, null, bizOrgCode);
                        warningCount++;
                        redisService.set(warningCountKey, warningCount, NumberUtil.INTEGER_ONE, TimeUnit.DAYS);
                    });

                }
            } catch (Exception e) {
                log.error("执行配销预警未执行捞单任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIS_WARNING_NO_AUTO_SALVAGE_SWITCH);
            }
        } else {
            log.info("配销预警未执行捞单定时器定时器锁还未释放：{}", SystemConstant.DIS_WARNING_NO_AUTO_SALVAGE_SWITCH);
        }
        log.info("配销预警未执行捞单任务执行结束");
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void unfreezeBusinessOrderAndFreezeDeliveryOrder() {
        String closeCutOrderSwitch = redisService.get(SystemConstant.DIS_CLOSE_UNFREEZE_BUSINESS_ORDER_AND_FREEZE_DELIVERY_SWITCH_KEY);
        if (CLOSE_CUT_ORDER_KEY.equals(closeCutOrderSwitch)) {
            log.info("配销审核后解冻原单与冻结定时器开关未打开......");
            return;
        }
        if (taskLockUtils.lock(SystemConstant.DIS_UNFREEZE_BUSINESS_ORDER_AND_FREEZE_DELIVERY_SWITCH, 20 * 60 * 1000)) {
            log.info("开始执行配销审核后解冻原单与冻结任务");
            try {
                LocalDateTime handleDateTime = LocalDateTime.now().minusDays(10);
                for (String bizOrgCode : SystemConstant.BIZORGCODES) {
                    List<OrdDisDelivery> needFreezeDeliveryOrderList = ordDisDeliveryService.findNeedFreezeDeliveryOrderList(DateUtils.format(handleDateTime), bizOrgCode);
                    if (CollectionUtils.isEmpty(needFreezeDeliveryOrderList)) {
                        continue;
                    }
                    Map<String, List<OrdDisDelivery>> sourceDeliveryOrderListMap = needFreezeDeliveryOrderList.stream()
                            .collect(Collectors.groupingBy(OrdDisDelivery::getSourceCode));
                    sourceDeliveryOrderListMap.entrySet().forEach(entry -> {
                        if (DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(entry.getKey())) {
                            this.handleOrderConfig(entry.getValue(), bizOrgCode, SystemConstant.SYSTEM_USER);
                        }
                        if (DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(entry.getKey())) {
                            this.handleFirstOrder(entry.getValue(), bizOrgCode, SystemConstant.SYSTEM_USER);
                        }
//                        if (DeliveryOrderSourceCodeEnum.MANUAL.getType().equals(entry.getKey())) {
//                            this.handleManualDeliveryOrder(entry.getValue(), bizOrgCode, SystemConstant.SYSTEM_USER);
//                        }
                    });
                }
            } catch (Exception e) {
                log.error("执行配销审核后解冻原单与冻结任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIS_UNFREEZE_BUSINESS_ORDER_AND_FREEZE_DELIVERY_SWITCH);
            }
        } else {
            log.info("配销审核后解冻原单与冻结定时器锁还未释放：{}", SystemConstant.DIS_UNFREEZE_BUSINESS_ORDER_AND_FREEZE_DELIVERY_SWITCH);
        }
        log.info("配销审核后解冻原单与冻结任务执行结束");
    }


    /**
     * @param disDeliveryList:
     * @param bizOrgCode:
     * @Description: 处理订单流（原订货单解冻+配销单冻结）
     * @Author: ZhangYao
     * @Date: 2023/7/19 19:32
     * @return: void
     **/
    private void handleOrderConfig(List<OrdDisDelivery> disDeliveryList, String bizOrgCode, String loginUsername) {
        List<OrderConfigFreezeDeliveryOrderOut> configDeliveryOrderOutList = ordDisDeliveryService.findOrderConfigDeliveryOrderOut(disDeliveryList, bizOrgCode);
        // 按同周期分组订货单
        Map<String, List<OrderConfigFreezeDeliveryOrderOut>> cycleDeliveryOrderMap = configDeliveryOrderOutList.stream()
                .collect(Collectors.groupingBy(orderConfigFreezeDeliveryOrderOut ->
                        orderConfigFreezeDeliveryOrderOut.getOrderCycleId() + SystemConstant.WAIT +
                                orderConfigFreezeDeliveryOrderOut.getTruncationDateTime() + SystemConstant.WAIT +
                                orderConfigFreezeDeliveryOrderOut.getStoreCode()));
        cycleDeliveryOrderMap.entrySet().forEach(entry -> {
            try {
                // 检查周期下配销单是否存在待审核和已预审。
                Optional<OrderConfigFreezeDeliveryOrderOut> checkOptional = entry.getValue().stream().filter(orderConfigFreezeDeliveryOrderOut ->
                        DeliveryOrderEnum.PENDING.getKey().equals(orderConfigFreezeDeliveryOrderOut.getDeliveryStatusCode())
                                || DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(orderConfigFreezeDeliveryOrderOut.getDeliveryStatusCode())).findFirst();
                if (checkOptional.isPresent()) {
                    OrderConfigFreezeDeliveryOrderOut orderConfigFreezeDeliveryOrderOut = checkOptional.get();
                    log.info("门店{}订货周期{}下配销单含有待审核或者已预审，不能进行释放冻结", orderConfigFreezeDeliveryOrderOut.getStoreCode(),
                            orderConfigFreezeDeliveryOrderOut.getTruncationDateTime());
                    return;
                }
                // 释放原订货单，冻结改批批销单
                Long orderCycleId = Long.valueOf(entry.getKey().split(SystemConstant.WAIT)[0]);
                String truncationDateTime = entry.getKey().split(SystemConstant.WAIT)[1];
                String storeCode = entry.getKey().split(SystemConstant.WAIT)[2];
                List<OrdDisOrder> needUnfreezeOrderList = disOrderHandle.findNeedUnfreezeOrderNo(orderCycleId);
                List<String> needUnfreezeOrderNoList = Lists.newArrayList();
                needUnfreezeOrderList.forEach(order -> {
                    needUnfreezeOrderNoList.add(order.getOrderNo());
                    order.setUpdater(loginUsername);
                    order.setUpdateTime(LocalDateTime.now());
                });
                List<FrozenOrderIn> frozenOrderIns = Lists.newArrayList();
                Map<String, Long> deliveryOrderMap = new HashMap<>();
                List<Long> deliveryIdList = Lists.newArrayList();
                entry.getValue().forEach(orderConfigFreezeDeliveryOrderOut -> {
                    // 非作废重新冻结
                    if (DeliveryOrderEnum.INVALID.getKey().equals(orderConfigFreezeDeliveryOrderOut.getDeliveryStatusCode())) {
                        return;
                    }
                    FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
                    frozenOrderIn.setBusinessNo(orderConfigFreezeDeliveryOrderOut.getDeliveryOrderNo());
                    frozenOrderIn.setAmount(orderConfigFreezeDeliveryOrderOut.getDistributionAmount());
                    frozenOrderIn.setBusinessType(FundTypeEnum.DISTRIBUTION_AUDIT.getCode());
                    frozenOrderIns.add(frozenOrderIn);
                    deliveryOrderMap.put(orderConfigFreezeDeliveryOrderOut.getDeliveryOrderNo(), orderConfigFreezeDeliveryOrderOut.getDeliveryOrderId());
                    deliveryIdList.add(orderConfigFreezeDeliveryOrderOut.getDeliveryOrderId());
                });
                UnFrozenAndFreezeIn unFrozenAndFreezeIn = new UnFrozenAndFreezeIn();
                unFrozenAndFreezeIn.setUnFrozenBusinessNos(needUnfreezeOrderNoList);
                unFrozenAndFreezeIn.setPrincipalCode(storeCode);
                unFrozenAndFreezeIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
                unFrozenAndFreezeIn.setBizOrgCode(bizOrgCode);
                unFrozenAndFreezeIn.setFrozenOrders(frozenOrderIns);
                Response response = fundServer.unFrozenAndFrozen(unFrozenAndFreezeIn);
                String deliveryOrderContent;
                String orderContent;
                if (!response.isSuccess()) {
                    log.info("解冻原订货单冻结配销单入参---{}", JSONObject.toJSONString(unFrozenAndFreezeIn));
                    log.error("门店{}订货周期{}下配销单调用资管解冻冻结异常{}", storeCode, truncationDateTime, response.getMessage());
                    deliveryOrderContent = "订货周期" + truncationDateTime + "下配销单冻结失败";
                    orderContent = "订货单解冻失败";
                } else {
                    deliveryOrderContent = "订货周期" + truncationDateTime + "下配销单冻结成功";
                    orderContent = "订货单解冻成功";
//                    ordDisDeliveryService.batchUpdateFreeze(deliveryIdList, SystemConstant.SYSTEM_USER, bizOrgCode);
//                    disOrderHandle.batchReleaseOrderList(needUnfreezeOrderList);
                    disFreezeHandle.handleOrderConfig(deliveryIdList, needUnfreezeOrderList, bizOrgCode);
                }
                frozenOrderIns.forEach(frozenOrderIn -> {
                    BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                            OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                            String.valueOf(deliveryOrderMap.get(frozenOrderIn.getBusinessNo())),
                            OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                            deliveryOrderContent, new Date(),
                            loginUsername);
                    asyncLogService.sendAsyncSaveLogByMq(businessLog);
                });
                needUnfreezeOrderList.forEach(order -> {
                    BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                            OrdLogTypeEnum.DIS_ORDER.getName(),
                            String.valueOf(order.getId()),
                            OrdLogTypeEnum.DIS_ORDER.getCode(),
                            orderContent, new Date(),
                            loginUsername);
                    asyncLogService.sendAsyncSaveLogByMq(businessLog);
                });
            } catch (Exception e) {
                log.error("执行订单流配置{}的配销单冻结流程异常", entry.getKey(), e);
            }
        });
    }

    /**
     * @param disDeliveryList:
     * @param bizOrgCode:
     * @Description: 处理首单铺货（原铺货单解冻+配销单冻结）
     * @Author: ZhangYao
     * @Date: 2023/7/19 19:29
     * @return: void
     **/
    private void handleFirstOrder(List<OrdDisDelivery> disDeliveryList, String bizOrgCode, String loginUsername) {
        List<FirstOrderFreezeDeliveryOrderOut> firstDeliveryOrderOutList = ordDisDeliveryService.findFirstOrderDeliveryOrderOut(disDeliveryList, bizOrgCode);
        // 按铺货单分组
        Map<String, List<FirstOrderFreezeDeliveryOrderOut>> firstDeliveryOrderMap = firstDeliveryOrderOutList.stream()
                .collect(Collectors.groupingBy(firstOrderFreezeDeliveryOrderOut ->
                        firstOrderFreezeDeliveryOrderOut.getFirstOrderNo() + SystemConstant.WAIT +
                                firstOrderFreezeDeliveryOrderOut.getFirstOrderId() + SystemConstant.WAIT +
                                firstOrderFreezeDeliveryOrderOut.getStoreCode()));
        firstDeliveryOrderMap.entrySet().forEach(entry -> {
            try {
                // 检查铺货单下配销单是否存在待审核和已预审
                Optional<FirstOrderFreezeDeliveryOrderOut> checkOptional = entry.getValue().stream().filter(firstOrderFreezeDeliveryOrderOut ->
                        DeliveryOrderEnum.PENDING.getKey().equals(firstOrderFreezeDeliveryOrderOut.getDeliveryStatusCode())
                                || DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(firstOrderFreezeDeliveryOrderOut.getDeliveryStatusCode())).findFirst();
                if (checkOptional.isPresent()) {
                    FirstOrderFreezeDeliveryOrderOut orderConfigFreezeDeliveryOrderOut = checkOptional.get();
                    log.info("门店{}铺货单{}下配销单含有待审核或者已预审，不能进行释放冻结", orderConfigFreezeDeliveryOrderOut.getStoreCode(),
                            orderConfigFreezeDeliveryOrderOut.getFirstOrderNo());
                    return;
                }
                // 释放原订货单，冻结改批批销单
                String firstOrderNo = entry.getKey().split(SystemConstant.WAIT)[0];
                Long firstOrderId = Long.parseLong(entry.getKey().split(SystemConstant.WAIT)[1]);
                String storeCode = entry.getKey().split(SystemConstant.WAIT)[2];
                List<FrozenOrderIn> frozenOrderIns = Lists.newArrayList();
                Map<String, Long> deliveryOrderMap = new HashMap<>();
                List<Long> idList = Lists.newArrayList();
                entry.getValue().forEach(firstOrderFreezeDeliveryOrderOut -> {
                    // 非作废重新冻结
                    if (DeliveryOrderEnum.INVALID.getKey().equals(firstOrderFreezeDeliveryOrderOut.getDeliveryStatusCode())) {
                        return;
                    }
                    FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
                    frozenOrderIn.setBusinessNo(firstOrderFreezeDeliveryOrderOut.getDeliveryOrderNo());
                    frozenOrderIn.setAmount(firstOrderFreezeDeliveryOrderOut.getDistributionAmount());
                    frozenOrderIn.setBusinessType(FundTypeEnum.DISTRIBUTION_AUDIT.getCode());
                    frozenOrderIns.add(frozenOrderIn);
                    deliveryOrderMap.put(firstOrderFreezeDeliveryOrderOut.getDeliveryOrderNo(), firstOrderFreezeDeliveryOrderOut.getDeliveryOrderId());
                    idList.add(firstOrderFreezeDeliveryOrderOut.getDeliveryOrderId());
                });
                OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstService.selectByPrimaryKey(firstOrderId);
                List<String> needUnfreezeOrderNoList = Lists.newArrayList();
                if (FirstOrderFreezeEnum.FREEZE.getKey().equals(ordDisOrderFirst.getFreezeStatus())) {
                    needUnfreezeOrderNoList.add(firstOrderNo);
                }
                UnFrozenAndFreezeIn unFrozenAndFreezeIn = new UnFrozenAndFreezeIn();
                unFrozenAndFreezeIn.setUnFrozenBusinessNos(needUnfreezeOrderNoList);
                unFrozenAndFreezeIn.setPrincipalCode(storeCode);
                unFrozenAndFreezeIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
                unFrozenAndFreezeIn.setBizOrgCode(bizOrgCode);
                unFrozenAndFreezeIn.setFrozenOrders(frozenOrderIns);
                Response response = fundServer.unFrozenAndFrozen(unFrozenAndFreezeIn);
                String deliveryOrderContent;
                String firstOrderContent;
                if (!response.isSuccess()) {
                    log.error("门店{}铺货单{}下配销单调用资管解冻冻结异常{}", storeCode, firstOrderNo, response.getMessage());
                    deliveryOrderContent = "门店" + storeCode + "铺货单" + firstOrderNo + "下配销单冻结失败";
                    firstOrderContent = "铺货单解冻失败";
                } else {
                    deliveryOrderContent = "门店" + storeCode + "铺货单" + firstOrderNo + "下配销单冻结成功";
                    firstOrderContent = "解冻铺货单冻结配销单环节中，铺货单" + firstOrderNo + "解冻成功";
                    ordDisOrderFirst.setUpdater(SystemConstant.SYSTEM_USER);
//                    ordDisDeliveryService.batchUpdateFreeze(idList, SystemConstant.SYSTEM_USER, bizOrgCode);
//                    ordDisOrderFirstService.unFreezeDisFirstOrder(ordDisOrderFirst);
                    disFreezeHandle.handleFirstOrder(idList, ordDisOrderFirst);
                }
                frozenOrderIns.forEach(frozenOrderIn -> {
                    BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                            OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                            String.valueOf(deliveryOrderMap.get(frozenOrderIn.getBusinessNo())),
                            OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                            deliveryOrderContent, new Date(),
                            loginUsername);
                    asyncLogService.sendAsyncSaveLogByMq(businessLog);
                });
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                        OrdLogTypeEnum.FIRST_ORDER_GOODS.getName(),
                        String.valueOf(firstOrderId),
                        OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                        firstOrderContent, new Date(),
                        loginUsername);
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
            } catch (NumberFormatException e) {
                log.error("执行铺货单{}的配销单冻结流程异常", entry.getKey(), e);
            }
        });
    }

//    /**
//     * @param disDeliveryList:
//     * @param bizOrgCode:
//     * @Description: 处理手动创建配销单（原订货单解冻+配销单冻结）
//     * @Author: ZhangYao
//     * @Date: 2023/7/19 19:31
//     * @return: void
//     **/
//    private void handleManualDeliveryOrder(List<OrdDisDelivery> disDeliveryList, String bizOrgCode, String loginUsername) {
//        disDeliveryList.forEach(ordDisDelivery -> {
//            try {
//                if (DeliveryOrderEnum.PENDING.getKey().equals(ordDisDelivery.getDeliveryStatusCode())
//                        || DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
//                    log.info("门店{}手动创建配销单{}为待审核或者已预审，不能进行释放冻结", ordDisDelivery.getStoreCode(),
//                            ordDisDelivery.getDeliveryOrderNo());
//                    return;
//                }
//                List<FrozenOrderIn> frozenOrderIns = Lists.newArrayList();
//                FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
//                frozenOrderIn.setBusinessNo(ordDisDelivery.getDeliveryOrderNo());
//                frozenOrderIn.setAmount(ordDisDelivery.getDistributionAmount());
//                frozenOrderIn.setBusinessType(FundTypeEnum.DISTRIBUTION_AUDIT.getCode());
//                frozenOrderIns.add(frozenOrderIn);
//                StoreFrozenIn storeFrozenIn = new StoreFrozenIn();
//                storeFrozenIn.setFrozenOrders(frozenOrderIns);
//                storeFrozenIn.setPrincipalCode(ordDisDelivery.getStoreCode());
//                storeFrozenIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
//                storeFrozenIn.setBizOrgCode(bizOrgCode);
//                Response response = fundServer.frozen(storeFrozenIn);
//                if (!response.isSuccess()) {
//                    BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
//                            OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
//                            String.valueOf(ordDisDelivery.getId()),
//                            OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
//                            "手动创建配销单冻结失败", new Date(),
//                            loginUsername);
//                    asyncLogService.sendAsyncSaveLogByMq(businessLog);
//                    log.error("手动创建配销单{}冻结调用资管异常{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
//                } else {
//                    ordDisDeliveryService.batchUpdateFreeze(Collections.singletonList(ordDisDelivery.getId()), SystemConstant.SYSTEM_USER, bizOrgCode);
//                    BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
//                            OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
//                            String.valueOf(ordDisDelivery.getId()),
//                            OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
//                            "手动创建配销单审核冻结成功", new Date(),
//                            loginUsername);
//                    asyncLogService.sendAsyncSaveLogByMq(businessLog);
//                }
//            } catch (Exception e) {
//                log.error("执行手工创建配销单{}的配销单冻结流程异常", ordDisDelivery.getDeliveryOrderNo(), e);
//            }
//        });
//
//    }
}
