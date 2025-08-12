package com.edc.erp.wholesale.job;


import com.alibaba.fastjson.JSONArray;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.wholesale.handle.PushPurWholesaleShipmentHandle;
import com.edc.erp.wholesale.model.in.shipment.QueryPushPurIn;
import com.edc.erp.wholesale.model.out.shipment.TransferShipmentPushPurchaseOut;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.utils.DateUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 配销单金额过大提醒定时器
 * @author lishaobo
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PushPurWholesaleShipmentScheduler {

    private final RedisService redisService;

    private final TaskLockUtils taskLockUtils;
    /**
     * 关闭查询配销单金额过大定时器的值
     */
    private final static String CLOSE_KEY = "OPEN";

    private final WholesaleShipmentService wholesaleShipmentService;

    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;

    private final PushPurWholesaleShipmentHandle pushPurWholesaleShipmentHandle;

    @XxlJob("pushShipmentDetailToPur")
    public ReturnT<String> pushShipmentDetailToPur() {
        log.info("pushShipmentDetailToPur----start");
        String jobParam = XxlJobHelper.getJobParam();
        String[] bizOrgCodeArray = jobParam.split(SystemConstant.COMMA);
        for (String bizOrgCode : bizOrgCodeArray) {
            try {
                this.handlePushShipmentDetailToPur(bizOrgCode);
            } catch (Exception e) {
                log.info("{}中转商品生成采购单任务执行异常", bizOrgCode, e);
            }
        }
        log.info("pushShipmentDetailToPur----end");
        return ReturnT.SUCCESS;
    }

    @XxlJob("delayPushShipmentDetailToPur")
    public ReturnT<String> delayPushShipmentDetailToPur() {
        log.info("delayPushShipmentDetailToPur----start");
        String jobParam = XxlJobHelper.getJobParam();
        String[] bizOrgCodeArray = jobParam.split(SystemConstant.COMMA);
        for (String bizOrgCode : bizOrgCodeArray) {
            try {
                this.handleDelayPushShipmentDetailToPur(bizOrgCode);
            } catch (Exception e) {
                log.info("延迟推送{}中转商品生成采购单任务执行异常", bizOrgCode, e);
            }
        }
        log.info("delayPushShipmentDetailToPur----end");
        return ReturnT.SUCCESS;
    }


//    /**
//     * 西安批发中转推送采购
//     */
////    @Scheduled(cron = "0 0 9 * * ?")
////    @Scheduled(cron = "0 0/10 * * * ?")
//    public void xaPushShipmentDetailToPur() {
//        String bizOrgCode = OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode();
//        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_PUSH_SHIPMENT_TO_PUR_SWITCH_KEY + bizOrgCode);
//        if (CLOSE_KEY.equals(closeOrderSwitch)) {
//            log.info("{}中转商品生成采购单定时器开关未打开......", bizOrgCode);
//            return;
//        }
//        String redisKey = SystemConstant.PUSH_SHIPMENT_TO_PUR_SWITCH + SystemConstant.COLON + bizOrgCode;
//        log.info("开始执行{}中转商品生成采购单任务", bizOrgCode);
//        if (taskLockUtils.lock(redisKey, 60 * 8 * 1000)) {
//            this.handlePushShipmentDetailToPur(bizOrgCode, redisKey);
//        } else {
//            log.info("{}中转商品生成采购单定时器锁还未释放：{}", bizOrgCode, redisKey);
//        }
//        log.info("{}中转商品生成采购单任务执行结束", bizOrgCode);
//    }
//
//    /**
//     * 天岁批发中转推送采购
//     */
//    @Scheduled(cron = "0 0 9 * * ?")
////    @Scheduled(cron = "0 10,20,30 * * * ?")
////    @Scheduled(cron = "0 0/10 * * * ?")
//    public void tsShipmentDetailToPur() {
//        String bizOrgCode = OrgCodeConvertEnum.TS_MYT.getBizOrgCode();
//        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_PUSH_SHIPMENT_TO_PUR_SWITCH_KEY + bizOrgCode);
//        if (CLOSE_KEY.equals(closeOrderSwitch)) {
//            log.info("{}中转商品生成采购单定时器开关未打开......", bizOrgCode);
//            return;
//        }
//        String redisKey = SystemConstant.PUSH_SHIPMENT_TO_PUR_SWITCH + SystemConstant.COLON + bizOrgCode;
//        log.info("开始执行{}中转商品生成采购单任务", bizOrgCode);
//        if (taskLockUtils.lock(redisKey, 60 * 8 * 1000)) {
//            this.handlePushShipmentDetailToPur(bizOrgCode, redisKey);
//        } else {
//            log.info("{}中转商品生成采购单定时器锁还未释放：{}", bizOrgCode, redisKey);
//        }
//        log.info("{}中转商品生成采购单任务执行结束", bizOrgCode);
//    }
//
//    /**
//     * 郑州批发中转推送采购
//     */
//    @Scheduled(cron = "0 0 9 * * ?")
//    public void zzPushShipmentDetailToPur() {
//        String bizOrgCode = OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode();
//        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_PUSH_SHIPMENT_TO_PUR_SWITCH_KEY + bizOrgCode);
//        if (CLOSE_KEY.equals(closeOrderSwitch)) {
//            log.info("{}中转商品生成采购单定时器开关未打开......", bizOrgCode);
//            return;
//        }
//        String redisKey = SystemConstant.PUSH_SHIPMENT_TO_PUR_SWITCH + SystemConstant.COLON + bizOrgCode;
//        log.info("开始执行{}中转商品生成采购单任务", bizOrgCode);
//        if (taskLockUtils.lock(redisKey, 60 * 8 * 1000)) {
//            this.handlePushShipmentDetailToPur(bizOrgCode, redisKey);
//        } else {
//            log.info("{}中转商品生成采购单定时器锁还未释放：{}", bizOrgCode, redisKey);
//        }
//        log.info("{}中转商品生成采购单任务执行结束", bizOrgCode);
//    }
//
//    /**
//     * 西安供应链批发中转推送采购
//     */
//    @Scheduled(cron = "0 0 9 * * ?")
//    public void xaChainPushShipmentDetailToPur() {
//        String bizOrgCode = OrgCodeConvertEnum.XA_SUPPLY_CHAIN.getBizOrgCode();
//        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_PUSH_SHIPMENT_TO_PUR_SWITCH_KEY + bizOrgCode);
//        if (CLOSE_KEY.equals(closeOrderSwitch)) {
//            log.info("{}中转商品生成采购单定时器开关未打开......", bizOrgCode);
//            return;
//        }
//        String redisKey = SystemConstant.PUSH_SHIPMENT_TO_PUR_SWITCH + SystemConstant.COLON + bizOrgCode;
//        log.info("开始执行{}中转商品生成采购单任务", bizOrgCode);
//        if (taskLockUtils.lock(redisKey, 60 * 8 * 1000)) {
//            this.handlePushShipmentDetailToPur(bizOrgCode, redisKey);
//        } else {
//            log.info("{}中转商品生成采购单定时器锁还未释放：{}", bizOrgCode, redisKey);
//        }
//        log.info("{}中转商品生成采购单任务执行结束", bizOrgCode);
//    }

    /**
     * @Description: 处理推送采购
     * @Author: ZhangYao
     * @Date: 2023/12/13 11:28
     * @param bizOrgCode:
     * @return: void
     **/
    private void handlePushShipmentDetailToPur(String bizOrgCode) {
        try {
            LocalDateTime beginTime = LocalDateTime.of(LocalDate.now().minusDays(NumberUtil.INTEGER_ONE), LocalTime.of(9, 0, 0));
            LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 59, 59));
            QueryPushPurIn queryPushPurIn = new QueryPushPurIn();
            queryPushPurIn.setShipmentStatus(ShipmentStatusEnum.APPROVED.getCode());
            queryPushPurIn.setBeginTime(DateUtils.format(beginTime));
            queryPushPurIn.setEndTime(DateUtils.format(endTime));
            queryPushPurIn.setDistributionType(DistributionWaysEnum.TRANSFER.getType());
            queryPushPurIn.setBizOrgCode(bizOrgCode);
            List<Long> idList = wholesaleShipmentService.findNeedPushPurWholesaleShipmentId(queryPushPurIn);
            if (CollectionUtils.isEmpty(idList)) {
                return;
            }
            idList.forEach(id -> {
                String key = SystemConstant.SHIPMENT_TRANSFER_IDS + bizOrgCode + SystemConstant.COLON + id;
                redisService.set(key, id, 5, TimeUnit.MINUTES);
            });
            List<TransferShipmentPushPurchaseOut> wholesaleShipmentDetailList = wholesaleShipmentDetailService.findNeedPushPurDetailList(queryPushPurIn);
            if (CollectionUtils.isEmpty(wholesaleShipmentDetailList)) {
                return;
            }
            pushPurWholesaleShipmentHandle.handlePushToPur(bizOrgCode, wholesaleShipmentDetailList);
        } catch (Exception e) {
            log.error("执行{}中转商品生成采购单任务异常：", bizOrgCode, e);
        }
    }


    private void handleDelayPushShipmentDetailToPur(String bizOrgCode) {
        try {
            QueryPushPurIn queryPushPurIn = new QueryPushPurIn();
            queryPushPurIn.setShipmentStatus(ShipmentStatusEnum.APPROVED.getCode());
            queryPushPurIn.setExecuteTime(DateUtils.format(LocalDateTime.now()));
            queryPushPurIn.setDistributionType(DistributionWaysEnum.TRANSFER.getType());
            queryPushPurIn.setBizOrgCode(bizOrgCode);
            List<Long> idList = wholesaleShipmentService.findNeedDelayPushPurWholesaleShipmentId(queryPushPurIn);
            if (CollectionUtils.isEmpty(idList)) {
                return;
            }
            idList.forEach(id -> {
                String key = SystemConstant.SHIPMENT_TRANSFER_IDS + bizOrgCode + SystemConstant.COLON + id;
                redisService.set(key, id, 5, TimeUnit.MINUTES);
            });
            List<TransferShipmentPushPurchaseOut> wholesaleShipmentDetailList = wholesaleShipmentDetailService.findNeedDelayPushPurDetailList(queryPushPurIn);
            if (CollectionUtils.isEmpty(wholesaleShipmentDetailList)) {
                return;
            }
            pushPurWholesaleShipmentHandle.handlePushToPur(bizOrgCode, wholesaleShipmentDetailList);
        } catch (Exception e) {
            log.error("执行{}中转商品生成采购单任务异常：", bizOrgCode, e);
        }
    }

}
