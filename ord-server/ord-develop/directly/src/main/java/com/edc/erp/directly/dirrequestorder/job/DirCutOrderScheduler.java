//package com.edc.erp.directly.dirrequestorder.job;
//
//
//import com.edc.erp.common.constant.SystemConstant;
//import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
//import com.edc.erp.common.enumeration.StoreConstant;
//import com.edc.erp.directly.dirrequestorder.handle.DirCutOrderHandle;
//import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
//import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
//import com.edc.erp.directly.enumeration.OrderStatusEnum;
//import com.edc.plugins.common.exception.BusinessException;
//import com.edc.plugins.redis.RedisService;
//import com.edc.plugins.redis.lock.TaskLockUtils;
//import com.google.common.collect.Lists;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// * 截单定时器
// */
//@Component
//@Slf4j
//public class DirCutOrderScheduler {
//
//    @Autowired
//    RedisService redisService;
//
//    @Autowired
//    TaskLockUtils taskLockUtils;
//
//    /**
//     * 关闭截单定时器的值
//     */
//    private final static String CLOSE_CUT_ORDER_KEY = "OPEN";
//
//    @Autowired
//    private DirCutOrderHandle dirCutOrderHandle;
//
//    @Autowired
//    private DirOrderCycleHandle dirOrderCycleHandle;
//
////    @Autowired
////    private StoreCenterService storeCenterService;
//
//    /**
//     * 西安直营截单定时器
//     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void operatorSIADirectlyCutOrder() {
//        try {
//            TimeUnit.MINUTES.sleep(1);
//        } catch (InterruptedException e) {
//            throw new BusinessException("截单定时器挂起异常");
//        }
//        String closeCutOrderSwitch = redisService.get(SystemConstant.DIR_SIA_DIRECTLY_CLOSE_CUT_ORDER_SWITCH_KEY);
//        String bizOrgCode = OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode();
//        if (CLOSE_CUT_ORDER_KEY.equals(closeCutOrderSwitch)) {
//            log.info("{}直营截单定时器开关未打开......", bizOrgCode);
//            return;
//        }
////        boolean flag = InterceptorUtil.checkSystemMaintenance(redisService.get(SystemConstant.SYSTEM_MAINTENANCE));
////        if (!flag) {
////            return;
////        }
//        this.optDirectlyCutOrderCycle(bizOrgCode);
//        log.info("直营{}截单任务执行结束", bizOrgCode);
//    }
//
//
//    /**
//     * 郑州直营截单定时器
//     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void operatorCGODirectlyCutOrder() {
//        try {
//            TimeUnit.MINUTES.sleep(1);
//        } catch (InterruptedException e) {
//            throw new BusinessException("截单定时器挂起异常");
//        }
//        String closeCutOrderSwitch = redisService.get(SystemConstant.DIR_CGO_DIRECTLY_CLOSE_CUT_ORDER_SWITCH_KEY);
//        String bizOrgCode = OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode();
//        if (CLOSE_CUT_ORDER_KEY.equals(closeCutOrderSwitch)) {
//            log.info("{}直营截单定时器开关未打开......", bizOrgCode);
//            return;
//        }
////        boolean flag = InterceptorUtil.checkSystemMaintenance(redisService.get(SystemConstant.SYSTEM_MAINTENANCE));
////        if (!flag) {
////            return;
////        }
//        this.optDirectlyCutOrderCycle(bizOrgCode);
//        log.info("直营{}截单任务执行结束", bizOrgCode);
//    }
//
//    /**
//     * 郑州直营截单定时器
//     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void operatorFXDirectlyCutOrder() {
//        try {
//            TimeUnit.MINUTES.sleep(1);
//        } catch (InterruptedException e) {
//            throw new BusinessException("截单定时器挂起异常");
//        }
//        String closeCutOrderSwitch = redisService.get(SystemConstant.DIR_FX_DIRECTLY_CLOSE_CUT_ORDER_SWITCH_KEY);
//        String bizOrgCode = OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode();
//        if (CLOSE_CUT_ORDER_KEY.equals(closeCutOrderSwitch)) {
//            log.info("{}直营截单定时器开关未打开......", bizOrgCode);
//            return;
//        }
////        boolean flag = InterceptorUtil.checkSystemMaintenance(redisService.get(SystemConstant.SYSTEM_MAINTENANCE));
////        if (!flag) {
////            return;
////        }
//        this.optDirectlyCutOrderCycle(bizOrgCode);
//        log.info("直营{}截单任务执行结束", bizOrgCode);
//    }
//
//    private void optDirectlyCutOrderCycle(String bizOrgCode) {
//        String storeProperty = StoreConstant.StoreProperty.DIRECTLY.getMytValue();
//        String lockKey = SystemConstant.DIR_CUT_ORDER_SWITCH + SystemConstant.COLON + bizOrgCode + SystemConstant.COLON + storeProperty;
//        if (taskLockUtils.lock(lockKey, 60 * 5 * 1000)) {
//            try {
//                log.info("开始执行业务组织{}直营截单任务", bizOrgCode);
//                // 获取截单时间在当天0点0分~当前时间的订货周期
//                String beginTime = LocalDate.now() + " 00:00:00";
//                String endTime = LocalDate.now() + " 23:59:59";
//                List<String> orderStatusCodeList = Lists.newArrayList(OrderStatusEnum.SUBMIT.getKey());
//                List<OrdDirOrderCycle> orderCycleList = dirOrderCycleHandle.findOrderCycleListBetweenCreateTime(beginTime, endTime, bizOrgCode, orderStatusCodeList);
//                if (CollectionUtils.isEmpty(orderCycleList)) {
//                    log.info("直营---业务组织{}没查到可截单的订货周期", bizOrgCode);
//                    return;
//                }
//                orderCycleList = orderCycleList.stream().filter(orderCycle -> orderCycle.getTruncationDateTime().isBefore(LocalDateTime.now())).collect(Collectors.toList());
////                List<String> storeCodeList = orderCycleList.stream().map(OrdDirOrderCycle::getStoreCode).collect(Collectors.toList());
////                // 获取直营属性的门店数据
////                List<StoreInfo> storeInfoList = storeCenterService.findStoreInfoByProperty(storeCodeList, storeProperty, bizOrgCode);
////                if (CollectionUtils.isEmpty(storeInfoList)) {
////                    log.info("直营---业务组织{}没查到可用门店", bizOrgCode);
////                    return;
////                }
////                Map<String, StoreInfo> storeMap = storeInfoList.stream().collect(Collectors.toMap(StoreInfo::getErpStoreCode, Function.identity()));
////                // 过滤直营门店订货周期
////                List<OrdDirOrderCycle> cutOrderCycleList = orderCycleList.stream().filter(orderCycle -> storeMap.containsKey(orderCycle.getStoreCode())).collect(Collectors.toList());
//                // 处理截单
//                dirCutOrderHandle.handleCutOrderJob(orderCycleList);
//            } catch (Exception e) {
//                log.error("执行直营业务组织{}截单任务异常：", bizOrgCode, e);
//            } finally {
//                taskLockUtils.unlock(lockKey);
//                log.info("业务组织{}直营截单任务执行完毕，释放截单定时器定时器锁：{}", bizOrgCode, lockKey);
//            }
//        } else {
//            log.info("直营{}截单定时器定时器锁还未释放：{}", bizOrgCode, lockKey);
//        }
//    }
//
//}
