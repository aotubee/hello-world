package com.edc.erp.common.model.out.purchase;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
* @return: 商品信息集合入参
* @Author: fxw
* @Date: 2022/11/23
*/
@Data
@ApiModel(value = "GoodsDtlsIn",description = "商品信息集合")
public class GoodsDtlsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品编号
     */
    @ApiModelProperty(value = "goodsCode",name = "商品编号")
    @NotEmpty(message = "商品code不能为空")
    private String goodsCode;

    /**
     * 订货总数
     */
    @ApiModelProperty(value = "totalQty",name = "订货总数")
    @NotNull(message = "订货总数不能为空")
    private BigDecimal totalQty;

    @ApiModelProperty(value = "validityCode",name = "效期码")
    private String validityCode;
}
