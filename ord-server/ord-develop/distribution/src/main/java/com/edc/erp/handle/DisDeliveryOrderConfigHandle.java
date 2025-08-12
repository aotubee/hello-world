package com.edc.erp.handle;

import com.edc.erp.enumeration.OrderCycleProcessCodeEnum;
import com.edc.erp.enumeration.OrderCycleProcessConfigCodeEnum;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.orderscheduing.handle.OrderProcessSchedulingHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author fxw
 * @description: 配货单拆分配置处理类
 * @since 2022/10/25 19:09
 */
@Service
@Slf4j
public class DisDeliveryOrderConfigHandle {

    @Autowired
    private OrderProcessSchedulingHandle orderProcessSchedulingHandle;


    /**
     * 获取拆分配货单规则
     *
     * @param orderCycleId 订货周期id
     * @param bizOrgCode   组织代码
     * @return
     */
    public List<OrderProcessConfigItemOut> findSplitRule(Integer orderCycleId, String bizOrgCode) {
        //配货单拆分配货流程
        String processCode = OrderCycleProcessCodeEnum.DELIVERY_ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.DELIVERY_SPLIT_CONDITION.getCode();
        return orderProcessSchedulingHandle.findOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 配销单是否自动收货
     *
     * @param orderCycleId 订货周期id
     * @param bizOrgCode   业务组织代码
     * @return
     */
    public OrderProcessConfigItemOut getAutoTakeDeliveryRule(Integer orderCycleId, String bizOrgCode) {
        //配销单是否自动收货
        String processCode = OrderCycleProcessCodeEnum.DELIVERY_CONFIRM.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.DELIVERY_IS_AUTO_RECEIVE.getCode();
        OrderProcessConfigItemOut orderProcessConfigItemOut = orderProcessSchedulingHandle.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
        return orderProcessConfigItemOut;
    }
}
