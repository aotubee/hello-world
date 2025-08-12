package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: 购物车基础商品（新）
 * @author fxw
 * @since 2022-10-17
 */
@Data
public class BaseOrderCartGoodsOut {

    @ApiModelProperty(name = "goodsCode", value = "商品code")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;

    @ApiModelProperty(name = "packageQuantity", value = "订货包装数量")
    private BigDecimal packageQuantity;

    @ApiModelProperty(name = "quantity", value = "订货数量")
    private BigDecimal quantity;

    @ApiModelProperty(name = "payUnitPrice", value = "商品支付单价/1*1")
    private BigDecimal payUnitPrice;

    @ApiModelProperty(name = "paySpecificationsPrice", value = "商品支付单价")
    private BigDecimal paySpecificationsPrice;

    @ApiModelProperty(name = "activityUnitPrice", value = "活动单价/1*1")
    private BigDecimal activityUnitPrice;

    @ApiModelProperty(name = "activitySpecificationsPrice", value = "活动单价")
    private BigDecimal activitySpecificationsPrice;

    @ApiModelProperty(name = "originalUnitPrice", value = "商品配货单价（原价）/1*1")
    private BigDecimal originalUnitPrice;

    @ApiModelProperty(name = "originalSpecificationsPrice", value = "商品配货单价（原价）")
    private BigDecimal originalSpecificationsPrice;


    @ApiModelProperty(name = "specificationUnit", value = "规格单位（瓶/包）")
    private String specificationUnit;

    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*12）")
    private String distributionSpecification;

    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;

    @ApiModelProperty(name = "distributionSpecificationNum", value = "商品配货规格数量")
    private BigDecimal distributionSpecificationNum;

    @ApiModelProperty(name = "position", value = "仓位")
    private String position;

    @ApiModelProperty(name = "suggestedRetailPrice", value = "商品零售价")
    private BigDecimal suggestedRetailPrice;

    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    @ApiModelProperty(name = "brand", value = "商品品牌")
    private String brand;

    @ApiModelProperty(name = "tagName", value = "商品标签")
    private String tagName;

    @ApiModelProperty(name = "isActivity", value = "是否参加活动")
    private Integer isActivity;

    @ApiModelProperty(name = "activityId", value = "活动主键")
    private Integer activityId;

    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;

    @ApiModelProperty(name = "baseOrderDetailsId", value = "赠品基础商品明细主键")
    private Integer baseOrderDetailId;

    @ApiModelProperty(name = "allowDistributionReturn", value = "是否可退")
    private String allowDistributionReturn;

    @ApiModelProperty(name = "isShelves", value = "是否下架")
    private Integer isShelves;

    @ApiModelProperty(name = "distributionType", value = "配货方式")
    private String distributionType;

    @ApiModelProperty(name = "isCanBuyFlashSale", value = "是否可以加购")
    private Integer isCanBuyFlashSale;

    @ApiModelProperty(name = "smallSort", value = "小分类代码")
    private String smallSort;

    /**
     * 商品属性
     */
    @ApiModelProperty(name = "goodsType", value = "商品属性")
    private String goodsType;
}