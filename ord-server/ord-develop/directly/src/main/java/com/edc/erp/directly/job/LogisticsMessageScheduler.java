package com.edc.erp.directly.job;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.warning.NoticeLogisticsTemplateEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.model.in.warning.LogisticsMessageOrderQuantityIn;
import com.edc.erp.common.model.in.warning.LogisticsMessageQueryIn;
import com.edc.erp.common.model.in.warning.RobotMarkDownMessageIn;
import com.edc.erp.common.model.out.warning.CutPurchaseOrderSummaryOut;
import com.edc.erp.common.model.out.warning.LogisticsMessageOrderOut;
import com.edc.erp.common.service.DingTalkService;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.directly.distribution.handle.OrderDetailHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-08-11 15:06
 */
@Component
@Slf4j
public class LogisticsMessageScheduler {

    @Autowired
    private OrderHandle orderHandle;

    @Autowired
    private OrderDetailHandle orderDetailHandle;

    @Autowired
    private TaskLockUtils taskLockUtils;

    @Autowired
    private RedisService redisService;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private DingTalkService dingTalkService;

    @Value("${baoJiLogisticsDingTalkRobotUrl}")
    private String baoJiLogisticsDingTalkRobotUrl;

    /**
     * 宝鸡直营和加盟
     */
    private static final String BAO_JI_DIRECTLY_FRANCHISE_UNIT_CODE = "09,10,11";

    /**
     * 西安6点30物流消息门店单元
     */
    private static final String XIAN_A_UNIT_CODE = "01,03,07";

    /**
     * 西安12点30物流消息门店单元
     */
    private static final String XIAN_B_UNIT_CODE = "02,04,05,06,08";

    private static final String CLOSE_BJ_LOGISTICS_MESSAGE_SWITCH_KEY = "OPEN";

    /**
     * 关闭向西安物流发送接单汇总信息定时器开关值
     */
    private final static String CLOSE_XIAN_LOGISTICS_MESSAGE_SWITCH_KEY = "OPEN";


    @Value("${sendToLogisticsDingTalkRobotUrl}")
    private String sendToLogisticsDingTalkRobotUrl;

    /**
     * 宝鸡直营物流发消息任务
     */
    @Scheduled(cron = "0 30 5,6 * * ?")
    @Scheduled(cron = "0 0 7 * * ?")
    public void bjDirectlyFranchiseSendLogisticsMessageJob() {
        log.info("开始执行宝鸡直营加盟物流发消息任务");
        String cutOrderSwitch = redisService.get(SystemConstant.CLOSE_BJ_LOGISTICS_MESSAGE_SWITCH_KEY);
        if (CLOSE_BJ_LOGISTICS_MESSAGE_SWITCH_KEY.equals(cutOrderSwitch)) {
            log.info("宝鸡物流发消息任务开关未打开......");
            return;
        }
        if (taskLockUtils.lock(SystemConstant.BJ_LOGISTICS_MESSAGE_SWITCH, 5 * 60 * 1000)) {
            try {
                List<String> unitCodeStrList = Arrays.asList(BAO_JI_DIRECTLY_FRANCHISE_UNIT_CODE.split(SystemConstant.COMMA));
                this.sendLogisticsDingTalkMessageForXIAN(unitCodeStrList, "合作经营", OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode(), baoJiLogisticsDingTalkRobotUrl);
            } catch (Exception e) {
                log.error("执行宝鸡合作经营物流发消息任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.BJ_LOGISTICS_MESSAGE_SWITCH);
            }
        } else {
            log.info("宝鸡合作经营物流发消息任务定时器锁还未释放：{}", SystemConstant.BJ_LOGISTICS_MESSAGE_SWITCH);
        }
        log.info("执行宝鸡合作经营物流发消息任务结束");
    }

    /**
     * 向物流发送直营接单汇总信息
     */
    @Scheduled(cron = "30 29 06 * * ? ")
    @Scheduled(cron = "0 0 4-6 * * ? ")
    public void handleDirectlyCutPurchaseOrderSummaryJob() {
        String handleCutPurchaseOrderSummarySwitch = redisService.get(SystemConstant.HANDLE_DIRECTLY_CUT_PURCHASE_ORDER_SUMMARY_SWITCH_KEY);
        if (CLOSE_XIAN_LOGISTICS_MESSAGE_SWITCH_KEY.equals(handleCutPurchaseOrderSummarySwitch)) {
            log.info("发送西安截单时间06点30分订货消息开关未打开......");
            return;
        }
        log.info("开始执行西安截单时间06点30分订货汇总信息任务");
        if (taskLockUtils.lock(SystemConstant.HANDLE_DIRECTLY_CUT_PURCHASE_ORDER_SUMMARY_SWITCH, 8 * 60 * 1000)) {
            try {
                List<String> unitCodeList = Arrays.asList(XIAN_A_UNIT_CODE.split(SystemConstant.COMMA));
                this.sendLogisticsDingTalkMessageForXIAN(unitCodeList, "06:30", OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode(), sendToLogisticsDingTalkRobotUrl);
            } catch (Exception e) {
                log.error("执行向物流发送西安截单时间06点30分订货汇总信息任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.HANDLE_DIRECTLY_CUT_PURCHASE_ORDER_SUMMARY_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", SystemConstant.HANDLE_DIRECTLY_CUT_PURCHASE_ORDER_SUMMARY_SWITCH);
        }
        log.info("向物流发送西安截单时间06点30分订货汇总信息任务执行结束");
    }

    /**
     * 向物流发送直营接单汇总信息
     */
    @Scheduled(cron = "0 0 9-12 * * ? ")
    @Scheduled(cron = "30 29 12 * * ? ")
    public void handleFranchiseCutPurchaseOrderSummaryJob() {
        String handleCutPurchaseOrderSummarySwitch = redisService.get(SystemConstant.HANDLE_FRANCHISE_CUT_PURCHASE_ORDER_SUMMARY_SWITCH_KEY);
        if (CLOSE_XIAN_LOGISTICS_MESSAGE_SWITCH_KEY.equals(handleCutPurchaseOrderSummarySwitch)) {
            log.info("发送西安截单时间12点30分订货消息开关未打开......");
            return;
        }
        log.info("开始执行西安截单时间12点30分订货汇总信息任务");
        if (taskLockUtils.lock(SystemConstant.HANDLE_FRANCHISE_CUT_PURCHASE_ORDER_SUMMARY_SWITCH, 8 * 60 * 1000)) {
            try {
                List<String> unitCodeList = Arrays.asList(XIAN_B_UNIT_CODE.split(SystemConstant.COMMA));
                this.sendLogisticsDingTalkMessageForXIAN(unitCodeList, "12:30", OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode(), sendToLogisticsDingTalkRobotUrl);
            } catch (Exception e) {
                log.error("执行向物流发送西安截单时间12点30分订货汇总信息任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.HANDLE_FRANCHISE_CUT_PURCHASE_ORDER_SUMMARY_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", SystemConstant.HANDLE_FRANCHISE_CUT_PURCHASE_ORDER_SUMMARY_SWITCH);
        }
        log.info("向物流发送西安截单时间12点30分订货汇总信息任务执行结束");
    }

    private void sendLogisticsDingTalkMessageForXIAN(List<String> unitCodeList, String truncationDateTime, String bizOrgCode, String messageUrls) {
        List<String> storeCodeList = storeCenterService.findStoreCodeListByUnitCodeListAndOrgCode(unitCodeList, bizOrgCode);
        if (CollectionUtils.isEmpty(storeCodeList)) {
            log.info("合作经营出货未查到源门店");
            return;
        }
        CutPurchaseOrderSummaryOut cutPurchaseOrderSummaryOut = this.initCutPurchaseOrderSummaryOut(storeCodeList, bizOrgCode);
        this.sendMessage(messageUrls, truncationDateTime, cutPurchaseOrderSummaryOut);
    }

    private CutPurchaseOrderSummaryOut initCutPurchaseOrderSummaryOut (List<String> storeCodeList, String bizOrgCode) {
        LogisticsMessageQueryIn logisticsMessageQueryIn = new LogisticsMessageQueryIn();
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.TO_REQUEST_ORDER.getKey());
        orderStatusCodeList.add(OrderStatusEnum.PAID.getKey());
        orderStatusCodeList.add(OrderStatusEnum.SUBMIT.getKey());
        logisticsMessageQueryIn.setOrderStatusCodeList(orderStatusCodeList);
        logisticsMessageQueryIn.setBizOrgCode(bizOrgCode);
        logisticsMessageQueryIn.setBeginTruncationDateTime(LocalDate.now() + " 00:00:00");
        logisticsMessageQueryIn.setEndTruncationDateTime(LocalDate.now() + " 23:59:59");
        logisticsMessageQueryIn.setStoreCodeList(storeCodeList);
        List<LogisticsMessageOrderOut> orderList = orderHandle.findOrderByTruncationDateTimeAndOrderStatus(logisticsMessageQueryIn);
        if (CollectionUtils.isEmpty(orderList)) {
            return this.initCutOrderSummaryOut();
        } else {
            return this.handleCutPurchaseOrderSummaryOut(logisticsMessageQueryIn, orderList);
        }
    }

    private CutPurchaseOrderSummaryOut handleCutPurchaseOrderSummaryOut(LogisticsMessageQueryIn logisticsMessageQueryIn, List<LogisticsMessageOrderOut> orderList) {
        List<LogisticsMessageOrderOut> allList = Lists.newArrayList();
        Map<Integer, List<LogisticsMessageOrderOut>> cycleOrderList = orderList.stream().collect(Collectors.groupingBy(LogisticsMessageOrderOut::getOrderCycleId));
        Map<Integer, BigDecimal> cycleMinimumOrderAmountMap = new LinkedHashMap<>();
        orderList.forEach(logisticsMessageOrderOut -> cycleMinimumOrderAmountMap.put(logisticsMessageOrderOut.getOrderCycleId(), logisticsMessageOrderOut.getMinimumOrderAmount()));
        cycleOrderList.forEach((orderCycleId, orders) -> {
            BigDecimal minimumOrderAmount = cycleMinimumOrderAmountMap.get(orderCycleId);
            BigDecimal orderAmount = orders.stream().reduce(BigDecimal.ZERO, (x, y) -> x.add(y.getPayableAmount()), BigDecimal::add);
            if (orderAmount.compareTo(minimumOrderAmount) < 0) {
                return;
            }
            allList.addAll(orders);
        });
        if (CollectionUtils.isEmpty(allList)) {
            return this.initCutOrderSummaryOut();
        }
        // 按门店去重获取门店数
        Integer totalStoreQuantity = allList.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(LogisticsMessageOrderOut::getStoreCode))), ArrayList::new)).size();
        CutPurchaseOrderSummaryOut summaryStorePay = new CutPurchaseOrderSummaryOut();
        summaryStorePay.setTotalStoreQuantity(totalStoreQuantity);
        // 总金额
        BigDecimal totalPayAmount = allList.stream().reduce(BigDecimal.ZERO, (x, y) -> x.add(y.getPayableAmount()), BigDecimal::add);
        summaryStorePay.setTotalPayAmount(totalPayAmount);
        List<Integer> orderIdList = allList.stream().map(LogisticsMessageOrderOut::getId).collect(Collectors.toList());
        LogisticsMessageOrderQuantityIn logisticsMessageOrderQuantityIn = new LogisticsMessageOrderQuantityIn();
        logisticsMessageOrderQuantityIn.setOrderIdList(orderIdList);
        logisticsMessageOrderQuantityIn.setBizOrgCode(logisticsMessageQueryIn.getBizOrgCode());
        logisticsMessageOrderQuantityIn.setStatisticalType(1);
        summaryStorePay.setTotalQuantity(orderDetailHandle.sumOrderSkuQuantity(logisticsMessageOrderQuantityIn));
        logisticsMessageOrderQuantityIn.setStatisticalType(2);
        summaryStorePay.setTotalPackageQuantity(orderDetailHandle.sumOrderSkuQuantity(logisticsMessageOrderQuantityIn));
        summaryStorePay.setTitleOrgName("MYT");
        return summaryStorePay;
    }

    private CutPurchaseOrderSummaryOut initCutOrderSummaryOut() {
        CutPurchaseOrderSummaryOut summaryStorePay = new CutPurchaseOrderSummaryOut();
        summaryStorePay.setTotalPayAmount(BigDecimal.ZERO);
        summaryStorePay.setTotalQuantity(BigDecimal.ZERO);
        summaryStorePay.setTotalPackageQuantity(BigDecimal.ZERO);
        summaryStorePay.setTotalStoreQuantity(0);
        summaryStorePay.setTitleOrgName("MYT");
        return summaryStorePay;
    }

    private void sendMessage(String urls, String storeProperty, CutPurchaseOrderSummaryOut summaryStorePay) {
        String content = MessageFormat.format(NoticeLogisticsTemplateEnum.CUT_PURCHASE_ORDER_SUMMARY.getTemplateMessage(),
                summaryStorePay.getTitleOrgName(), storeProperty, summaryStorePay.getTotalStoreQuantity(), summaryStorePay.getTotalQuantity(),
                summaryStorePay.getTotalPackageQuantity(), summaryStorePay.getTotalPayAmount());
        RobotMarkDownMessageIn robotMarkDownMessageIn = new RobotMarkDownMessageIn();
        robotMarkDownMessageIn.setTitle(MessageFormat.format(NoticeLogisticsTemplateEnum.CUT_PURCHASE_ORDER_SUMMARY.getKey(), summaryStorePay.getTitleOrgName()));
        robotMarkDownMessageIn.setText(content);
        robotMarkDownMessageIn.setIsAtAll(true);
        try {
            if (StringUtils.isNotEmpty(urls)) {
                for (String url : urls.split(SystemConstant.COMMA)) {
                    robotMarkDownMessageIn.setUrl(url);
                    Response response = dingTalkService.sendDingTalkRobotMarkDownMessage(robotMarkDownMessageIn);
                    if (null != response && response.isSuccess()) {
                        log.info("发送截单汇总信息" + JSON.toJSONString(response));
                    }
                }
            }
        } catch (Exception e) {
            log.error("发送截单汇总信息异常", e);
        }
    }
}
