package com.edc.erp.directly.handle;

import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.enumeration.OrderCycleProcessCodeEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigCodeEnum;
import com.edc.erp.directly.orderscheduing.handle.DirOrderProcessSchedulingHandle;
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
public class DirDeliveryOrderConfigHandle {

    @Autowired
    private DirOrderProcessSchedulingHandle orderProcessSchedulingHandle;


    /**
     * 获取拆分配货单规则
     *
     * @param orderCycleId 订货周期id
     * @param storeCode   门店代码
     * @param bizOrgCode   组织代码
     * @return
     */
    public List<DirOrderProcessConfigItem> findSplitRule(Integer orderCycleId, String storeCode, String bizOrgCode) {
        //配货单拆分配货流程
        String processCode = OrderCycleProcessCodeEnum.DELIVERY_ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.DELIVERY_SPLIT_CONDITION.getCode();
        return orderProcessSchedulingHandle.findOrderProcessConfigItemOut(orderCycleId, storeCode,bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 配销单是否自动收货
     *
     * @param orderCycleId 订货周期id
     * @param bizOrgCode   业务组织代码
     * @return
     */
    public DirOrderProcessConfigItem getAutoTakeDeliveryRule(Integer orderCycleId, String bizOrgCode) {
        //配销单是否自动收货
        String processCode = OrderCycleProcessCodeEnum.DELIVERY_CONFIRM.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.DELIVERY_IS_AUTO_RECEIVE.getCode();
        DirOrderProcessConfigItem dirOrderProcessConfigItem = orderProcessSchedulingHandle.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
        return dirOrderProcessConfigItem;
    }
}
