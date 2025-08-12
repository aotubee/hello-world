package com.edc.erp.directly.returnnoticeorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author wuke
 */
@Data
public class SaveOrderGoodsOut {
    @ApiModelProperty(name = "specification", value = "规格")
    private String specification;

    @ApiModelProperty(name = "unit", value = "单位")
    private String unit;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品代码")
    private String goodsName;

    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文")
    private String goodsTypeStr;

    @ApiModelProperty(name = "returnPrice", value = "配货价")
    private BigDecimal distributionPrice;

    @ApiModelProperty(name = "orgGoodsId", value = "组织商品id")
    private Integer orgGoodsId;

    @ApiModelProperty(name = "sort", value = "品类代码")
    private String sort;

    @ApiModelProperty(name = "sortName", value = "品类名称")
    private String sortName;

    @ApiModelProperty(name = "brandName", value = "品牌名称")
    private String brandName;

}
