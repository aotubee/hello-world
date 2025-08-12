package com.edc.erp.presale.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName PresaleActivityGoodsInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/29 11:32
 **/
@Data
public class PresaleActivityGoodsForAppOut implements Serializable {
    private static final long serialVersionUID = 128754905944468214L;
    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    private Long id;
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
     * 商品图片
     */
    @ApiModelProperty(name = "goodsImage", value = "商品图片")
    private String goodsImage;

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
    private Integer packageSpecificationNum;

    /**
     * 包装数
     */
    @ApiModelProperty(name = "packageQuantity", value = "包装数")
    private BigDecimal packageQuantity;

    /**
     * 是否赠品
     */
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;
    /**
     * 配销价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配销价")
    private BigDecimal distributionPrice;
}
