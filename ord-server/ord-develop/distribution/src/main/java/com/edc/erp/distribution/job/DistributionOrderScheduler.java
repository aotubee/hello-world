package com.edc.erp.distribution.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.service.OrdDisOrderDistributionService;
import com.edc.erp.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 配销分货单生效定时任务
 * @author lx
 * @since 2022-11-21 19:13:23
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class DistributionOrderScheduler {

    private final OrdDisOrderDistributionService ordDisOrderDistributionService;

    private final AsyncTaskItemService asyncTaskItemService;

//    private final AsyncPushTaskService asyncPushTaskService;

    private static final String CLOSE_AUTO_ORD_DIST_ORDER_SWITCH_KEY = "OPEN";

    @Qualifier("disDistributionCreateOrderSender")
    private final MessageSender disDistributionCreateOrderSender;

    /**
     * 配销分货单自动生效定时任务
     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void distributionOrderJob(){
//        //获取任务开关
//        String orderSwitch = redisService.get(SystemConstant.CLOSE_AUTO_ORD_DIST_ORDER_SWITCH_KEY);
//        if (CLOSE_AUTO_ORD_DIST_ORDER_SWITCH_KEY.equals(orderSwitch)){
//            log.info("配销分货单自动生效定时任务开关未打开....................................");
//            return;
//        }
//        log.info("开始执行配销分货单自动生效定时任务....................................");
//        if (taskLockUtils.lock(SystemConstant.AUTO_ORD_DIST_ORDER_SWITCH,SystemConstant.LOCK_TIME_OUT)){
//            try{
//                //获取配销分货单信息
//                List<OrdDisOrderDistributionOrderOut> orderOuts = ordDisOrderDistributionService.findOrderByNewTimeAndStatus();
//                if(CollectionUtils.isEmpty(orderOuts)){
//                    log.info("无配销分货单自动生效任务....................................");
//                    return;
//                }
//
//                log.info("配销分货单自动生效定时任务的条数是：...........{}条",orderOuts.size());
//                for (OrdDisOrderDistributionOrderOut orderOut : orderOuts) {
//                    try{
//                        //生成订货单任务
//                        ordDisOrderDistributionService.disDistributionInitOrder(orderOut.getId(),orderOut.getEffectiveTime(), orderOut.getBizOrgCode(),orderOut.getCreator());
//                    }catch (Exception e){
//                        log.error("分货单:{}生效创建订货单任务异常...........=>{}",orderOut.getDistributionOrderNo(),e);
//                    }
//
//                }
//            }catch (Exception e){
//                log.error("配销分货单自动生效任务异常");
//            }finally {
//                taskLockUtils.unlock(SystemConstant.AUTO_ORD_DIST_ORDER_SWITCH);
//            }
//        }else {
//            log.info("配销分货单自动生效任务定时器锁还未释放：{}", SystemConstant.AUTO_ORD_DIST_ORDER_SWITCH);
//        }
//        log.info("配销分货单自动生效任务结束....................................");
//    }
    @XxlJob("disDistributionOrderCreateOrderJob")
    public ReturnT<String> disDistributionOrderCreateOrderJob() {
        log.info("加盟分货定时创建订货单任务----start");
        this.disOrderCreateOrderExecute();
        log.info("加盟分货定时创建订货单任务----end");
        return ReturnT.SUCCESS;
    }

    public void disOrderCreateOrderExecute() {
        //获取配销分货单信息
        List<Long> idList = ordDisOrderDistributionService.findNeedExecuteList();
        if (CollectionUtils.isEmpty(idList)) {
            log.info("无配销分货单自动生效任务....................................");
            return;
        }
        log.info("配销分货单自动生效定时任务的条数是：...........{}条", idList.size());
        for (Long id : idList) {
            OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionService.selectByPrimaryKey(id);
            try {
                if (Objects.isNull(ordDisOrderDistribution)) {
                    log.info("分货单ID{}不存在", id);
                    continue;
                }
                if (!OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(ordDisOrderDistribution.getDistributionOrderStatus())) {
                    log.info("分货单{}状态不正确", ordDisOrderDistribution.getDistributionOrderNo());
                    continue;
                }
                //生成订货单任务
                OrdDisOrderDistribution taskDistribution = new OrdDisOrderDistribution();
                taskDistribution.setId(ordDisOrderDistribution.getId());
                taskDistribution.setDistributionOrderNo(ordDisOrderDistribution.getDistributionOrderNo());
                taskDistribution.setUpdater(ordDisOrderDistribution.getUpdater());
                String taskData = JSONObject.toJSONString(taskDistribution);
//                asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DISTRIBUTION_CREATE_ORDER, taskData, ordDisOrderDistribution.getBizOrgCode(), ordDisOrderDistribution.getDistributionOrderNo());
                SendResponse sendResponse = disDistributionCreateOrderSender.sendSync(taskData.getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
                log.info("配销分货单生成订货单{}下发DTS消息ID---{}", ordDisOrderDistribution.getDistributionOrderNo(), sendResponse.getMessageId());
            } catch (Exception e) {
                log.error("分货单:{}生效创建订货单任务异常...........=>{}", ordDisOrderDistribution.getDistributionOrderNo(), e);
            }
        }
    }

//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void distributionOrderCreateOrderJob() {
//        //获取任务开关
//        String orderSwitch = redisService.get(SystemConstant.CLOSE_AUTO_ORD_DIST_ORDER_SWITCH_KEY);
//        if (CLOSE_AUTO_ORD_DIST_ORDER_SWITCH_KEY.equals(orderSwitch)) {
//            log.info("配销分货单自动生效定时任务开关未打开....................................");
//            return;
//        }
//        log.info("开始执行配销分货单自动生效定时任务....................................");
//        if (taskLockUtils.lock(SystemConstant.AUTO_ORD_DIST_ORDER_SWITCH, SystemConstant.LOCK_TIME_OUT)) {
//            try {
//                //获取配销分货单信息
//                List<Long> idList = ordDisOrderDistributionService.findNeedExecuteList();
//                if (CollectionUtils.isEmpty(idList)) {
//                    log.info("无配销分货单自动生效任务....................................");
//                    return;
//                }
//                log.info("配销分货单自动生效定时任务的条数是：...........{}条", idList.size());
//                for (Long id : idList) {
//                    OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionService.selectByPrimaryKey(id);
//                    try {
//                        if (Objects.isNull(ordDisOrderDistribution)) {
//                            log.info("分货单ID{}不存在", id);
//                            continue;
//                        }
//                        if (!OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(ordDisOrderDistribution.getDistributionOrderStatus())) {
//                            log.info("分货单{}状态不正确", ordDisOrderDistribution.getDistributionOrderNo());
//                            continue;
//                        }
//                        //生成订货单任务
//                        OrdDisOrderDistribution taskDistribution = new OrdDisOrderDistribution();
//                        taskDistribution.setId(ordDisOrderDistribution.getId());
//                        taskDistribution.setUpdater(ordDisOrderDistribution.getUpdater());
//                        String taskData = JSONObject.toJSONString(taskDistribution);
//                        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DISTRIBUTION_CREATE_ORDER, taskData, ordDisOrderDistribution.getBizOrgCode(), ordDisOrderDistribution.getDistributionOrderNo());
//                    } catch (Exception e) {
//                        log.error("分货单:{}生效创建订货单任务异常...........=>{}", ordDisOrderDistribution.getDistributionOrderNo(), e);
//                    }
//                }
//            } catch (Exception e) {
//                log.error("配销分货单自动生效任务异常");
//            } finally {
//                taskLockUtils.unlock(SystemConstant.AUTO_ORD_DIST_ORDER_SWITCH);
//            }
//        } else {
//            log.info("配销分货单自动生效任务定时器锁还未释放：{}", SystemConstant.AUTO_ORD_DIST_ORDER_SWITCH);
//        }
//        log.info("配销分货单自动生效任务结束....................................");
//    }
}

