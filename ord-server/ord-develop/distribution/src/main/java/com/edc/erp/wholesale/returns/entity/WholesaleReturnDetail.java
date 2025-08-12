package com.edc.erp.wholesale.returns.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;



/**
 * 批发退货明细单(WholesaleReturnDetail)实体类
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_wholesale_return_detail")
@ApiModel(value = "WholesaleReturnDetail", description = "批发退货明细单")
public class WholesaleReturnDetail implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 批发退货单id */
    @ApiModelProperty(name = "wholesaleReturnId", value = "批发退货单id")
    private Long wholesaleReturnId;

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

    /** 退货单价 */
    @ApiModelProperty(name = "returnsPrice", value = "退货单价")
    private BigDecimal returnsPrice;

    /** 申请数量 */
    @ApiModelProperty(name = "applyQuantity", value = "申请数量")
    private Integer applyQuantity;

    /** 申请包装数 */
    @ApiModelProperty(name = "applyPackageNum", value = "申请包装数")
    private String applyPackageNum;

    /** 申请金额 */
    @ApiModelProperty(name = "applyAmount", value = "申请金额")
    private BigDecimal applyAmount;

    /** 审核数量 */
    @ApiModelProperty(name = "checkQuantity", value = "审核数量")
    private Integer checkQuantity;

    /** 入库数量 */
    @ApiModelProperty(name = "storageQuantity", value = "入库数量")
    private Integer storageQuantity;

    /** 库存价 */
    @ApiModelProperty(name = "inventoryPrice", value = "库存价")
    private BigDecimal inventoryPrice;

    /** 实际入库金额 */
    @ApiModelProperty(name = "practicalStorageAmount", value = "实际入库金额")
    private BigDecimal practicalStorageAmount;

    /** 入库去税金额 */
    @ApiModelProperty(name = "storageNetProfit", value = "入库去税金额")
    private BigDecimal storageNetProfit;

    /** 入库税额 */
    @ApiModelProperty(name = "storageTax", value = "入库税额")
    private BigDecimal storageTax;

    /** 成本金额 */
    @ApiModelProperty(name = "costAmount", value = "成本金额")
    private BigDecimal costAmount;

    /** 成本去税金额 */
    @ApiModelProperty(name = "costNetProfitAmount", value = "成本去税金额")
    private BigDecimal costNetProfitAmount;

    /** 成本税额 */
    @ApiModelProperty(name = "costTax", value = "成本税额")
    private BigDecimal costTax;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号", required = true)
    @NotNull(message = "行号不能为空!")
    private Integer line;

    /** 退货原则 */
    @ApiModelProperty(name = "returnPrinciple", value = "退货原则", required = true)
    private String returnPrinciple;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

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

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;

    @Transient
    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;
}
