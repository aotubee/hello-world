package com.edc.erp.directly.distribution.job;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.model.out.store.StoreDelivery;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPool;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.directly.distribution.model.out.AfterOrderCreatedMqOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderAllocationPoolService;
import com.edc.erp.directly.enumeration.OrderAllocationPoolStatusEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Lists;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 直营订单调配定时任务
 * @author zy
 * @since 2025-1-7
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class DirOrderAllocationPoolScheduler {

    private final TaskLockUtils taskLockUtils;

    private final RedisService redisService;

    private final OrdDirOrderAllocationPoolService ordDirOrderAllocationPoolService;

    private final StoreCenterService storeCenterService;

    private final OrderHandle orderHandle;

    private static final String CLOSE_SWITCH_KEY = "OPEN";

        @Scheduled(cron = "0 0/10 4 * * ?")
//    @Scheduled(cron = "0 0/10 * * * ?")
    public void dirHandleSupplementQuantityJob() {
        //获取任务开关
        String orderSwitch = redisService.get(SystemConstant.CLOSE_DIR_ORDER_ALLOCATION_POOL_SWITCH_KEY);
        if (CLOSE_SWITCH_KEY.equals(orderSwitch)) {
            log.info("直营订货单调配定时任务开关未打开....................................");
            return;
        }
        log.info("开始执行直营订货单调配定时任务....................................");
        if (taskLockUtils.lock(SystemConstant.DIR_ORDER_ALLOCATION_POOL_SWITCH, SystemConstant.LOCK_TIME_OUT)) {
            try {
                List<String> storeList = ordDirOrderAllocationPoolService.findStoreCodeList();
                if (CollectionUtils.isEmpty(storeList)) {
                    return;
                }
                storeList.forEach(storeCode -> {
                    // 获取配送周期信息
                    List<StoreDelivery> storeDeliverLogicList = storeCenterService.getByStoreCode(storeCode);
                    if (CollectionUtils.isEmpty(storeDeliverLogicList)) {
                        throw new BusinessException("门店配送周期信息为空");
                    }
                    Map<String, StoreDelivery> deliveryTypeMap = storeDeliverLogicList.stream().collect(Collectors.toMap(StoreDelivery::getDeliveryType, Function.identity()));
                    List<OrdDirOrderAllocationPool> storePoolList = ordDirOrderAllocationPoolService.findEmptyTruncationDateTimeListByStoreCode(storeCode);
                    if (CollectionUtils.isEmpty(storePoolList)) {
                        return;
                    }
                    Map<String, Map<String, BigDecimal>> truncationTimeGoodsQuantityMap = new HashMap<>();
                    storePoolList.forEach(ordDirOrderAllocationPool -> {
                        // 计算补单量,过滤删除不复合条件的记录
                        ordDirOrderAllocationPoolService.handleSupplementQuantity(ordDirOrderAllocationPool, deliveryTypeMap, truncationTimeGoodsQuantityMap);
                    });
                });
            } catch (Exception e) {
                log.error("直营订货单调配任务异常");
            } finally {
                taskLockUtils.unlock(SystemConstant.DIR_ORDER_ALLOCATION_POOL_SWITCH);
            }
        } else {
            log.info("直营订货单调配任务定时器锁还未释放：{}", SystemConstant.DIR_ORDER_ALLOCATION_POOL_SWITCH);
        }
        log.info("直营订货单调配任务结束....................................");
    }

        @Scheduled(cron = "0 0/10 5-13 * * ?")
//    @Scheduled(cron = "0 0/10 * * * ?")
    public void dirAllocationPoolCreateOrderJob() {
        //获取任务开关
        String orderSwitch = redisService.get(SystemConstant.CLOSE_DIR_ALLOCATION_POOL_CREATE_ORDER_SWITCH_KEY);
        if (CLOSE_SWITCH_KEY.equals(orderSwitch)) {
            log.info("直营订货单调配创建订货单任务开关未打开....................................");
            return;
        }
        log.info("开始执行直营订货单调配创建订货单定时任务....................................");
        if (taskLockUtils.lock(SystemConstant.DIR_ALLOCATION_POOL_CREATE_ORDER_SWITCH, SystemConstant.LOCK_TIME_OUT)) {
            try {
                List<OrdDirOrderAllocationPool> list = ordDirOrderAllocationPoolService.findStoreTruncationDateTimeList();
                if (CollectionUtils.isEmpty(list)) {
                    return;
                }
                list.forEach(pool -> {
                    try {
                        LocalDateTime truncationDateTime = pool.getTruncationDateTime();
                        Duration duration = Duration.between(LocalDateTime.now(), truncationDateTime);
                        if (duration.toMinutes() > 60) {
                            return;
                        }
                        List<OrdDirOrderAllocationPool> storePoolList = ordDirOrderAllocationPoolService.findListByStoreCodeAndTruncationDateTime(pool.getStoreCode(), pool.getTruncationDateTime());
                        if (CollectionUtils.isEmpty(storePoolList)) {
                            return;
                        }
                        List<CreateOrderSkuIn> orderSkuInList = Lists.newArrayList();
                        List<OrdDirOrderAllocationPool> doneList = storePoolList.stream().filter(ordDirOrderAllocationPool -> OrderAllocationPoolStatusEnum.DONE.getKey().equals(ordDirOrderAllocationPool.getStatus())).collect(Collectors.toList());
                        doneList.forEach(ordDirOrderAllocationPool -> {
                            if (ordDirOrderAllocationPool.getSupplementQuantity().compareTo(BigDecimal.ZERO) == 0) {
                                ordDirOrderAllocationPool.setIsSuccess(ModelConst.DELETE.NO);
                                return;
                            }
                            ordDirOrderAllocationPool.setIsSuccess(ModelConst.DELETE.YES);
                            CreateOrderSkuIn createOrderSkuIn = new CreateOrderSkuIn();
                            createOrderSkuIn.setGoodsCode(ordDirOrderAllocationPool.getGoodsCode());
                            createOrderSkuIn.setPackageQuantity(ordDirOrderAllocationPool.getSupplementPackageQuantity());
                            orderSkuInList.add(createOrderSkuIn);
                        });
                        int isExist = orderHandle.getCountByParameter(pool.getStoreCode(), BusinessTypeColumnEnum.HEAD_OFFICE_REPLENISH.getType(), pool.getTruncationDateTime());
//                        Response<AfterOrderCreatedMqOut> response = null;
                        if (isExist == 0 && CollectionUtils.isNotEmpty(orderSkuInList)) {
                            orderHandle.createUpAndDownOrder(pool.getStoreCode(),
                                    SystemConstant.SYSTEM_USER, orderSkuInList, pool.getBizOrgCode(), BusinessTypeColumnEnum.HEAD_OFFICE_REPLENISH.getType());
                        }
//                        if (isExist > 0) {
//                            response = Response.success();
//                        }
                        // 创建订货单成功后转移到历史库
//                        if (response.isSuccess()) {
                        ordDirOrderAllocationPoolService.toHistory(storePoolList, pool.getStoreCode(), pool.getTruncationDateTime());
//                        }
                    } catch (Exception e) {
                        log.error("门店{}截单时间{}调配创建订货单异常", pool.getStoreCode(), DateUtils.format(pool.getTruncationDateTime()), e);
                    }
                });
            } catch (Exception e) {
                log.error("直营订货单调配创建订货单任务异常");
            } finally {
                taskLockUtils.unlock(SystemConstant.DIR_ALLOCATION_POOL_CREATE_ORDER_SWITCH);
            }
        } else {
            log.info("直营订货单调配创建订货单任务定时器锁还未释放：{}", SystemConstant.DIR_ALLOCATION_POOL_CREATE_ORDER_SWITCH);
        }
        log.info("直营订货单调配创建订货单任务结束....................................");
    }

}
