package com.edc.erp.disrequestorder.job;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.disrequestorder.handle.DisCutOrderHandle;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.enumeration.OrderStatusEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.google.common.collect.Lists;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 截单定时器
 */
@Component
@Slf4j
public class DisCutOrderXXLJob {

    @Autowired
    RedisService redisService;

    @Autowired
    TaskLockUtils taskLockUtils;

    /**
     * 关闭截单定时器的值
     */
    private final static String CLOSE_CUT_ORDER_KEY = "OPEN";

    @Autowired
    private DisCutOrderHandle disCutOrderHandle;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

//    @Autowired
//    private StoreCenterService storeCenterService;

    /**
     * 天岁加盟截单定时器
     */
    @XxlJob("operatorTSFranchiseCutOrderJob")
    public ReturnT<String> operatorTSFranchiseCutOrderJob() {
        String bizOrgCode = XxlJobHelper.getJobParam();
        log.info("配销{}截单任务执行结束", bizOrgCode);
        this.optFranchiseCutOrderCycle(bizOrgCode);
        log.info("配销{}截单任务执行结束", bizOrgCode);
        return ReturnT.SUCCESS;
    }

    /**
     * 西安加盟截单定时器
     */
    @XxlJob("operatorSIAFranchiseCutOrderJob")
    public ReturnT<String> operatorSIAFranchiseCutOrderJob() {
        String bizOrgCode = XxlJobHelper.getJobParam();
        log.info("配销{}截单任务执行结束", bizOrgCode);
        this.optFranchiseCutOrderCycle(bizOrgCode);
        log.info("配销{}截单任务执行结束", bizOrgCode);
        return ReturnT.SUCCESS;
    }

    /**
     * 郑州加盟截单定时器
     */
    @XxlJob("operatorCGOFranchiseCutOrderJob")
    public ReturnT<String> operatorCGOFranchiseCutOrderJob() {
        String bizOrgCode = XxlJobHelper.getJobParam();
        log.info("配销{}截单任务执行结束", bizOrgCode);
        this.optFranchiseCutOrderCycle(bizOrgCode);
        log.info("配销{}截单任务执行结束", bizOrgCode);
        return ReturnT.SUCCESS;
    }







    private void optFranchiseCutOrderCycle(String bizOrgCode) {
//        String storeProperty = StoreConstant.StoreProperty.FRANCHISE.getMytValue();
        try {
            log.info("开始执行业务组织{}加盟截单任务", bizOrgCode);
            // 获取截单时间在当天0点0分~当前时间的订货周期
            String beginTime = LocalDate.now() + " 00:00:00";
            String endTime = LocalDate.now() + " 23:59:59";
            List<String> orderStatusCodeList = Lists.newArrayList(OrderStatusEnum.PAID.getKey(), OrderStatusEnum.WAIT_PAYMENT.getKey());
            List<OrdDisOrderCycle> orderCycleList = disOrderCycleHandle.findOrderCycleListBetweenCreateTime(beginTime, endTime, bizOrgCode, orderStatusCodeList);
            if (CollectionUtils.isEmpty(orderCycleList)) {
                log.info("加盟---业务组织{}没查到可截单的订货周期", bizOrgCode);
                return;
            }
            orderCycleList = orderCycleList.stream().filter(orderCycle -> orderCycle.getTruncationDateTime().isBefore(LocalDateTime.now())).collect(Collectors.toList());
//                List<String> storeCodeList = orderCycleList.stream().map(OrdDisOrderCycle::getStoreCode).collect(Collectors.toList());
//                // 获取加盟属性的门店数据
//                List<StoreInfo> storeInfoList = storeCenterService.findStoreInfoByProperty(storeCodeList, storeProperty, bizOrgCode);
//                if (CollectionUtils.isEmpty(storeInfoList)) {
//                    log.info("加盟---业务组织{}没查到可用门店", bizOrgCode);
//                    return;
//                }
//                Map<String, StoreInfo> storeMap = storeInfoList.stream().collect(Collectors.toMap(StoreInfo::getErpStoreCode, Function.identity()));
//                // 过滤加盟门店订货周期
//                List<OrdDisOrderCycle> cutOrderCycleList = orderCycleList.stream().filter(orderCycle -> storeMap.containsKey(orderCycle.getStoreCode())).collect(Collectors.toList());
            // 处理截单
            disCutOrderHandle.handleCutOrderJob(orderCycleList);
        } catch (Exception e) {
            log.error("执行加盟业务组织{}截单任务异常：", bizOrgCode, e);
        }

    }

}
