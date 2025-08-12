package com.edc.erp.directly.handle;

import com.edc.erp.common.enumeration.SourceTypeEnum;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.enumeration.OrderCycleProcessCodeEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigCodeEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigItemCodeEnum;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
import com.edc.erp.directly.service.DirGoodsCombinationService;
import com.edc.erp.directly.service.DirOrderProcessCopyService;
import com.edc.erp.directly.service.impl.DirOrderTypeConfigServiceImpl;
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
public class DirOrderConfigHandle extends DirOrderTypeConfigServiceImpl {

    @Qualifier("dirOrderProcessCopyServiceImpl")
    @Autowired
    private DirOrderProcessCopyService dirOrderProcessCopyService;

    @Autowired
    private DirGoodsCombinationService dirGoodsCombinationService;

    /**
     * 获取转单时机
     */
    public DirOrderProcessConfigItem getRequestOrderCreateOpportunity(Integer orderCycleId, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.REQUEST_ORDER_CREATE_OPPORTUNITY.getCode();
        return dirOrderProcessCopyService.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 根据商品组合条件和组织代码查询订单类型配置信息
     *
     * @param storeCode
     * @param bizOrgCode
     * @param combinationTypeValueList
     * @return
     */
    public DirOrderTypeConfig getOneByCombinationTypeValueList(String storeCode, String bizOrgCode, List<String> combinationTypeValueList) {
        Integer orderTypeConfigId = dirGoodsCombinationService.getOrderTypeConfigIdByCombinationTypeValues(storeCode, bizOrgCode, combinationTypeValueList);
        if (null == orderTypeConfigId) {
            return null;
        }
        return this.getOrderTypeConfigByIdAndBizOrgCode(orderTypeConfigId, bizOrgCode);
    }

    /**
     * 获取允许的下单来源
     *
     * @param id
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    public List<DirOrderProcessConfigItem> findAllowOrderSource(Integer id, String storeCode, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.ORDER_SOURCE_CODE.getCode();
        return dirOrderProcessCopyService.findOrderProcessConfigItemOut(id, storeCode, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 是否匹配促销活动
     *
     * @param orderCycleId
     * @param bizOrgCode
     * @return
     */
    public boolean isMatchActivity(Integer orderCycleId, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.CHECK_ACTIVITY.getCode();
        DirOrderProcessConfigItem dirOrderProcessConfigItem = dirOrderProcessCopyService.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
        if (Objects.isNull(dirOrderProcessConfigItem)) {
            return false;
        }
        if (OrderCycleProcessConfigItemCodeEnum.NO_CHECK_ACTIVITY.getCode().equals(dirOrderProcessConfigItem.getItemCode())) {
            return false;
        }
        if (OrderCycleProcessConfigItemCodeEnum.CHECK_ACTIVITY.getCode().equals(dirOrderProcessConfigItem.getItemCode())) {
            return true;
        }
        return false;
    }

    /**
     * 获取起订额规则
     *
     * @param orderCycleId
     * @param bizOrgCode
     * @return
     */
    public DirOrderProcessConfigItem minAmountCheckType(Integer orderCycleId, String bizOrgCode) {
        String processCode = OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String orderProcessConfigCode = OrderCycleProcessConfigCodeEnum.MIN_AMOUNT.getCode();
        return dirOrderProcessCopyService.getOrderProcessConfigItemOut(orderCycleId, bizOrgCode, processCode, orderProcessConfigCode);
    }

    /**
     * 是否能编辑订货单
     *
     * @param order
     * @return
     */
    public boolean isCanEditOrder(OrdDirOrder order) {
        if (SourceTypeEnum.DISTRIBUTION.getKey().equals(order.getSourceCode())) {
            return false;
        }
        boolean isCanEditFlag = false;
        // 直营无付款业务
        // 无付款，并且订货单状态为 已保存 或 已提交 可编辑
        if ((OrderStatusEnum.SUBMIT.getKey().equals(order.getOrderStatusCode()))) {
            isCanEditFlag = true;
        }
        if (isCanEditFlag) {
            // 订单类型中校验促销的订货单不可编辑
            boolean isMatchActivityFlag = this.isMatchActivity(order.getOrderCycleId(), order.getBizOrgCode());
            return !isMatchActivityFlag;
        }
        return isCanEditFlag;
    }
}
