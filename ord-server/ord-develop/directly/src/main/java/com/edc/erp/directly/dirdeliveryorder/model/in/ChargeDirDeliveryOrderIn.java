package com.edc.erp.directly.dirdeliveryorder.model.in;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 冲销配配货单入参
 *
 * @author weichao
 */
@Data
public class ChargeDirDeliveryOrderIn {

    /** 配货单主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键")
    @NotNull
    private Long deliveryOrderId;
    /**
     * 配货单明细列表
     */
    @ApiModelProperty(value = "直营配货单明细列表")
    private List<OrdDirDeliveryDetail> detailList;

//    private String name;

    private String bizOrgCode;
}
