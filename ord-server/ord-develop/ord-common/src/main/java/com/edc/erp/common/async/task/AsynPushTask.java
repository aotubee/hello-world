//package com.edc.erp.common.async.task;
//
//import cn.hutool.core.date.DateTime;
//import cn.hutool.core.date.DateUtil;
//import com.edc.erp.common.async.AsynPusher;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.constant.SystemConstant;
//import com.edc.erp.common.entity.AsyncTask;
//import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
//import com.edc.erp.common.service.AsyncTaskService;
//import com.edc.erp.common.util.SpringContextUtil;
//import com.edc.plugins.redis.lock.TaskLockUtils;
//import com.google.common.util.concurrent.MoreExecutors;
//import lombok.extern.slf4j.Slf4j;
//import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.ApplicationListener;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//import java.util.concurrent.*;
//
///**
// * @Description: 异步推送任务
// * @Author: fxw
// * @Date: 2022/11/22
// */
//@Slf4j
//@Component
//public class AsynPushTask implements ApplicationListener<ApplicationReadyEvent> {
//
//    /**
//     * 线程池数
//     */
//    private static final int THREAD_COUNT = 2;
//
//    /**
//     * 核心线程数
//     */
//    private static final int CORE_POOL_SIZE = 1;
//
//    /**
//     * 任务执行数量
//     */
//    private static final int EXECUTED_QUANTITY = 100;
//
//    @Autowired
//    private AsyncTaskService asyncTaskService;
//
//    @Autowired
//    private TaskLockUtils taskLockUtils;
//
//    @Value("#{${asyn-task.interval}}")
//    private Map<Integer, Integer> interval;
//    @Value("${asyn-task.retry-max}")
//    private Integer retryMax;
//
//    private static Map<String, AsynPusher> pusherMap = new HashMap<>();
//
//
//    public static void regPusher(String type, AsynPusher pusher) {
//        pusherMap.put(type, pusher);
//    }
//
//    /*
//      前置：监听springboot初始化完成事件
//      1、查询数据库retry次数不超过5(yaml)的，并且next_try_time <=now()
//      2、在yaml里配置map 来计算下一次重试时间   1:5，2:10,3：15 4:15
//      3、调用AsyncPusher push方法，如果没有异常把返回值更新进去，如果有异常，把异常message放到exception字段里
//     */
//
//    /**
//     * @param applicationReadyEvent
//     */
//    @Override
//    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
//        SpringContextUtil.getBeansByClass(AsynPusher.class);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asynPushTask")
//    public void execute() {
//        if (taskLockUtils.lock(SystemConstant.ASYNC_PUSH_TASK, SystemConstant.ASYNC_LOCK_TIME_OUT)) {
//            try {
////                for (int i = 0; i < SystemConstant.ASYNC_LOCK_REPEAT_TIME; i++) {
//                List<Long> taskIdList = asyncTaskService.findTaskIdList(retryMax);
//                for (Long taskId : taskIdList) {
//                    AsyncTask asyncTask = asyncTaskService.getAsyncTaskByParameter(taskId, retryMax);
//                    if (Objects.isNull(asyncTask)) {
//                        continue;
//                    }
////                    log.info("执行定时任务{}", JSONObject.toJSONString(asyncTask));
//                    AsynPusher asynPusher = pusherMap.get(asyncTask.getType());
//                    String res = null;
//                    try {
//                        res = asynPusher.push(asyncTask.getMessageJson());
//                        asyncTask.setExecStatus(AsyncTaskConstant.ExecStatus.SUCCESS);
//                        asyncTask.setReturnResult(res);
//                        asyncTask.setExceptionMessage(null);
//                        asyncTaskService.updateByPrimaryKeySelective(asyncTask);
//                    } catch (Exception e) {
//                        log.error("执行任务id{}异常", taskId, e);
//                        Integer retryNo = asyncTask.getRetryNo();
//                        if (retryNo > retryMax) {
//                            asyncTask.setExecStatus(AsyncTaskConstant.ExecStatus.FAIL);
//                        } else {
//                            asyncTask.setRetryNo(retryNo + 1);
//                            asyncTask.setExecStatus(AsyncTaskConstant.ExecStatus.ABNORMAL);
//                        }
//                        asyncTask.setExceptionMessage(e.getMessage());
//                        Integer intervalMinute = interval.get(retryNo);
//                        DateTime dateTime = DateUtil.offsetMinute(new Date(), intervalMinute);
//                        asyncTask.setNextRetryTime(dateTime);
//                        asyncTask.setReturnResult(res);
//                        asyncTaskService.updateByPrimaryKeySelective(asyncTask);
//                    }
//                }
////                }
//            } catch (Exception e) {
//                log.error("订单流转任务异常：", e);
//            } finally {
//                taskLockUtils.unlock(SystemConstant.ASYNC_PUSH_TASK);
//            }
//        } else {
//            log.info("定时器锁还未释放：{}", SystemConstant.ASYNC_PUSH_TASK);
//        }
//    }
//
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskXA")
//    public void executeXAOrg() {
//        String bizOrgCode = OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode();
//        this.handleAsyncTaskJob(bizOrgCode, null, 50);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskXADeliveryToDts")
////    public void executeXAOrgDeliveryToDts() {
////        String bizOrgCode = OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskXADeliveryDtsToErp")
////    public void executeXAOrgDeliveryDtsToErp() {
////        String bizOrgCode = OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskXARequestToDelivery")
////    public void executeXAOrgRequestToDelivery() {
////        String bizOrgCode = OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, 100);
////    }
//
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskZZDeliveryToDts")
////    public void executeZZOrgDeliveryToDts() {
////        String bizOrgCode = OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskZZDeliveryDtsToErp")
////    public void executeZZOrgDeliveryDtsToErp() {
////        String bizOrgCode = OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskZZRequestToDelivery")
////    public void executeZZOrgRequestToDelivery() {
////        String bizOrgCode = OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskTSDeliveryToDts")
////    public void executeTSOrgDeliveryToDts() {
////        String bizOrgCode = OrgCodeConvertEnum.TS_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskTSDeliveryDtsToErp")
////    public void executeTSOrgDeliveryDtsToErp() {
////        String bizOrgCode = OrgCodeConvertEnum.TS_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskTSRequestToDelivery")
////    public void executeTSOrgRequestToDelivery() {
////        String bizOrgCode = OrgCodeConvertEnum.TS_MYT.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskTS")
//    public void executeTSOrg() {
//        String bizOrgCode = OrgCodeConvertEnum.TS_MYT.getBizOrgCode();
//        this.handleAsyncTaskJob(bizOrgCode, null, 50);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskZZ")
//    public void executeZZOrg() {
//        String bizOrgCode = OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode();
//        this.handleAsyncTaskJob(bizOrgCode, null, 50);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskMPT")
//    public void executeMPTOrg() {
//        String bizOrgCode = OrgCodeConvertEnum.MEI_PIN_TANG.getBizOrgCode();
//        this.handleAsyncTaskJob(bizOrgCode, null, 50);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "asyncPushTaskMPTIceCream")
//    public void executeMPTIceCreamOrg() {
//        String bizOrgCode = OrgCodeConvertEnum.MEI_PIN_TANG_ICE_CREAM.getBizOrgCode();
//        this.handleAsyncTaskJob(bizOrgCode, null, 50);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeZZSupplyChainOrg")
//    public void executeZZSupplyChainOrg() {
//        String bizOrgCode = OrgCodeConvertEnum.ZZ_SUPPLY_CHAIN.getBizOrgCode();
//        this.handleAsyncTaskJob(bizOrgCode, null, 50);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeZZSupplyChainOrgDeliveryToDts")
////    public void executeZZSupplyChainOrgDeliveryToDts() {
////        String bizOrgCode = OrgCodeConvertEnum.ZZ_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeZZSupplyChainOrgDeliveryDtsToErp")
////    public void executeZZSupplyChainOrgDeliveryDtsToErp() {
////        String bizOrgCode = OrgCodeConvertEnum.ZZ_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeZZSupplyChainOrgRequestToDelivery")
////    public void executeZZSupplyChainOrgRequestToDelivery() {
////        String bizOrgCode = OrgCodeConvertEnum.ZZ_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, 100);
////    }
//
//
//
//    //
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeXASupplyChainOrg")
//    public void executeXASupplyChainOrg() {
//        String bizOrgCode = OrgCodeConvertEnum.XA_SUPPLY_CHAIN.getBizOrgCode();
//        this.handleAsyncTaskJob(bizOrgCode, null, 50);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeXASupplyChainOrgDeliveryToDts")
////    public void executeXASupplyChainOrgDeliveryToDts() {
////        String bizOrgCode = OrgCodeConvertEnum.XA_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeXASupplyChainOrgDeliveryDtsToErp")
////    public void executeXASupplyChainOrgDeliveryDtsToErp() {
////        String bizOrgCode = OrgCodeConvertEnum.XA_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeXASupplyChainOrgRequestToDelivery")
////    public void executeXASupplyChainOrgRequestToDelivery() {
////        String bizOrgCode = OrgCodeConvertEnum.XA_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, 100);
////    }
//
//
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeFXSupplyChainOrg")
//    public void executeFXSupplyChainOrg() {
//        String bizOrgCode = OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode();
//        this.handleAsyncTaskJob(bizOrgCode, null, 50);
//    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeFXSupplyChainOrgDeliveryToDts")
////    public void executeFXSupplyChainOrgDeliveryToDts() {
////        String bizOrgCode = OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeFXSupplyChainOrgDeliveryDtsToErp")
////    public void executeFXSupplyChainOrgDeliveryDtsToErp() {
////        String bizOrgCode = OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP, 100);
////    }
//
////    @Scheduled(cron = "0 0/1 * * * ? ")
////    @SchedulerLock(name = "executeFXSupplyChainOrgRequestToDelivery")
////    public void executeFXSupplyChainOrgRequestToDelivery() {
////        String bizOrgCode = OrgCodeConvertEnum.SX_FX_SUPPLY_CHAIN.getBizOrgCode();
////        this.handleAsyncTaskJob(bizOrgCode, AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, 100);
////    }
//
//
//
//    private void handleAsyncTaskJob(String bizOrgCode, String type, Integer limitNum) {
//        String lockKey;
//        if (StringUtils.isBlank(type)) {
//            lockKey = SystemConstant.ASYNC_PUSH_TASK_ORG + SystemConstant.COLON + bizOrgCode;
//        } else {
//            lockKey = SystemConstant.ASYNC_PUSH_TASK_ORG + SystemConstant.COLON + bizOrgCode + SystemConstant.COLON + type;
//        }
//        if (taskLockUtils.lock(lockKey, SystemConstant.ASYNC_LOCK_TIME_OUT)) {
//            log.info("-----------{}开始执行任务-------------", lockKey);
//            try {
////                for (int i = 0; i < SystemConstant.ASYNC_LOCK_REPEAT_TIME; i++) {
//                List<Long> taskIdList = asyncTaskService.findTaskIdListByBizOrgCode(retryMax, type, bizOrgCode, limitNum);
//                if (StringUtils.isBlank(type)) {
//                    this.executedTaskJob(taskIdList, bizOrgCode, lockKey);
//                } else {
//                    this.handleThreadTask(taskIdList, bizOrgCode, lockKey);
//                }
//                log.info("-----------{}任务执行结束-------------", lockKey);
//            } catch (Exception e) {
//                log.error("{}订单流转{}任务异常：", OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode), type, e);
//            } finally {
//                taskLockUtils.unlock(lockKey);
//            }
//        } else {
//            log.info("定时器锁还未释放：{}----{}", OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode), lockKey);
//        }
//    }
//
//    /**
//     * @Description: 多线程处理任务
//     * @Author: ZhangYao
//     * @Date: 2023/9/15 17:00
//     * @param taskIdList:
//     * @param bizOrgCode:
//     * @param lockKey:
//     * @return: void
//     **/
//    public void handleThreadTask(List<Long> taskIdList, String bizOrgCode, String lockKey) {
//        //设置线程池
//        ExecutorService threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, THREAD_COUNT, 5L,
//                TimeUnit.SECONDS, new LinkedBlockingDeque<>(30), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
//        taskIdList.forEach(id -> {
//            threadPool.submit(new Runnable() {
//                @Override
//                public void run() {
//                    log.info("多线程---当前线程{}待执处理的任务有{}条数据........................", lockKey, taskIdList.size());
//                    executedTaskJob(Collections.singletonList(id), bizOrgCode, lockKey);
//                }
//            });
//        });
//        //关闭线程池
//        MoreExecutors.shutdownAndAwaitTermination(threadPool, 10, TimeUnit.MINUTES);
//    }
//
//    /**
//     * @Description: 处理任务
//     * @Author: ZhangYao
//     * @Date: 2023/9/15 17:00
//     * @param taskIdList:
//     * @param bizOrgCode:
//     * @param lockKey:
//     * @return: void
//     **/
//    public void executedTaskJob(List<Long> taskIdList, String bizOrgCode, String lockKey) {
//        log.info("当前线程{}待执处理的任务有{}条数据", lockKey, taskIdList.size());
//        for (Long taskId : taskIdList) {
//            AsyncTask asyncTask = asyncTaskService.getAsyncTaskByParameter(taskId, retryMax);
//            if (Objects.isNull(asyncTask)) {
//                continue;
//            }
////                    log.info("{}执行定时任务{}", OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode), JSONObject.toJSONString(asyncTask));
//            AsynPusher asynPusher = pusherMap.get(asyncTask.getType());
//            String res = null;
//            try {
//                res = asynPusher.push(asyncTask.getMessageJson());
//                asyncTask.setExecStatus(AsyncTaskConstant.ExecStatus.SUCCESS);
//                asyncTask.setReturnResult(res);
//                asyncTask.setExceptionMessage(null);
//                asyncTaskService.updateByPrimaryKeySelective(asyncTask);
//            } catch (Exception e) {
//                log.error("{}执行任务id{}异常", OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode), taskId, e);
//                Integer retryNo = asyncTask.getRetryNo();
//                if (retryNo > retryMax) {
//                    asyncTask.setExecStatus(AsyncTaskConstant.ExecStatus.FAIL);
//                } else {
//                    asyncTask.setRetryNo(retryNo + 1);
//                    asyncTask.setExecStatus(AsyncTaskConstant.ExecStatus.ABNORMAL);
//                }
//                asyncTask.setExceptionMessage(e.getMessage());
//                Integer intervalMinute = interval.get(retryNo);
//                DateTime dateTime = DateUtil.offsetMinute(new Date(), intervalMinute);
//                asyncTask.setNextRetryTime(dateTime);
//                asyncTask.setReturnResult(res);
//                asyncTaskService.updateByPrimaryKeySelective(asyncTask);
//            }
//        }
//    }
//
//}
