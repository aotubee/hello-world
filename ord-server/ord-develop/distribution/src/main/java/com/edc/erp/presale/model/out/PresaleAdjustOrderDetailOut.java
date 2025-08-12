package com.edc.erp.presale.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class PresaleAdjustOrderDetailOut implements Serializable {
    private static final long serialVersionUID = 5999270486888276860L;
    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 调整数量
     */
    @ApiModelProperty(name = "adjustQty", value = "调整数量")
    private BigDecimal adjustQty;

    /**
     * 当前预售数量
     */
    @ApiModelProperty(name = "beforeQty", value = "当前预售数量")
    private BigDecimal beforeQty;

    /**
     * 品类属性
     */
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    /**
     * 包装单位
     */
    @ApiModelProperty(name = "packageUnit", value = "包装单位")
    private String packageUnit;

    /**
     * 包装规格
     */
    @ApiModelProperty(name = "packageSpecification", value = "包装规格")
    private String packageSpecification;

    /**
     * 包装规格数
     */
    @ApiModelProperty(name = "packageSpecificationNum", value = "包装规格数")
    private BigDecimal packageSpecificationNum;

    /**
     * 预售活动号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
    private String presaleActivityNo;
}
