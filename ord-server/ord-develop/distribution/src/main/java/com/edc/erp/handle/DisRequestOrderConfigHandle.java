package com.edc.erp.handle;

import com.edc.erp.common.entity.OrderProcessCopy;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.enumeration.OrderCycleProcessCodeEnum;
import com.edc.erp.enumeration.OrderCycleProcessConfigCodeEnum;
import com.edc.erp.orderscheduing.handle.OrderProcessSchedulingHandle;
import com.edc.erp.service.DisOrderProcessCopyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author fxw
 * @description: 集货单配置handle
 * @since 2022/10/24 12:19
 */
@Service
public class DisRequestOrderConfigHandle {

    @Autowired
    @Qualifier("disOrderProcessCopyServiceImpl")
    private DisOrderProcessCopyService orderProcessCopyService;

    @Autowired
    private OrderProcessSchedulingHandle orderProcessSchedulingHandle;

    /**
     * 获取集货单合并规则
     *
     * @param orderCycleId 订货周期id
     * @param bizOrgCode   业务组织代码
     * @return
     */
    public OrderProcessConfigItemOut getDataMergingRule(Integer orderCycleId, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.REQUEST_ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.REQUEST_ORDER_CREATE_APPROACHES.getCode();
        return orderProcessCopyService.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 订单流是否终结于集货单（集货单发送ERP）
     *
     * @param orderCycleId 订货周期id
     * @param storeCode    门店代码
     * @param bizOrgCode   业务组织代码
     * @return
     */
    public boolean isEndRequestOrder(Integer orderCycleId, String storeCode, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.DELIVERY_ORDER_CREATE.getCode();
        OrderProcessCopy orderProcessCopy = orderProcessSchedulingHandle.getOrderProcessCopyByParameter(orderCycleId, storeCode, processCode, bizOrgCode);
        return Objects.isNull(orderProcessCopy);
    }
}
