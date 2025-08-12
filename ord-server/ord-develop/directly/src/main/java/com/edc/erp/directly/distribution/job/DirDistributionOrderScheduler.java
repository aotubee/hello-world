package com.edc.erp.directly.distribution.job;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionService;
import com.edc.erp.directly.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 直营分货单生效定时任务
 * @author lx
 * @since 2022-11-21 19:13:23
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class DirDistributionOrderScheduler {

    private final OrdDirOrderDistributionService ordDirOrderDistributionService;


    private final DirAsyncTaskItemService dirAsyncTaskItemService;

//    private final AsyncPushTaskService asyncPushTaskService;

    private static final String CLOSE_AUTO_ORD_DIRT_ORDER_SWITCH_KEY = "OPEN";

    @Qualifier("dirDistributionCreateOrderSender")
    private final MessageSender dirDistributionCreateOrderSender;

    /**
     * 直营分货单自动生效定时任务
     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void distributionOrderJob(){
//        //获取任务开关
//        String orderSwitch = redisService.get(SystemConstant.CLOSE_AUTO_ORD_DIRT_ORDER_SWITCH_KEY);
//        if (CLOSE_AUTO_ORD_DIRT_ORDER_SWITCH_KEY.equals(orderSwitch)){
//            log.info("直营分货单自动生效定时任务开关未打开....................................");
//            return;
//        }
//        log.info("开始执行直营分货单自动生效定时任务....................................");
//        if (taskLockUtils.lock(SystemConstant.AUTO_ORD_DIRT_ORDER_SWITCH,SystemConstant.LOCK_TIME_OUT)){
//            try{
//                //获取直营分货单信息
//                List<OrdDirOrderDistributionOrderOut> orderOuts = ordDirOrderDistributionService.findOrderByNewTimeAndStatus();
//                if(CollectionUtils.isEmpty(orderOuts)){
//                    log.info("无直营分货单自动生效任务....................................");
//                    return;
//                }
//
//                log.info("直营分货单自动生效定时任务的条数是：...........{}条",orderOuts.size());
//                for (OrdDirOrderDistributionOrderOut orderOut : orderOuts) {
//                    try{
//                        //生成订货单任务
//                        ordDirOrderDistributionService.dirDistributionInitOrder(orderOut.getId(),orderOut.getEffectiveTime(), orderOut.getBizOrgCode(),orderOut.getCreator());
//                    }catch (Exception e){
//                        log.error("分货单:{}生效创建订货单任务异常...........=>{}",orderOut.getDistributionOrderNo(),e);
//                    }
//
//                }
//            }catch (Exception e){
//                log.error("直营分货单自动生效任务异常");
//            }finally {
//                taskLockUtils.unlock(SystemConstant.AUTO_ORD_DIRT_ORDER_SWITCH);
//            }
//        }else {
//            log.info("直营分货单自动生效任务定时器锁还未释放：{}", SystemConstant.AUTO_ORD_DIRT_ORDER_SWITCH);
//        }
//        log.info("直营分货单自动生效任务结束....................................");
//    }
    @XxlJob("dirDistributionOrderCreateOrderJob")
    public ReturnT<String> dirDistributionOrderCreateOrderJob() {
        log.info("直营分货定时创建订货单任务----start");
        this.disOrderCreateOrderExecute();
        log.info("直营分货定时创建订货单任务----end");
        return ReturnT.SUCCESS;
    }

    public void disOrderCreateOrderExecute() {
        //获取直营分货单信息
        List<Long> idList = ordDirOrderDistributionService.findNeedExecuteList();
        if (CollectionUtils.isEmpty(idList)) {
            log.info("无直营分货单自动生效任务....................................");
            return;
        }

        log.info("直营分货单自动生效定时任务的条数是：...........{}条", idList.size());
        for (Long id : idList) {
            OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistributionService.selectByPrimaryKey(id);
            try {
                if (Objects.isNull(ordDirOrderDistribution)) {
                    log.info("分货单ID{}不存在", id);
                    continue;
                }
                if (!OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(ordDirOrderDistribution.getDistributionOrderStatus())) {
                    log.info("分货单{}状态不正确", ordDirOrderDistribution.getDistributionOrderNo());
                    continue;
                }
                //生成订货单任务
                OrdDirOrderDistribution taskDistribution = new OrdDirOrderDistribution();
                taskDistribution.setId(ordDirOrderDistribution.getId());
                taskDistribution.setUpdater(ordDirOrderDistribution.getUpdater());
                taskDistribution.setDistributionOrderNo(ordDirOrderDistribution.getDistributionOrderNo());
                String taskData = JSONObject.toJSONString(taskDistribution);
//                        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_DISTRIBUTION_CREATE_ORDER, taskData, ordDirOrderDistribution.getBizOrgCode(), ordDirOrderDistribution.getDistributionOrderNo());
                SendResponse sendResponse = dirDistributionCreateOrderSender.sendSync(taskData.getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_TIME);
                log.info("直营分货单生成订货单{}下发DTS消息ID---{}", ordDirOrderDistribution.getDistributionOrderNo(), sendResponse.getMessageId());
            } catch (Exception e) {
                log.error("分货单:{}生效创建订货单任务异常...........=>{}", ordDirOrderDistribution.getDistributionOrderNo(), e);
            }
        }
    }
}


//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void distributionOrderCreateOrderJob() {
//        //获取任务开关
//        String orderSwitch = redisService.get(SystemConstant.CLOSE_AUTO_ORD_DIRT_ORDER_SWITCH_KEY);
//        if (CLOSE_AUTO_ORD_DIRT_ORDER_SWITCH_KEY.equals(orderSwitch)) {
//            log.info("直营分货单自动生效定时任务开关未打开....................................");
//            return;
//        }
//        log.info("开始执行直营分货单自动生效定时任务....................................");
//        if (taskLockUtils.lock(SystemConstant.AUTO_ORD_DIRT_ORDER_SWITCH, SystemConstant.LOCK_TIME_OUT)) {
//            try {
//                //获取直营分货单信息
//                List<Long> idList = ordDirOrderDistributionService.findNeedExecuteList();
//                if (CollectionUtils.isEmpty(idList)) {
//                    log.info("无直营分货单自动生效任务....................................");
//                    return;
//                }
//
//                log.info("直营分货单自动生效定时任务的条数是：...........{}条", idList.size());
//                for (Long id : idList) {
//                    OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistributionService.selectByPrimaryKey(id);
//                    try {
//                        if (Objects.isNull(ordDirOrderDistribution)) {
//                            log.info("分货单ID{}不存在", id);
//                            continue;
//                        }
//                        if (!OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(ordDirOrderDistribution.getDistributionOrderStatus())) {
//                            log.info("分货单{}状态不正确", ordDirOrderDistribution.getDistributionOrderNo());
//                            continue;
//                        }
//                        //生成订货单任务
//                        OrdDirOrderDistribution taskDistribution = new OrdDirOrderDistribution();
//                        taskDistribution.setId(ordDirOrderDistribution.getId());
//                        taskDistribution.setUpdater(ordDirOrderDistribution.getUpdater());
//                        String taskData = JSONObject.toJSONString(taskDistribution);
//                        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_DISTRIBUTION_CREATE_ORDER, taskData, ordDirOrderDistribution.getBizOrgCode(), ordDirOrderDistribution.getDistributionOrderNo());
//                    } catch (Exception e) {
//                        log.error("分货单:{}生效创建订货单任务异常...........=>{}", ordDirOrderDistribution.getDistributionOrderNo(), e);
//                    }
//                }
//            } catch (Exception e) {
//                log.error("直营分货单自动生效任务异常");
//            } finally {
//                taskLockUtils.unlock(SystemConstant.AUTO_ORD_DIRT_ORDER_SWITCH);
//            }
//        } else {
//            log.info("直营分货单自动生效任务定时器锁还未释放：{}", SystemConstant.AUTO_ORD_DIRT_ORDER_SWITCH);
//        }
//        log.info("直营分货单自动生效任务结束....................................");
//    }
