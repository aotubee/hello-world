package com.edc.erp.directly.dirdifferenceorder.entity;


import java.math.BigDecimal;
import java.util.Date;
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
import java.time.LocalDateTime;



/**
 * 差异单详细表(OrdDirDelivDifferenceDetail)实体类
 *
 * @author weichao
 * @since 2022-11-14 11:32:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_deliv_difference_detail")
@ApiModel(value = "OrdDirDelivDifferenceDetail", description = "差异单详细表")
public class OrdDirDelivDifferenceDetail implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 差异单主键 */
    @ApiModelProperty(name = "differenceOrderId", value = "差异单主键")
    private Integer differenceOrderId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品条码 */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 商品主图 */
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;

    /** 发货量 */
    @ApiModelProperty(name = "deliveryQuantity", value = "发货量")
    private BigDecimal deliveryQuantity;

    /** 发货包装数 */
    @ApiModelProperty(name = "deliveryPackageQuantity", value = "发货包装数")
    private BigDecimal deliveryPackageQuantity;

    /** 到货量 */
    @ApiModelProperty(name = "arrivalQuantity", value = "到货量")
    private BigDecimal arrivalQuantity;

    /** 到货包装数 */
    @ApiModelProperty(name = "arrivalPackageQuantity", value = "到货包装数")
    private BigDecimal arrivalPackageQuantity;

    /** 配货单价 */
    @ApiModelProperty(name = "distributionUnitPrice", value = "配货单价")
    private BigDecimal distributionUnitPrice;

    /** 申请差异数量 */
    @ApiModelProperty(name = "applyDifferenceQuantity", value = "申请差异数量")
    private BigDecimal applyDifferenceQuantity;

    /** 申请差异金额 */
    @ApiModelProperty(name = "applyDifferenceAmount", value = "申请差异金额")
    private BigDecimal applyDifferenceAmount;

    /** 批准差异数量 */
    @ApiModelProperty(name = "approvalDifferenceQuantity", value = "批准差异数量")
    private BigDecimal approvalDifferenceQuantity;

    /** 批准差异金额 */
    @ApiModelProperty(name = "differenceAmount", value = "批准差异金额")
    private BigDecimal differenceAmount;

    /** 商品配货规格（1*12） */
    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*12）")
    private String distributionSpecification;

    /** 商品配货规格单位（件/箱） */
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

    /** 品类属性 */
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    /** 批准差异去税金额 */
    @ApiModelProperty(name = "approvalDifferenceExceptTaxAmount", value = "批准差异去税金额")
    private BigDecimal approvalDifferenceExceptTaxAmount;

    /** 批准差异税额 */
    @ApiModelProperty(name = "approvalDifferenceTaxAmount", value = "批准差异税额")
    private BigDecimal approvalDifferenceTaxAmount;

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

    /** 是否赠品 */
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;

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

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;

    @ApiModelProperty(value = "效期码")
    private String expiry;
}
