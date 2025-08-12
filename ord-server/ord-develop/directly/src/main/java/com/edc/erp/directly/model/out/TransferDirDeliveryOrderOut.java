package com.edc.erp.directly.model.out;

import com.edc.erp.common.model.in.purchase.TransferNoticePurchaseIn;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author LZQ
 * @date 2023年01月12日 10:09
 * 查询中转发送采购入参参数 出参
 */
@Data
@Builder
public class TransferDirDeliveryOrderOut implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 采购入参信息
     */
    private List<TransferNoticePurchaseIn> transferNoticePurchaseIns;

    /**
     * 截止时间存入redis的key
     */
    private String truncationDateTimeKey;

    /**
     * 截止时间
     */
    private List<String> truncationDateTime;

    /**
     * 结转周期
     */
    private String carryForwardCycle;

    /**
     * 配货单明细
     */
    private List<OrdDirDeliveryDetail> orderDirDeliveryDetails;
}
