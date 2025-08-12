package com.edc.erp.ord.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.RequestOrderStatusEnum;
import com.edc.erp.common.model.in.purchase.FindTransferOrderIn;
import com.edc.erp.common.model.in.purchase.GoodsDtlsIn;
import com.edc.erp.common.model.in.purchase.TransferNoticePurchaseIn;
import com.edc.erp.common.model.out.TransferDeliveryOrderDetailOut;
import com.edc.erp.common.model.out.purchase.OrderDeliverRequestOut;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryMapper;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDetailService;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.ord.handle.DtsErpBusinessOrderHandle;
import com.edc.erp.ord.model.out.TransferDeliveryOrderPushPurVO;
import com.edc.erp.ord.model.out.TransferDirDeliveryOrderOut;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 中转采购定时器
 * @since 2022/12/15 14:50
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransferDeliveryOrderScheduler {

    private final TaskLockUtils taskLockUtils;

    private final RedisService redisService;

    private static final String CLOSE_AUTO_TRANSFER_DELIVERY_ORDER_SWITCH_KEY = "OPEN";

    private final OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    private final OrdDisDeliveryService ordDisDeliveryService;

    private final OrdDirDeliveryMapper ordDirDeliveryMapper;

    private final DtsErpBusinessOrderHandle dtsErpBusinessOrderHandle;

    @XxlJob("transferDeliveryOrderJobOne")
    public ReturnT<String> transferDeliveryOrderOne() {
        log.info("transferDeliveryOrderJobOne----start");
        String jobParam = XxlJobHelper.getJobParam();
        List<TransferDeliveryOrderPushPurVO> list = JSONArray.parseArray(jobParam, TransferDeliveryOrderPushPurVO.class);
        executeJob(list);
        log.info("transferDeliveryOrderJobOne----end");
        return ReturnT.SUCCESS;
    }

    @XxlJob("transferDeliveryOrderJobTwo")
    public ReturnT<String> transferDeliveryOrderJobTwo() {
        log.info("transferDeliveryOrderJobTwo----start");
        String jobParam = XxlJobHelper.getJobParam();
        List<TransferDeliveryOrderPushPurVO> list = JSONArray.parseArray(jobParam, TransferDeliveryOrderPushPurVO.class);
        executeJob(list);
        log.info("transferDeliveryOrderJobTwo----end");
        return ReturnT.SUCCESS;
    }

    private void executeJob(List<TransferDeliveryOrderPushPurVO> list) {
        list.forEach(transferDeliveryOrderPushPurVO -> {
            try {
                TransferDirDeliveryOrderOut transferDirDeliveryOrderOut = this.initTransferNoticePurchaseIn(transferDeliveryOrderPushPurVO.getBizOrgCode(), transferDeliveryOrderPushPurVO.getExecuteTime());
                List<TransferNoticePurchaseIn> transferNoticePurchaseIns = transferDirDeliveryOrderOut.getTransferNoticePurchaseIns();
                if (CollectionUtils.isEmpty(transferNoticePurchaseIns)) {
                    log.info("{}没有中转商品", OrgCodeConvertEnum.getNameByBizOrgCode(transferDeliveryOrderPushPurVO.getBizOrgCode()));
                    return;
                }
                log.info("{}没有中转商品条数是---{}", OrgCodeConvertEnum.getNameByBizOrgCode(transferDeliveryOrderPushPurVO.getBizOrgCode()), transferNoticePurchaseIns.size());
                log.info("{}组织调用定时中转发采购单入参------{}", OrgCodeConvertEnum.getNameByBizOrgCode(transferDeliveryOrderPushPurVO.getBizOrgCode()), JSONArray.toJSONString(transferNoticePurchaseIns));
                dtsErpBusinessOrderHandle.updateDtlsCycleAndSendPur(transferDirDeliveryOrderOut, transferNoticePurchaseIns, transferDeliveryOrderPushPurVO.getBizOrgCode());
            } catch (Exception e) {
                log.error("{}组织执行中转配货推采购异常", transferDeliveryOrderPushPurVO.getBizOrgCode(), e);
            }
        });
    }

//    /**
//     * 西安组织中转商品生成采购单
//     */
//    @Scheduled(cron = "0 0 10 * * ?")
//    public void autoXIANTransferDeliveryOrderJob() {
//        log.info("开始执行西安组织中转商品生成采购单任务");
//        String orderSwitch = redisService.get(SystemConstant.CLOSE_AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH_KEY + SystemConstant.SHORT_LINE + OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode());
//        if (CLOSE_AUTO_TRANSFER_DELIVERY_ORDER_SWITCH_KEY.equals(orderSwitch)) {
//            log.info("西安组织任务开关未打开......");
//            return;
//        }
//        long lockTimeOut = 5 * 60 * 1000;
//        String lockKey = SystemConstant.AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH + SystemConstant.SHORT_LINE + OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode();
//        if (taskLockUtils.lock(lockKey, lockTimeOut)) {
//            try {
//                TransferDirDeliveryOrderOut transferDirDeliveryOrderOut = this.initTransferNoticePurchaseIn(OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode(), SystemConstant.XIAN_EXECUTE_TIME);
//                List<TransferNoticePurchaseIn> transferNoticePurchaseIns = transferDirDeliveryOrderOut.getTransferNoticePurchaseIns();
//                if (CollectionUtils.isEmpty(transferNoticePurchaseIns)) {
//                    log.info("西安组织没有中转商品");
//                    return;
//                }
//                log.info("西安组织中转商品条数是---{}", transferNoticePurchaseIns.size());
//                log.info("西安组织调用定时中转发采购单入参------{}", JSONArray.toJSONString(transferNoticePurchaseIns));
//                dtsErpBusinessOrderHandle.updateDtlsCycleAndSendPur(transferDirDeliveryOrderOut, transferNoticePurchaseIns, OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode());
////                if (!response.isSuccess()) {
////                    log.error("西安组织中转商品生成采购单失败" + (StringUtils.isNotBlank(response.getMessage()) ? response.getMessage() : null));
////                } else {
////                    redisService.setIfAbsent(transferDirDeliveryOrderOut.getTruncationDateTimeKey(), StringUtils.join(transferDirDeliveryOrderOut.getTruncationDateTime().toArray(), ","), 3, TimeUnit.DAYS);
////                    //发送采购成功修改订单明细的结算周期
////                    if (CollectionUtils.isNotEmpty(transferDirDeliveryOrderOut.getOrderDirDeliveryDetails())){
////                        ordDirDeliveryDetailService.batchUpdate(transferDirDeliveryOrderOut.getOrderDirDeliveryDetails());
////                    }
////                    if (CollectionUtils.isNotEmpty(transferDirDeliveryOrderOut.getOrderDisDeliveryDetails())){
////                        ordDisDeliveryDetailService.batchUpdate(transferDirDeliveryOrderOut.getOrderDisDeliveryDetails());
////                    }
////                }
//            } catch (Exception e) {
//                log.error("西安组织执行中转商品生成采购单任务异常：", e);
//            } finally {
//                taskLockUtils.unlock(lockKey);
//            }
//        } else {
//            log.info("西安中转商品生成采购单任务定时器锁还未释放：{}", lockKey);
//        }
//        log.info("西安组织执行中转商品生成采购单任务结束");
//    }
//
//    /**
//     * 天岁组织中转商品生成采购单
//     */
//    @Scheduled(cron = "0 0 10 * * ?")
//    public void autoTSTransferDeliveryOrderJob() {
//        log.info("天岁组织开始执行中转商品生成采购单任务");
//        String orderSwitch = redisService.get(SystemConstant.CLOSE_AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH_KEY + SystemConstant.SHORT_LINE + OrgCodeConvertEnum.TS_MYT.getBizOrgCode());
//        if (CLOSE_AUTO_TRANSFER_DELIVERY_ORDER_SWITCH_KEY.equals(orderSwitch)) {
//            log.info("天岁组织任务开关未打开......");
//            return;
//        }
//        long lockTimeOut = 5 * 60 * 1000;
//        String lockKey = SystemConstant.AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH + SystemConstant.SHORT_LINE + OrgCodeConvertEnum.TS_MYT.getBizOrgCode();
//        if (taskLockUtils.lock(lockKey, lockTimeOut)) {
//            try {
//                TransferDirDeliveryOrderOut transferDirDeliveryOrderOut = this.initTransferNoticePurchaseIn(OrgCodeConvertEnum.TS_MYT.getBizOrgCode(), SystemConstant.TS_EXECUTE_TIME);
//                List<TransferNoticePurchaseIn> transferNoticePurchaseIns = transferDirDeliveryOrderOut.getTransferNoticePurchaseIns();
//                if (CollectionUtils.isEmpty(transferNoticePurchaseIns)) {
//                    log.info("天岁组织没有中转商品");
//                    return;
//                }
//                log.info("天岁组织中转商品条数是---{}", transferNoticePurchaseIns.size());
//                log.info("天岁组织调用定时中转发采购单入参------{}", JSONArray.toJSONString(transferNoticePurchaseIns));
//                dtsErpBusinessOrderHandle.updateDtlsCycleAndSendPur(transferDirDeliveryOrderOut, transferNoticePurchaseIns, OrgCodeConvertEnum.TS_MYT.getBizOrgCode());
//            } catch (Exception e) {
//                log.error("天岁组织执行中转商品生成采购单任务异常：", e);
//            } finally {
//                taskLockUtils.unlock(lockKey);
//            }
//        } else {
//            log.info("天岁组织中转商品生成采购单任务定时器锁还未释放：{}", lockKey);
//        }
//        log.info("天岁组织执行中转商品生成采购单任务结束");
//    }
//
//    @Scheduled(cron = "0 0 10 * * ?")
//    public void autoFXTransferDeliveryOrderJob() {
//        log.info("蜂行组织开始执行中转商品生成采购单任务");
//        String orderSwitch = redisService.get(SystemConstant.CLOSE_AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH_KEY + SystemConstant.SHORT_LINE + OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode());
//        if (CLOSE_AUTO_TRANSFER_DELIVERY_ORDER_SWITCH_KEY.equals(orderSwitch)) {
//            log.info("蜂行组织任务开关未打开......");
//            return;
//        }
//        long lockTimeOut = 5 * 60 * 1000;
//        String lockKey = SystemConstant.AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH + SystemConstant.SHORT_LINE + OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode();
//        if (taskLockUtils.lock(lockKey, lockTimeOut)) {
//            try {
//                TransferDirDeliveryOrderOut transferDirDeliveryOrderOut = this.initTransferNoticePurchaseIn(OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode(), SystemConstant.FX_EXECUTE_TIME);
//                List<TransferNoticePurchaseIn> transferNoticePurchaseIns = transferDirDeliveryOrderOut.getTransferNoticePurchaseIns();
//                if (CollectionUtils.isEmpty(transferNoticePurchaseIns)) {
//                    log.info("蜂行组织没有中转商品");
//                    return;
//                }
//                log.info("蜂行组织中转商品条数是---{}", transferNoticePurchaseIns.size());
//                log.info("蜂行组织调用定时中转发采购单入参------{}", JSONArray.toJSONString(transferNoticePurchaseIns));
//                dtsErpBusinessOrderHandle.updateDtlsCycleAndSendPur(transferDirDeliveryOrderOut, transferNoticePurchaseIns, OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode());
//            } catch (Exception e) {
//                log.error("蜂行组织执行中转商品生成采购单任务异常：", e);
//            } finally {
//                taskLockUtils.unlock(lockKey);
//            }
//        } else {
//            log.info("蜂行组织中转商品生成采购单任务定时器锁还未释放：{}", lockKey);
//        }
//        log.info("蜂行组织执行中转商品生成采购单任务结束");
//    }
//
//    /**
//     * 郑州组织中转商品生成采购单
//     */
//    @Scheduled(cron = "0 30 07 * * ?")
//    public void autoZHENGZHOUTransferDeliveryOrderJob() {
//        log.info("郑州组织开始执行中转商品生成采购单任务");
//        String orderSwitch = redisService.get(SystemConstant.CLOSE_AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH_KEY + SystemConstant.SHORT_LINE + OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode());
//        if (CLOSE_AUTO_TRANSFER_DELIVERY_ORDER_SWITCH_KEY.equals(orderSwitch)) {
//            log.info("郑州组织任务开关未打开......");
//            return;
//        }
//        long lockTimeOut = 5 * 60 * 1000;
//        String lockKey = SystemConstant.AUTO_TRANSFER_DIR_DELIVERY_ORDER_SWITCH + SystemConstant.SHORT_LINE + OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode();
//        if (taskLockUtils.lock(lockKey, lockTimeOut)) {
//            try {
//                TransferDirDeliveryOrderOut transferDirDeliveryOrderOut = this.initTransferNoticePurchaseIn(OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode(), SystemConstant.ZHENGZHOU_EXECUTE_TIME);
//                List<TransferNoticePurchaseIn> transferNoticePurchaseIns = transferDirDeliveryOrderOut.getTransferNoticePurchaseIns();
//                if (CollectionUtils.isEmpty(transferNoticePurchaseIns)) {
//                    log.info("郑州组织没有中转商品");
//                    return;
//                }
//                log.info("郑州组织中转商品条数是---{}", transferNoticePurchaseIns.size());
//                log.info("郑州组织调用定时中转发采购单入参------{}", JSONArray.toJSONString(transferNoticePurchaseIns));
//                dtsErpBusinessOrderHandle.updateDtlsCycleAndSendPur(transferDirDeliveryOrderOut, transferNoticePurchaseIns, OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode());
//            } catch (Exception e) {
//                log.error("郑州组织执行中转商品生成采购单任务异常：", e);
//            } finally {
//                taskLockUtils.unlock(lockKey);
//            }
//        } else {
//            log.info("郑州组织中转商品生成采购单任务定时器锁还未释放：{}", lockKey);
//        }
//        log.info("郑州组织执行中转商品生成采购单任务结束");
//    }

    /**
     * 初始化配货单中转商品发送采购入参
     *
     * @return
     */
    public TransferDirDeliveryOrderOut initTransferNoticePurchaseIn(String bizOrgCode, String executeTime) {
        List<TransferNoticePurchaseIn> transferNoticePurchaseIns = new ArrayList<>();
        List<TransferDeliveryOrderDetailOut> transferDeliveryOrderDetails = new ArrayList<>();
        //配销单明细
        List<TransferDeliveryOrderDetailOut> orderDisDeliveryDetails = new ArrayList<>();
        //配货单明细
        List<TransferDeliveryOrderDetailOut> orderDirDeliveryDetails = new ArrayList<>();
        //截单时间的开始与结束时间
        String startTime = DateUtil.format(LocalDateTime.of(LocalDate.now().minusDays(NumberUtil.INTEGER_ONE.longValue()), LocalTime.parse(executeTime)), DatePattern.NORM_DATETIME_PATTERN);
        String endTime = DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN);
        String carryForwardCycle = bizOrgCode + DateUtil.format(LocalDateTime.now(), "yyMMdd");

        //获取已处理的截止时间
        List<String> truncationDateTimeList = new ArrayList<>();
        String truncationDateTimeKey = redisService.get(carryForwardCycle + "truncationDateTimeKey");
        if (StringUtils.isNotEmpty(truncationDateTimeKey)) {
            truncationDateTimeList.addAll(Arrays.asList(truncationDateTimeKey.split(SystemConstant.COMMA)));
        }
        //获取截单时间
        List<OrderDeliverRequestOut> list = new ArrayList<>();
        list.addAll(ordDirDeliveryMapper.findTruncationDateTime(startTime, endTime, DistributionWaysEnum.TRANSFER.getType(), bizOrgCode));
        list.addAll(ordDisDeliveryService.findTruncationDateTime(startTime, endTime, DistributionWaysEnum.TRANSFER.getType(), bizOrgCode));
        Map<String, List<OrderDeliverRequestOut>> findTruncationDateTimeList = list.stream().collect(Collectors.groupingBy(OrderDeliverRequestOut::getTruncationDateTime));
        List<String> timeList = new ArrayList<>();
        for (String truncation : findTruncationDateTimeList.keySet()) {
            List<OrderDeliverRequestOut> orderDeliverRequestOutList = findTruncationDateTimeList.get(truncation).stream().filter(e -> !RequestOrderStatusEnum.EXCRETED.getKey().equals(e.getStatusCode())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orderDeliverRequestOutList) && (CollectionUtils.isEmpty(truncationDateTimeList) || truncationDateTimeList.stream().noneMatch(m -> m.equals(truncation)))) {
                timeList.add(truncation);
            }
        }
        if (CollectionUtils.isEmpty(timeList)) {
            return TransferDirDeliveryOrderOut.builder().transferNoticePurchaseIns(transferNoticePurchaseIns).truncationDateTime(truncationDateTimeList).truncationDateTimeKey(carryForwardCycle + "truncationDateTimeKey").carryForwardCycle(carryForwardCycle).build();
        }
        truncationDateTimeList.addAll(timeList);
        //获取配货单中转订单
        FindTransferOrderIn findTransferOrderIn = FindTransferOrderIn.builder().distributionType(DistributionWaysEnum.TRANSFER.getType())
                .deliveryStatusCode(DeliveryOrderEnum.PREVIEWAPPROVED.getKey()).truncationDateTime(timeList).bizOrgCode(bizOrgCode).build();
        List<OrdDirDelivery> ordDirLists = ordDirDeliveryMapper.findTransferOrderByCarryForwardCycle(findTransferOrderIn);
        if (CollectionUtils.isNotEmpty(ordDirLists)) {
            //查询所有中转商品配货单明细
            List<Long> idList = ordDirLists.stream().map(OrdDirDelivery::getId).collect(Collectors.toList());
            orderDirDeliveryDetails = ordDirDeliveryDetailService.findTransferDeliveryOrderDetails(idList);
        }
        //获取配销单中转订单
        List<OrdDisDelivery> ordDisList = ordDisDeliveryService.findTransferOrderByCarryForwardCycle(findTransferOrderIn);
        if (CollectionUtils.isNotEmpty(ordDisList)) {
            //查询所有中转商品配销单明细
            List<Long> idList = ordDisList.stream().map(OrdDisDelivery::getId).collect(Collectors.toList());
            orderDisDeliveryDetails = ordDisDeliveryDetailService.findTransferDeliveryOrderDetails(idList);
        }
        transferDeliveryOrderDetails.addAll(orderDisDeliveryDetails);
        transferDeliveryOrderDetails.addAll(orderDirDeliveryDetails);
        if (CollectionUtils.isNotEmpty(transferDeliveryOrderDetails)) {
            Map<String, List<TransferDeliveryOrderDetailOut>> vendorDetails = transferDeliveryOrderDetails.stream().collect(Collectors.groupingBy(item ->
                    item.getVendorCode() + "_" + item.getWarehouseCode() + "_" + item.getStockCode() + "_" + carryForwardCycle + "_" + item.getOrderPriority()
            ));
            StringJoiner stringJoiner = new StringJoiner(";");
            for (String key : vendorDetails.keySet()) {
                stringJoiner.add(key);
            }
            log.info("定时发送采购拆分采购单参数key" + stringJoiner + "---------------------------------------------------");
            for (List<TransferDeliveryOrderDetailOut> transferDeliveryOrderDetailOuts : vendorDetails.values()) {
                TransferNoticePurchaseIn transferNoticePurchaseIn = new TransferNoticePurchaseIn();
                if (CollectionUtils.isNotEmpty(transferDeliveryOrderDetailOuts)) {
                    TransferDeliveryOrderDetailOut transferDeliveryOrderDetailOut = transferDeliveryOrderDetailOuts.get(NumberUtil.INTEGER_ZERO);
                    BeanUtils.copy(transferDeliveryOrderDetailOut, transferNoticePurchaseIn);
                    transferNoticePurchaseIn.setCarryForwardCycle(carryForwardCycle);
                    List<GoodsDtlsIn> goodsDtlsInList = new ArrayList<>();
                    //行号
                    int lineNo = 1;
                    for (int i = 0; i < transferDeliveryOrderDetailOuts.size(); i++) {
                        String goodsCode = transferDeliveryOrderDetailOuts.get(i).getGoodsCode();
                        GoodsDtlsIn goodsMessage = goodsDtlsInList.stream().filter(e -> e.getGoodsCode().equals(goodsCode)).findFirst().orElse(null);
                        if (Objects.isNull(goodsMessage)) {
                            GoodsDtlsIn goodsDtlsIn = new GoodsDtlsIn();
                            goodsDtlsIn.setGoodsCode(goodsCode);
                            goodsDtlsIn.setLineNo(lineNo);
                            goodsDtlsIn.setTaxRate(transferDeliveryOrderDetailOuts.get(i).getSellTax().toString());
                            goodsDtlsIn.setTotalQty(transferDeliveryOrderDetailOuts.get(i).getOrderQuantity());
                            goodsDtlsInList.add(goodsDtlsIn);
                        } else {
                            goodsMessage.setTotalQty(goodsMessage.getTotalQty().add(transferDeliveryOrderDetailOuts.get(i).getOrderQuantity()));
                            goodsDtlsInList.removeIf(e -> e.getGoodsCode().equals(goodsCode));
                            goodsDtlsInList.add(goodsMessage);
                        }
                        lineNo++;
                    }
                    transferNoticePurchaseIn.setGoodsDtls(goodsDtlsInList);
                    transferNoticePurchaseIns.add(transferNoticePurchaseIn);
                }
            }
        }
        List<OrdDisDeliveryDetail> disDeliveryDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orderDisDeliveryDetails)) {
            String finalCarryForwardCycle = carryForwardCycle;
            orderDisDeliveryDetails.forEach(item -> {
                OrdDisDeliveryDetail ordDisDeliveryDetail = new OrdDisDeliveryDetail();
                BeanUtils.copy(item, ordDisDeliveryDetail);
                ordDisDeliveryDetail.setCarryForwardCycle(finalCarryForwardCycle);
                disDeliveryDetails.add(ordDisDeliveryDetail);
            });
        }
        List<OrdDirDeliveryDetail> dirDeliveryDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orderDirDeliveryDetails)) {
            String finalCarryForwardCycle = carryForwardCycle;
            orderDirDeliveryDetails.forEach(item -> {
                OrdDirDeliveryDetail ordDirDeliveryDetail = new OrdDirDeliveryDetail();
                BeanUtils.copy(item, ordDirDeliveryDetail);
                ordDirDeliveryDetail.setCarryForwardCycle(finalCarryForwardCycle);
                dirDeliveryDetails.add(ordDirDeliveryDetail);
            });
        }
        return TransferDirDeliveryOrderOut.builder().transferNoticePurchaseIns(transferNoticePurchaseIns).truncationDateTime(truncationDateTimeList).truncationDateTimeKey(carryForwardCycle + "truncationDateTimeKey").carryForwardCycle(carryForwardCycle)
                .orderDirDeliveryDetails(dirDeliveryDetails).orderDisDeliveryDetails(disDeliveryDetails).build();
    }

}
