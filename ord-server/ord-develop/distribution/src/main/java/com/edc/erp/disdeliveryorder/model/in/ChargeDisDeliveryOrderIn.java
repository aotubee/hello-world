package com.edc.erp.disdeliveryorder.model.in;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 冲销配销单入参
 *
 * @author weichao
 */
@Data
public class ChargeDisDeliveryOrderIn {

    private static final long serialVersionUID = -1;

    /** 配销单主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配销单主键")
    @NotNull
    private Long deliveryOrderId;
    /**
     * 配销单明细列表
     */
    @ApiModelProperty(value = "配销单明细列表")
    private List<OrdDisDeliveryDetail> detailList;

    private String bizOrgCode;
}
