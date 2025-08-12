package com.edc.erp.common.model.in.purchase;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
* @return: 商品信息集合入参
* @Author: fxw
* @Date: 2022/11/23
*/
@Data
@ApiModel(value = "GoodsDtlsIn",description = "商品信息集合")
public class GoodsDtlsIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "goodsCode",name = "商品编号")
    private String goodsCode;

    @ApiModelProperty(value = "totalQty",name = "订货总数")
    private BigDecimal totalQty;

    @ApiModelProperty(value = "taxRate",name = "税率")
    private String taxRate;

    @ApiModelProperty(value = "lineNo",name = "行号")
    private Integer lineNo;
}
