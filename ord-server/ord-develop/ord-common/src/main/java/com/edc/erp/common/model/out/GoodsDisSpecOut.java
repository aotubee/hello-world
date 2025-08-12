package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author lee
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GoodsDisSpecOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    @ApiModelProperty(name = "orgGoodsBar", value = "商品主条码")
    private String orgGoodsBar;

    @ApiModelProperty(name = "imgUrl", value = "商品主图")
    private String imgUrl;

    @ApiModelProperty(value = "配货规格id")
    private Integer disSpecId;

    @ApiModelProperty(value = "配货规格名称")
    private String disSpecName;

    @ApiModelProperty(value = "配货规格数量")
    private Integer disSpecNum;

    @ApiModelProperty(value = "配货规格单位")
    private String disSpecUnit;

    @ApiModelProperty(value = "配货规格单位ID")
    private Integer unitId;

    @ApiModelProperty(value = "配货价格")
    private BigDecimal disPrice;
}
