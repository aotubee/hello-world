package com.edc.erp.orderscheduing.model.out;

import com.edc.erp.common.model.in.purchase.TransferNoticePurchaseIn;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 查询中转配销单发送采购入参参数 出参
 * @since 2023/2/3 15:10
 */
@Data
@Builder
public class TransferDisDeliveryOrderOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采购入参信息
     */
    private List<TransferNoticePurchaseIn> transferNoticePurchaseIns;

    /**
     * 结转周期
     */
    private String carryForwardCycle;

    /**
     * 配销单明细
     */
    private List<OrdDisDeliveryDetail> orderDisDeliveryDetails;
}
