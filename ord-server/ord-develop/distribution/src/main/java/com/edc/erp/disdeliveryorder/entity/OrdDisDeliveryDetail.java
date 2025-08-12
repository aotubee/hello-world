package com.edc.erp.disdeliveryorder.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * 配销单详情表(OrdDisDeliveryDetail)实体类
 *
 * @author weichao
 * @since 2022-10-10 19:48:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_delivery_detail")
@ApiModel(value = "OrdDisDeliveryDetail", description = "配销单详情表")
public class OrdDisDeliveryDetail implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配销单主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配销单主键")
    private Long deliveryOrderId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品条形码 */
    @ApiModelProperty(name = "barCode", value = "商品条形码")
    private String barCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 商品主图 */
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;

    /** 小分类 */
    @ApiModelProperty(name = "smallSort", value = "小分类")
    private String smallSort;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;

    /** 组织商品id */
    @ApiModelProperty(name = "orgGoodsId", value = "组织商品id")
    private Integer orgGoodsId;

    /** 集货数量 */
    @ApiModelProperty(name = "orderQuantity", value = "集货数量")
    private BigDecimal orderQuantity;

    /** 集货包装数 */
    @ApiModelProperty(name = "orderPackageQuantity", value = "集货包装数")
    private BigDecimal orderPackageQuantity;

    /** 集货单价 */
    @ApiModelProperty(name = "orderUnitPrice", value = "集货单价")
    private BigDecimal orderUnitPrice;

    /** 集货金额 */
    @ApiModelProperty(name = "orderAmount", value = "集货金额")
    private BigDecimal orderAmount;

    /** 配销数量 */
    @ApiModelProperty(name = "distributionQuantity", value = "配销数量")
    private BigDecimal distributionQuantity;

    /** 配销包装数 */
    @ApiModelProperty(name = "distributionPackageQuantity", value = "配销包装数")
    private BigDecimal distributionPackageQuantity;

    /** 配销单价 */
    @ApiModelProperty(name = "distributionUnitPrice", value = "配销单价")
    private BigDecimal distributionUnitPrice;

    /** 配销金额 */
    @ApiModelProperty(name = "distributionAmount", value = "配销金额")
    private BigDecimal distributionAmount;

    /** 实配数量 */
    @ApiModelProperty(name = "deliveryQuantity", value = "实配数量")
    private BigDecimal deliveryQuantity;

    /** 实配包装数 */
    @ApiModelProperty(name = "deliveryPackageQuantity", value = "实配包装数")
    private BigDecimal deliveryPackageQuantity;

    /** 实配金额 */
    @ApiModelProperty(name = "deliveryAmount", value = "实配金额")
    private BigDecimal deliveryAmount;

    /** 实收数量 */
    @ApiModelProperty(name = "arrivalQuantity", value = "实收数量")
    private BigDecimal arrivalQuantity;

    /** 实收包装数量 */
    @ApiModelProperty(name = "arrivalPackageQuantity", value = "实收包装数量")
    private BigDecimal arrivalPackageQuantity;

    /** 实收金额 */
    @ApiModelProperty(name = "arrivalAmount", value = "实收金额")
    private BigDecimal arrivalAmount;

    /** 配货规格数量（1*12中的12） */
    @ApiModelProperty(name = "distributionSpecificationNum", value = "配货规格数量（1*12中的12）")
    private BigDecimal distributionSpecificationNum;

    /** 商品配货规格（1*12） */
    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*12）")
    private String distributionSpecification;

    /** 商品配货规格单位（件/箱） */
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;

    /** 是否赠品 */
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;

    /** 品类属性 */
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    /** 配销去税金额 */
    @ApiModelProperty(name = "distributionExceptTaxAmount", value = "配销去税金额")
    private BigDecimal distributionExceptTaxAmount;

    /** 配销税额 */
    @ApiModelProperty(name = "distributionTaxAmount", value = "配销税额")
    private BigDecimal distributionTaxAmount;

    /** 仓储库存价 */
    @ApiModelProperty(name = "wrhPrice", value = "仓储库存价")
    private BigDecimal wrhPrice;

    /** 仓储成本金额 */
    @ApiModelProperty(name = "wrhCostAmount", value = "仓储成本金额")
    private BigDecimal wrhCostAmount;

    /** 仓储成本去税金额 */
    @ApiModelProperty(name = "wrhExceptTaxAmount", value = "仓储成本去税金额")
    private BigDecimal wrhExceptTaxAmount;

    /** 仓储成本税额 */
    @ApiModelProperty(name = "wrhTaxAmount", value = "仓储成本税额")
    private BigDecimal wrhTaxAmount;

    /** 门店库存价 */
    @ApiModelProperty(name = "storeStockPrice", value = "门店库存价")
    private BigDecimal storeStockPrice;

    /** 门店成本金额 */
    @ApiModelProperty(name = "storeCostAmount", value = "门店成本金额")
    private BigDecimal storeCostAmount;

    /** 门店成本去税金额 */
    @ApiModelProperty(name = "storeExceptTaxAmount", value = "门店成本去税金额")
    private BigDecimal storeExceptTaxAmount;

    /** 门店成本税额 */
    @ApiModelProperty(name = "storeTaxAmount", value = "门店成本税额")
    private BigDecimal storeTaxAmount;

    /** 税率 */
    @ApiModelProperty(name = "sellTax", value = "税率")
    private BigDecimal sellTax;

    /** 配销价 */
    @ApiModelProperty(name = "distributionPrice", value = "配销价")
    private BigDecimal distributionPrice;

    /** 缺货数 */
    @ApiModelProperty(name = "stockoutQuantity", value = "缺货数")
    private BigDecimal stockoutQuantity;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

    /** 保质期 */
    @ApiModelProperty(name = "expirationDate", value = "保质期")
    private String expirationDate;

    /** 生产日期 */
    @ApiModelProperty(name = "produceDate", value = "生产日期")
    private LocalDate produceDate;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改人 */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /** 采购单号 */
    @ApiModelProperty(name = "purchaseNo", value = "采购单号")
    private String purchaseNo;

    /** 转单优先级 */
    @ApiModelProperty(name = "orderPriority", value = "转单优先级")
    private String orderPriority;

    /** 结转周期号 */
    @ApiModelProperty(name = "carryForwardCycle", value = "结转周期号")
    private String carryForwardCycle;

    /** 赠品主商品代码 */
    @ApiModelProperty(name = "baseGoodsCode", value = "赠品主商品代码")
    private String baseGoodsCode;

    @ApiModelProperty(name = "otherGoodsCode", value = "映射商品")
    private String otherGoodsCode;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;

    @ApiModelProperty(value = "效期码")
    private String expiry;
}
