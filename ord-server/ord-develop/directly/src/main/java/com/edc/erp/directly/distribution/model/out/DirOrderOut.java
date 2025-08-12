package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 直营订货单列表查询出参
 *
 * @author wanglidong
 * @since 2022/11/15 16:43
 */
@Data
@ApiModel(value = "DirOrderOut", description = "直营订货单列表查询出参")
public class DirOrderOut extends OrdDirOrder implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @ApiModelProperty(name = "requestOrderNo", value = "要货单单号")
    private String requestOrderNo;

    @ApiModelProperty(name = "distributionOrderNo", value = "分货单单号")
    private String distributionOrderNo;
}