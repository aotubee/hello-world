package com.edc.erp.directly.handle;

import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.entity.DirOrderProcessCopy;
import com.edc.erp.directly.enumeration.OrderCycleProcessCodeEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigCodeEnum;
import com.edc.erp.directly.orderscheduing.handle.DirOrderProcessSchedulingHandle;
import com.edc.erp.directly.service.DirOrderProcessCopyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 要货单配置handle
 * @author fxw
 * @since 2022/10/24 12:19
 */
@Service
public class DirRequestOrderConfigHandle {

    @Autowired
    @Qualifier("dirOrderProcessCopyServiceImpl")
    private DirOrderProcessCopyService orderProcessCopyService;

    @Autowired
    private DirOrderProcessSchedulingHandle orderProcessSchedulingHandle;

    /**
     * 获取要货单合并规则
     *
     * @param orderCycleId 订货周期id
     * @param bizOrgCode   业务组织代码
     * @return
     */
    public DirOrderProcessConfigItem getDataMergingRule(Integer orderCycleId, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.REQUEST_ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.REQUEST_ORDER_CREATE_APPROACHES.getCode();
        return orderProcessCopyService.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 订单流是否终结于要货单（要货单发送ERP）
     *
     * @param orderCycleId 订货周期id
     * @param storeCode    门店代码
     * @param bizOrgCode   业务组织代码
     * @return
     */
    public boolean isEndRequestOrder(Integer orderCycleId, String storeCode, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.DELIVERY_ORDER_CREATE.getCode();
        DirOrderProcessCopy dirOrderProcessCopy = orderProcessSchedulingHandle.getOrderProcessCopyByParameter(orderCycleId, storeCode, processCode, bizOrgCode);
        return Objects.isNull(dirOrderProcessCopy);
    }
}
