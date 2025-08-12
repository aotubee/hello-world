package com.edc.erp.disdeliveryorder.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryMapper;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.enumeration.FirstOrderFreezeEnum;
import com.edc.erp.disfirstorder.mapper.OrdDisOrderFirstMapper;
import com.edc.erp.disrequestorder.mapper.OrdDisDelivRequestMapper;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.mapper.OrdDisOrderMapper;
import com.edc.erp.enumeration.DeliveryOrderFreezeEnum;
import com.edc.erp.enumeration.OrderFreezeEnum;
import com.edc.erp.enumeration.OrderLogEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * @ClassName DisDeliveryInvalidUnFreezeHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/7/21 17:54
 **/
@Component
@Slf4j
@RequiredArgsConstructor
public class DisDeliveryInvalidUnFreezeHandle {

    private final OrdDisDelivRequestMapper ordDisDelivRequestMapper;

    private final OrdDisDeliveryMapper ordDisDeliveryMapper;

    private final OrdDisOrderMapper ordDisOrderMapper;

    private final OrdDisOrderFirstMapper ordDisOrderFirstMapper;

    private final FundServer fundServer;

    public List<BusinessLog> deliveryFreezeForBusinessOrder(Long deliveryOrderId, String loginUsername) {
        OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(deliveryOrderId);
        List<String> unFrozenBusinessNos = Lists.newArrayList();
        List<BusinessLog> businessLogList = Lists.newArrayList();
        // 如果是冻结,表示配销单为已审核状态，一定执行过释放上游单据+冻结配销单（定时器 解冻原单+冻结新单逻辑），且需要释放本配销单;如果配销单是非冻结，表示配销单未待审核/已预审，不用理会本单释放，只考虑上游单据
        if (DeliveryOrderFreezeEnum.FREEZE.getKey().equals(ordDisDelivery.getFreezeStatus())) {
            unFrozenBusinessNos.add(ordDisDelivery.getDeliveryOrderNo());
            String content = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_INVALID_UNFROZEN.getKey(), ordDisDelivery.getDeliveryOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrderId),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), loginUsername);
            businessLogList.add(businessLog);
        }
        // 订单流逻辑 ，完全交给 释放上游单据+冻结配销单（定时器 解冻原单+冻结新单逻辑）
//        if (DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDisDelivery.getSourceCode())) {
//            // 查找周期下是否还有其他非作废状态的配销单
//            Long orderCycleId = ordDisDelivRequestMapper.findOrderCycleIdByDeliveryOrderId(deliveryOrderId, ordDisDelivery.getBizOrgCode());
//            Integer count = ordDisDeliveryMapper.countNoInvalidByCycleIdAndNonDeliveryOrderId(orderCycleId, deliveryOrderId, ordDisDelivery.getBizOrgCode());
//            if (count == 0) {
//                // 如果本周期下没有其他非作废配销单，则证明本周期下订货业务结束，释放周期下所有  “已冻结”  的订货单金额
//                List<OrdDisOrder> needUnfreezeOrderList = ordDisOrderMapper.findNeedUnfreezeOrderNo(orderCycleId);
//                if (CollectionUtils.isNotEmpty(needUnfreezeOrderList)) {
//                    needUnfreezeOrderList.forEach(order -> {
//                        unFrozenBusinessNos.add(order.getOrderNo());
//                        String content = MessageFormat.format(OrderLogEnum.ORDER_UNFROZEN_FOR_DELIVERY_INVALID.getKey(), order.getOrderNo());
//                        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
//                                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), loginUsername);
//                        businessLogList.add(businessLog);
//                    });
//                }
//            }
//        }
        // 首单铺货逻辑，完全交给 释放上游单据+冻结配销单（定时器 解冻原单+冻结新单逻辑）
//        if (DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDisDelivery.getSourceCode())) {
//            OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstMapper.getOrdDisFirstByDeliveryOrderId(deliveryOrderId);
//            Integer count = ordDisDeliveryMapper.countNoInvalidByFirstOrderAndNonDeliveryOrderId(ordDisOrderFirst.getId(), deliveryOrderId, ordDisDelivery.getBizOrgCode());
//            if (count == 0 && FirstOrderFreezeEnum.FREEZE.getKey().equals(ordDisOrderFirst.getFreezeStatus())) {
//                // 如果本周期下没有其他非作废配销单，则证明本周期下订货业务结束，释放铺货单
//                unFrozenBusinessNos.add(ordDisOrderFirst.getFirstOrderNo());
//                String content = MessageFormat.format(OrderLogEnum.FIRST_ORDER_UNFROZEN_FOR_DELIVERY_INVALID.getKey(), ordDisOrderFirst.getFirstOrderNo());
//                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
//                        String.valueOf(ordDisOrderFirst.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), content, new Date(), loginUsername);
//                businessLogList.add(businessLog);
//            }
//        }
        if (CollectionUtils.isNotEmpty(unFrozenBusinessNos)) {
            UnFrozenIn unFrozenIn = new UnFrozenIn();
            unFrozenIn.setUnFrozenBusinessNos(unFrozenBusinessNos);
            Response response = fundServer.unFrozen(unFrozenIn);
            if (!response.isSuccess()) {
                throw new BusinessException("配销单作废释放金额异常：" + response.getMessage());
            }
        }
        return businessLogList;
    }
}
