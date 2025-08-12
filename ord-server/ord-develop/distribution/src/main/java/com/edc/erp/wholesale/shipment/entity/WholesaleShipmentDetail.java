package com.edc.erp.wholesale.shipment.entity;


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
import java.time.LocalDateTime;



/**
 * 批发出货单明细(WholesaleShipmentDetail)实体类
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_wholesale_shipment_detail")
@ApiModel(value = "WholesaleShipmentDetail", description = "批发出货单明细")
public class WholesaleShipmentDetail implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 批发出货单id */
    @ApiModelProperty(name = "wholesaleShipmentId", value = "批发出货单id")
    private Long wholesaleShipmentId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 品类属性 */
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    /** 商品条码 */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /** 包装规格 */
    @ApiModelProperty(name = "packageSpecification", value = "包装规格")
    private String packageSpecification;

    /** 包装单位 */
    @ApiModelProperty(name = "packageUnit", value = "包装单位")
    private String packageUnit;

    /** 申请数量 */
    @ApiModelProperty(name = "applyQuantity", value = "申请数量")
    private Integer applyQuantity;

    /** 申请包装数 */
    @ApiModelProperty(name = "applyPackageNum", value = "申请包装数")
    private String applyPackageNum;

    /** 单价 */
    @ApiModelProperty(name = "unitPrice", value = "单价")
    private BigDecimal unitPrice;

    /** 审请金额 */
    @ApiModelProperty(name = "applyAmount", value = "审请金额")
    private BigDecimal applyAmount;

    /** 审核数量 */
    @ApiModelProperty(name = "auditQuantity", value = "审核数量")
    private Integer auditQuantity;

    /** 审核金额 */
    @ApiModelProperty(name = "auditAmount", value = "审核金额")
    private BigDecimal auditAmount;

    /** 出库数量 */
    @ApiModelProperty(name = "shipmentQuantity", value = "出库数量")
    private Integer shipmentQuantity;

    /** 出库包装数 */
    @ApiModelProperty(name = "shipmentPackageQuantity", value = "出库包装数")
    private String shipmentPackageQuantity;

    /** 库存价 */
    @ApiModelProperty(name = "inventoryPrice", value = "库存价")
    private BigDecimal inventoryPrice;

    /** 实际出库金额 */
    @ApiModelProperty(name = "practicalShipmentAmount", value = "实际出库金额")
    private BigDecimal practicalShipmentAmount;

    /** 出库去税金额 */
    @ApiModelProperty(name = "shipmentNetProfit", value = "出库去税金额")
    private BigDecimal shipmentNetProfit;

    /** 出库税额 */
    @ApiModelProperty(name = "shipmentTax", value = "出库税额")
    private BigDecimal shipmentTax;

    /** 成本金额 */
    @ApiModelProperty(name = "costAmount", value = "成本金额")
    private BigDecimal costAmount;

    /** 成本去税金额 */
    @ApiModelProperty(name = "costNetProfitAmount", value = "成本去税金额")
    private BigDecimal costNetProfitAmount;

    /** 成本税额 */
    @ApiModelProperty(name = "costTax", value = "成本税额")
    private BigDecimal costTax;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

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

    /** 退货原则 */
    @ApiModelProperty(name = "returnPrinciple", value = "退货原则")
    private String returnPrinciple;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;

    /** 是否赠品 */
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

    /** 采购单号 */
    @ApiModelProperty(name = "purchaseNo", value = "采购单号")
    private String purchaseNo;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;
}
