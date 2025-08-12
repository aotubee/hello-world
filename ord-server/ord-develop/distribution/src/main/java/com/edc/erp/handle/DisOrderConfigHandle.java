package com.edc.erp.handle;

import com.edc.erp.common.entity.OrderProcessCopy;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.service.GoodsCombinationService;
import com.edc.erp.common.service.impl.OrderTypeConfigServiceImpl;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.enumeration.*;
import com.edc.erp.service.DisOrderProcessCopyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author fxw
 * @description: 订货单配置handle
 * @since 2022/10/18 20:18
 */
@Service
@Slf4j
public class DisOrderConfigHandle extends OrderTypeConfigServiceImpl {

    @Qualifier("disOrderProcessCopyServiceImpl")
    @Autowired
    private DisOrderProcessCopyService orderProcessCopyService;

    @Autowired
    private GoodsCombinationService goodsCombinationService;

    /**
     * 获取转单时机
     */
    public OrderProcessConfigItemOut getRequestOrderCreateOpportunity(Integer orderCycleId, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.REQUEST_ORDER_CREATE_OPPORTUNITY.getCode();
        return orderProcessCopyService.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 根据商品组合条件和组织代码查询订单类型配置信息
     *
     * @param storeCode
     * @param bizOrgCode
     * @param combinationTypeValueList
     * @return
     */
    public OrderTypeConfig getOneByCombinationTypeValueList(String storeCode, String bizOrgCode, List<String> combinationTypeValueList) {
        Integer orderTypeConfigId = goodsCombinationService.getOrderTypeConfigIdByCombinationTypeValues(storeCode, bizOrgCode, combinationTypeValueList);
        if (null == orderTypeConfigId) {
            return null;
        }
        return this.getOrderTypeConfigByIdAndBizOrgCode(orderTypeConfigId, bizOrgCode);
    }

    /**
     * 获取允许的下单来源
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    public List<OrderProcessConfigItemOut> findAllowOrderSource(Integer id, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.ORDER_SOURCE_CODE.getCode();
        return orderProcessCopyService.findOrderProcessConfigItemOut(id, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 获取起订额规则
     *
     * @param orderCycleId
     * @param bizOrgCode
     * @return
     */
    public OrderProcessConfigItemOut minAmountCheckType(Integer orderCycleId, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.MIN_AMOUNT.getCode();
        return orderProcessCopyService.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 是否匹配促销活动
     * @param orderCycleId
     * @param bizOrgCode
     * @return
     */
    public boolean isMatchActivity(Integer orderCycleId, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.CHECK_ACTIVITY.getCode();
        OrderProcessConfigItemOut orderProcessConfigItemOut = orderProcessCopyService.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
        if(Objects.isNull(orderProcessConfigItemOut)){
            return false;
        }
        if (OrderCycleProcessConfigItemCodeEnum.NO_CHECK_ACTIVITY.getCode().equals(orderProcessConfigItemOut.getItemCode())) {
            return false;
        }
        if (OrderCycleProcessConfigItemCodeEnum.CHECK_ACTIVITY.getCode().equals(orderProcessConfigItemOut.getItemCode())) {
            return true;
        }
        return false;
    }

    /**
     * 是否有支付流程
     * @param orderCycleId
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    public boolean isHavePayProcess(Integer orderCycleId, String storeCode, String bizOrgCode) {
        OrderProcessCopy orderProcessCopy = orderProcessCopyService.getOrderProcessCopyByParameter(orderCycleId,storeCode,OrderCycleProcessCodeEnum.ORDER_PAY.getCode(),bizOrgCode);
        if (Objects.isNull(orderProcessCopy)){
            return false;
        }
        return true;
    }

    /**
     * 是否能编辑订货单
     *
     * @param order
     * @return
     */
    public boolean isCanEditOrder(OrdDisOrder order) {
        if (SourceTypeEnum.DISTRIBUTION.getKey().equals(order.getSourceCode())) {
            return false;
        }
        boolean isCanEditFlag = false;
        boolean isHavePayFlag = this.isHavePayProcess(order.getOrderCycleId(), order.getStoreCode(), order.getOrgCode());
        // 有付款，并且订货单状态为 已保存 或 待付款 可编辑
        if (isHavePayFlag && (OrderStatusEnum.WAIT_PAYMENT.getKey().equals(order.getOrderStatusCode()))) {
            isCanEditFlag = true;
        }
//        // 无付款，并且订货单状态为 已保存 或 已提交 可编辑
//        if (!isHavePayFlag && (OrderStatusEnum..getKey().equals(order.getOrderStatusCode()) || OrderStatusEnum.SUBMIT.getKey().equals(order.getOrderStatusCode()))) {
//            isCanEditFlag = true;
//        }
        if (isCanEditFlag) {
            // 订单类型中校验促销的订货单不可编辑
            boolean isMatchActivityFlag = this.isMatchActivity(order.getOrderCycleId(), order.getBizOrgCode());
            return !isMatchActivityFlag;
        }
        return isCanEditFlag;
    }

}
