package com.edc.erp.distribution.model.out;

import com.edc.erp.distribution.entity.OrdDisOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fxw
 * @description: 配销订货单列表查询出参
 * @since 2022/10/17 16:09
 */
@Data
public class DisOrderOut extends OrdDisOrder implements Serializable {

    @ApiModelProperty(name = "orderStatusCodeStr", value = "订货单状态中文")
    private String orderStatusCodeStr;

    @ApiModelProperty(name = "sourceCodeStr", value = "订货类型中文")
    private String sourceCodeStr;

    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private LocalDateTime truncationDateTime;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "storeAreaStr", value = "门店区域中文")
    private String storeAreaStr;

    @ApiModelProperty(name = "orderTypeCode", value = "订单类型编码")
    private String orderTypeCode;

    @ApiModelProperty(name = "orderTypeName", value = "订单类型名称")
    private String orderTypeName;

    @ApiModelProperty(name = "requestOrderNo", value = "集货单号")
    private String requestOrderNo;

    @ApiModelProperty(name = "distributionOrderNo", value = "分货单号")
    private String distributionOrderNo;

    @ApiModelProperty(name = "orderIdentificationStr", value = "订货标识")
    private String orderIdentificationStr;
}
