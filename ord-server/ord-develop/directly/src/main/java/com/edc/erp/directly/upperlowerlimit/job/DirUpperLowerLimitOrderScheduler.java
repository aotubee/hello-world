package com.edc.erp.directly.upperlowerlimit.job;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.service.DssOrderInfoService;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.directly.upperlowerlimit.server.DirUpperLowerLimitServer;
import com.edc.erp.directly.upperlowerlimit.server.DssDirUpperLowerLimitServer;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 上下限跑货定时器
 *
 * @author weichao
 */
@Slf4j
@Component
public class DirUpperLowerLimitOrderScheduler {

    @Autowired
    private TaskLockUtils taskLockUtils;
    @Autowired
    private DirUpperLowerLimitServer upperLowerLimitServer;
    @Autowired
    private StoreCenterService storeCenterService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private DssOrderInfoService dssOrderInfoService;
    @Autowired
    private DssDirUpperLowerLimitServer dssDirUpperLowerLimitServer;

    /**
     * 线程池数
     */
    private static final int THREAD_COUNT = 10;

    @XxlJob("executeDirReplenishmentJob")
    public ReturnT<String> executeDirReplenishmentJob() {
        String jobParam = XxlJobHelper.getJobParam();
        String[] bizOrgCodeArray = jobParam.split(SystemConstant.COMMA);
        log.info("开始执行直营上下限跑货任务");
        this.asyncDirExecute(bizOrgCodeArray);
        return ReturnT.SUCCESS;
    }

    @Async
    public void asyncDirExecute(String[] bizOrgCodeArray) {
        String storeProperty = StoreConstant.StoreProperty.DIRECTLY.getMytValue();
        for (String bizOrgCode : bizOrgCodeArray) {
            log.info("正在执行直营---业务组织{}跑货任务", bizOrgCode);
            try {
                List<String> storeCodeList = storeCenterService.findIsAutoReplenishmentStoreCodeList(bizOrgCode, storeProperty);
                if (CollectionUtils.isEmpty(storeCodeList)) {
                    log.info("直营---业务组织{}没有可跑货门店", bizOrgCode);
                    continue;
                }
                // 执行跑货
                upperLowerLimitServer.replenishmentOrderJob(storeCodeList, bizOrgCode);
                log.info("直营{}上下限跑货任务执行结束", bizOrgCode);
            } catch (Exception e) {
                log.error("直营---业务组织{}执行上下限跑货任务异常：", bizOrgCode, e);
            }
        }
        log.info("直营上下限跑货任务执行结束");
    }

//    /**
//     * 上下限跑货定时器
//     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void replenishmentOrderDirectlyJob() {
//        log.info("开始执行直营上下限跑货任务");
//        String storeProperty = StoreConstant.StoreProperty.DIRECTLY.getMytValue();
//        for (String bizOrgCode : SystemConstant.BIZORGCODES) {
//            String switchKey = SystemConstant.CLOSE_AUTO_DIR_ORDER_SWITCH_KEY + SystemConstant.SHORT_LINE + bizOrgCode;
//            String orderSwitch = redisService.get(switchKey);
//            if (SystemConstant.CLOSE_AUTO_ORDER_SWITCH_IS_OPEN.equals(orderSwitch)) {
//                log.info("{}组织任务开关未打开......", bizOrgCode);
//                continue;
//            }
//            String lockKey = SystemConstant.DIR_REPLENISHMENT_ORDER_SWITCH + bizOrgCode + storeProperty;
//            if (taskLockUtils.lock(lockKey, SystemConstant.UL_LOCK_TIME_OUT)) {
//                log.info("正在执行直营---业务组织{}跑货任务", bizOrgCode);
//                try {
//                    List<String> storeCodeList = storeCenterService.findIsAutoReplenishmentStoreCodeList(bizOrgCode, storeProperty);
//                    if (CollectionUtils.isEmpty(storeCodeList)) {
//                        log.info("直营---业务组织{}没有可跑货门店", bizOrgCode);
//                        continue;
//                    }
//                    // 执行跑货
//                    upperLowerLimitServer.replenishmentOrderJob(storeCodeList, bizOrgCode);
//                } catch (Exception e) {
//                    log.error("直营---业务组织{}执行上下限跑货任务异常：", bizOrgCode, e);
//                }
////                finally {
////                    taskLockUtils.unlock(lockKey);
////                    log.info("直营---业务组织{}跑货任务执行完毕，释放锁{}", bizOrgCode, lockKey);
////                }
//            } else {
//                log.info("直营---业务组织{}定时器锁{}还未释放：{}", bizOrgCode, lockKey);
//            }
//        }
//        log.info("直营上下限跑货任务执行结束");
//    }

//    @Scheduled(cron = "0 0 4 * * ?")
////    @Scheduled(cron = "0 0/5 * * * ?")
//    public void dssReplenishmentOrderDirectlyJob() {
//        log.info("开始执行直营点三三订单上下限跑货任务");
//        String storeProperty = StoreConstant.StoreProperty.DIRECTLY.getMytValue();
//        for (String bizOrgCode : SystemConstant.BIZORGCODES) {
//            String switchKey = SystemConstant.CLOSE_AUTO_DIR_DSS_ORDER_SWITCH_KEY + SystemConstant.SHORT_LINE + bizOrgCode;
//            String orderSwitch = redisService.get(switchKey);
//            if (SystemConstant.CLOSE_AUTO_ORDER_SWITCH_IS_OPEN.equals(orderSwitch)) {
//                log.info("{}组织直营点三三订单上下限跑货任务开关未打开......", bizOrgCode);
//                continue;
//            }
//            String lockKey = SystemConstant.REPLENISHMENT_DIR_DSS_ORDER_SWITCH + bizOrgCode + storeProperty;
////            taskLockUtils.unlock(lockKey);
//            if (taskLockUtils.lock(lockKey, SystemConstant.UL_LOCK_TIME_OUT)) {
//                log.info("正在执行直营---业务组织{}加盟点三三订单上下限跑货", bizOrgCode);
//                try {
//                    Map<String, List<DssOrderInfoOut>> storeDssOrderGoodsMap = dssOrderInfoService.handleDssReplenishment(bizOrgCode, storeProperty);
//                    if (null == storeDssOrderGoodsMap || storeDssOrderGoodsMap.size() == 0) {
//                        continue;
//                    }
//                    // 执行跑货
//                    dssDirUpperLowerLimitServer.dssReplenishmentOrderJob(storeDssOrderGoodsMap, bizOrgCode);
//                } catch (Exception e) {
//                    log.error("直营---业务组织{}执行加盟点三三订单上下限跑货异常：", bizOrgCode, e);
//                }
////                finally {
////                    taskLockUtils.unlock(lockKey);
////                    log.info("直营---业务组织{}跑货任务执行完毕，释放锁{}", bizOrgCode, lockKey);
////                }
//            } else {
//                log.info("直营---业务组织{}直营点三三订单上下限跑货定时器锁{}还未释放：{}", bizOrgCode, lockKey);
//            }
//        }
//        log.info("直营点三三订单上下限跑货执行结束");
//    }
}
