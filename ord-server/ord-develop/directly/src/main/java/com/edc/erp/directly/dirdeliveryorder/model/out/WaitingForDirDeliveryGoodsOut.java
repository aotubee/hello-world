package com.edc.erp.directly.dirdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 等待收货商品
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-07-15 18:04
 */
@Data
public class WaitingForDirDeliveryGoodsOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配货单明细主键
     */
    @ApiModelProperty(name = "deliveryOrderDetailsId", value = "配货单明细主键")
    private Long deliveryOrderDetailsId;

    /**
     * 商品SKU
     */
    @ApiModelProperty(name = "goodsCode", value = "商品SKU")
    private String goodsCode;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 商品主图
     */
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;


    /**
     * 发货量
     */
    @ApiModelProperty(name = "deliveryQuantity", value = "发货量")
    private BigDecimal deliveryQuantity;


    /**
     * 发货包装数
     */
    @ApiModelProperty(name = "deliveryPackageQuantity", value = "发货包装数")
    private BigDecimal deliveryPackageQuantity;


    /**
     * 到货量
     */
    @ApiModelProperty(name = "arrivalQuantity", value = "到货量")
    private BigDecimal arrivalQuantity;

    /**
     * 零售单位
     */
    @ApiModelProperty(name = "specificationUnit", value = "零售单位")
    private String specificationUnit;

    /**
     * 配货规格数量（1*12中的12）
     */
    @ApiModelProperty(name = "distributionSpecificationNum", value = "配货规格数量（1*12中的12）")
    private BigDecimal distributionSpecificationNum;

    /**
     * 商品配货规格（1*12）
     */
    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*12）")
    private String distributionSpecification;

    /**
     * 商品配货规格单位（件/箱）
     */
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;

    /**
     * 订货量
     */
    @ApiModelProperty(name = "orderQuantity", value = "订货量")
    private BigDecimal orderQuantity;

    @ApiModelProperty(name = "isCanEdit", value = "是否能更新收货量，1:是,0:否")
    private Integer isCanEdit;

    /**
     * 扫码次数
     */
    @ApiModelProperty(name = "sweepTheCodeNumber", value = "扫码次数")
    private Integer sweepTheCodeNumber;

    /**
     * 统计类型
     */
    @ApiModelProperty(name = "statisticalType", value = "物流统计类型", example = "1:散件，2:整件,3:高值")
    private Integer statisticalType;

    /**
     * 缓存中到货量
     */
    @ApiModelProperty(name = "cacheArrivalQuantity", value = "缓存中到货量")
    private BigDecimal cacheArrivalQuantity;

    @ApiModelProperty(name = "expiry", value = "效期码")
    private String expiry;
}
