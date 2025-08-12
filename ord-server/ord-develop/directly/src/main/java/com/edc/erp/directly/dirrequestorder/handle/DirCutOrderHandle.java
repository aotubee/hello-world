package com.edc.erp.directly.dirrequestorder.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.SalvageAuditTypeEnum;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.model.in.DirOrderQueryIn;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
import com.edc.erp.directly.handle.DirOrderConfigHandle;
import com.edc.erp.directly.handle.DirRequestOrderHandle;
import com.edc.plugins.common.exception.BusinessException;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Service
@Slf4j
public class DirCutOrderHandle {

    @Autowired
    private DirRequestOrderHandle dirRequestOrderHandle;

    @Autowired
    private DirOrderConfigHandle orderConfigHandle;

    @Autowired
    private OrderHandle orderHandle;

    /**
     * 线程池数
     */
    private static final int THREAD_COUNT = 10;

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 4;

    /**
     * 任务执行数量
     */
    private static final int EXECUTED_QUANTITY = 100;

    /**
     * 处理截单任务
     *
     * @param orderCycleList
     */
    public void handleCutOrderJob(List<OrdDirOrderCycle> orderCycleList) {
        if (CollectionUtils.isEmpty(orderCycleList)) {
            return;
        }
        if (orderCycleList.size() < EXECUTED_QUANTITY) {
            this.executedCutOrdJob(orderCycleList);
        } else {
            // 计算每个线程处理多少条数据
            int threadDataSize = orderCycleList.size() / CORE_POOL_SIZE;
            // 判断是否是最后一个线程
//            boolean breakFlag = orderCycleList.size() % CORE_POOL_SIZE == 0;
            //设置线程池
            ExecutorService threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, THREAD_COUNT, 5L,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
            List<OrdDirOrderCycle> needHandleOrderCycleList;
            for (int i = 0; i < CORE_POOL_SIZE; i++) {
                if (i == CORE_POOL_SIZE - 1) {
//                    if (breakFlag) {
//                        break;
//                    }
                    needHandleOrderCycleList = orderCycleList.subList(i * threadDataSize, orderCycleList.size());
                } else {
                    needHandleOrderCycleList = orderCycleList.subList(i * threadDataSize, threadDataSize * (i + 1));
                }
                // 开启线程
                List<OrdDirOrderCycle> finalNeedHandleOrderCycleList = needHandleOrderCycleList;
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        log.info("当前线程待执处理的直营截单有{}条数据........................", finalNeedHandleOrderCycleList.size());
                        executedCutOrdJob(finalNeedHandleOrderCycleList);
                    }
                });
            }
            //关闭线程池
            MoreExecutors.shutdownAndAwaitTermination(threadPool, 2, TimeUnit.HOURS);

        }
    }

    /**
     * 执行截单job
     *
     * @param orderCycleList
     */
    private void executedCutOrdJob(List<OrdDirOrderCycle> orderCycleList) {
        orderCycleList.forEach(orderCycle -> {
//            if (LocalDateTime.now().isBefore(orderCycle.getTruncationDateTime())) {
//                log.info("门店" + orderCycle.getStoreCode() + "订货周期" + orderCycle.getTruncationDateTime() +
//                        orderCycle.getShortOrderType() + "截单时间未到");
//                return;
//            }
            log.info("开始截单处理门店{}订货周期id{}----{}", orderCycle.getStoreCode(), orderCycle.getId(), orderCycle.getTruncationDateTime());
            try {
                this.cutOrderCycle(orderCycle);
            } catch (Exception e) {
                log.error("直营门店{}{}订货周期{}截单异常", orderCycle.getStoreCode(), orderCycle.getShortOrderType(), orderCycle.getTruncationDateTime(), e);
            }
        });
    }

    /**
     * 截断指定订货周期
     *
     * @param orderCycle 订货周期
     */
    public void cutOrderCycle(OrdDirOrderCycle orderCycle) {
        // 获取订货周期起订额校验规则
        DirOrderProcessConfigItem minAmountItem = orderConfigHandle.minAmountCheckType(orderCycle.getId(), orderCycle.getBizOrgCode());
        if (Objects.isNull(minAmountItem)) {
            throw new BusinessException("截单时起订额校验规则查询为空");
        }
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.SUBMIT.getKey());
        DirOrderQueryIn dirOrderQueryIn = new DirOrderQueryIn();
        dirOrderQueryIn.setStoreCode(orderCycle.getStoreCode());
        dirOrderQueryIn.setOrderCycleId(orderCycle.getId());
        dirOrderQueryIn.setBizOrgCode(orderCycle.getBizOrgCode());
        dirOrderQueryIn.setOrderStatusCodeList(orderStatusCodeList);
        List<OrdDirOrder> orderList = orderHandle.findNeedCutOrderListByParameter(dirOrderQueryIn);
        if (CollectionUtils.isEmpty(orderList)) {
            log.info("门店" + orderCycle.getStoreCode() + "订货周期" + orderCycle.getTruncationDateTime() +
                    orderCycle.getShortOrderType() + "没有可处理的订货单");
            return;
        }
        // 春节截单特殊处理 begin
//        OrderTypeConfig orderTypeConfig = orderConfigHandle.getOrderTypeConfigByIdAndOrgCode(orderCycle.getOrderTypeConfigId(), orderCycle.getOrgCode());
//        boolean isCanCutOrderFlag = this.isCanCutOrderForSpringFestival(new SpringFestivalNoCutOrderIn(orderCycle.getTruncationDateTime(), orderTypeConfig.getOrderTypeCode(), orderCycle.getOrgCode()));
//        if (!isCanCutOrderFlag) {
//            orderList.forEach(order -> {
//                try {
//                    orderHandle.invalidOrder(order, SystemConstant.SYSTEM_NAME, orderCycle.getOrgCode());
//                    String content = MessageFormat.format(OrderLogEnum.SPRING_FESTIVAL_INVALID_ORDER.getKey(), order.getOrderNo());
//                    BusinessLog businessLog = new BusinessLog(com.edc.pp.order.constant.SystemConstant.SYSTEM_CODE, BusinessLogTypeEnum.ORDER.getCode(), order.getOrderNo(),
//                            OrderLogEnum.SPRING_FESTIVAL_INVALID_ORDER.getGlobalType(), content, new Date(), SystemConstant.SYSTEM_NAME);
//                    asyncLogService.sendAsyncSaveLogByMq(businessLog);
//                } catch (Exception e) {
//                    log.error("春节作废订单{}异常", order.getOrderNo(), e);
//                }
//            });
//            return;
//        }
//        // 春节截单特殊处理 end
        dirRequestOrderHandle.mergeDirOrder(orderCycle, minAmountItem.getItemCode(),
//                orderList, SystemConstant.SYSTEM_NAME, true, false, SalvageAuditTypeEnum.WAIT_AUTO_AUDIT.getCode());
                orderList, SystemConstant.SYSTEM_NAME, true, false, SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode());
    }

    //    /**
//     * 定时器调用-查找需要截单的订货周期
//     */
//    public void findNeedCutOrderCycle(String bizOrgCode, String storeProperty) {
//        String beginTime = LocalDate.now() + " 00:00:00";
//        List<OrdDirOrderCycle> orderCycleList = dirOrderCycleHandle.findOrderCycleListBetweenCreateTime(beginTime, storeProperty);
//        orderCycleList.forEach(orderCycle -> {
//            try {
//                this.cutOrderCycle(orderCycle);
//            } catch (Exception e) {
//                log.error("门店{}{}订货周期{}截单异常", orderCycle.getStoreCode(), orderCycle.getShortOrderType(), orderCycle.getTruncationDateTime(), e);
//            }
//        });
//    }

}
