package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author LZQ
 * @date 2023年01月09日 14:22
 * 中转配货单明细出参
 */
@Data
public class TransferDeliveryOrderDetailOut implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配货单主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键")
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

    /** 要货数量 */
    @ApiModelProperty(name = "orderQuantity", value = "要货数量")
    private BigDecimal orderQuantity;

    /** 要货包装数 */
    @ApiModelProperty(name = "orderPackageQuantity", value = "要货包装数")
    private BigDecimal orderPackageQuantity;

    /** 要货单价 */
    @ApiModelProperty(name = "orderUnitPrice", value = "要货单价")
    private BigDecimal orderUnitPrice;

    /** 要货金额 */
    @ApiModelProperty(name = "orderAmount", value = "要货金额")
    private BigDecimal orderAmount;

    /** 配货数量 */
    @ApiModelProperty(name = "distributionQuantity", value = "配货数量")
    private BigDecimal distributionQuantity;

    /** 配货包装数 */
    @ApiModelProperty(name = "distributionPackageQuantity", value = "配货包装数")
    private BigDecimal distributionPackageQuantity;

    /** 配货单价 */
    @ApiModelProperty(name = "distributionUnitPrice", value = "配货单价")
    private BigDecimal distributionUnitPrice;

    /** 配货金额 */
    @ApiModelProperty(name = "distributionAmount", value = "配货金额")
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

    /**
     * 配送方式
     */
    @ApiModelProperty(name = "distributionType", value = "")
    private String distributionType;

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

    /** 配货去税金额 */
    @ApiModelProperty(name = "distributionExceptTaxAmount", value = "配货去税金额")
    private BigDecimal distributionExceptTaxAmount;

    /** 配货税额 */
    @ApiModelProperty(name = "distributionTaxAmount", value = "配货税额")
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

    /** 配送价 */
    @ApiModelProperty(name = "distributionPrice", value = "配送价")
    private BigDecimal distributionPrice;

    /** 缺货数 */
    @ApiModelProperty(name = "stockoutQuantity", value = "缺货数")
    private BigDecimal stockoutQuantity;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

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
    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "")
    private String remark;

    /** 采购单号 */
    @ApiModelProperty(name = "purchaseNo", value = "采购单号")
    private String purchaseNo;

    /** 转单优先级 */
    @ApiModelProperty(name = "orderPriority", value = "转单优先级")
    private String orderPriority;

    /** 结转周期号 */
    @ApiModelProperty(name = "carryForwardCycle", value = "结转周期号")
    private String carryForwardCycle;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /** 仓储代码 */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
