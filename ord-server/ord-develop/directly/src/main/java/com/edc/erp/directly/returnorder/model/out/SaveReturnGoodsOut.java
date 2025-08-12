package com.edc.erp.directly.returnorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * 添加商品时出参
 * @author
 * @since
 */
@Data
public class SaveReturnGoodsOut {

    @ApiModelProperty(name = "specification", value = "规格")
    private String distributionSpecification;

    @ApiModelProperty(name = "unit", value = "单位")
    private String distributionSpecificationUnit;
    /**
     * 配货规格数量（1*12中的12）
     */
    @ApiModelProperty(name = "distributionSpecificationNum", value = "配货规格数量（1*12中的12）")
    private BigDecimal distributionSpecificationNum;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品代码")
    private String goodsName;

    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文")
    private String goodsTypeStr;

    @ApiModelProperty(name = "returnPrice", value = "退货单价")
    private BigDecimal returnUnitPrice;


    @ApiModelProperty(name = "orgGoodsId", value = "组织商品id")
    private Integer orgGoodsId;

    /**
     * 供应商
     */
    @ApiModelProperty(name = "vendorName", value = "订单方名称")
    private String vendorName;

    /**
     * 供应商(订单方)
     */
    @ApiModelProperty(name = "vendorCode", value = "供应商代码(订单方代码)")
    private String vendorCode;

    /**
     * 退货原因
     */
    @ApiModelProperty(name = "returnReason", value = "退货原因")
    private String returnReason;

    /**
     * 申请退货数量
     */
    @ApiModelProperty(name = "applyReturnQuantity", value = "申请退货数量")
    private BigDecimal applyReturnQuantity;

    /**
     * 申请退货包装数
     */
    @ApiModelProperty(name = "applyPackageQuantity", value = "申请退货包装数")
    private BigDecimal applyPackageQuantity;

    /**
     * 申请退货金额
     */
    @ApiModelProperty(name = "applyReturnAmount", value = "申请退货金额")
    private BigDecimal applyReturnAmount;

    /**
     * 审核退货数量
     */
    @ApiModelProperty(name = "auditReturnQuantity", value = "审核退货数量")
    private BigDecimal auditReturnQuantity;

    /**
     * 审核退货包装数
     */
    @ApiModelProperty(name = "auditPackageQuantity", value = "审核退货包装数")
    private BigDecimal auditPackageQuantity;

    /**
     * 审核退货金额
     */
    @ApiModelProperty(name = "auditReturnAmount", value = "审核退货金额")
    private BigDecimal auditReturnAmount;

    /**
     * 实际退货数量
     */
    @ApiModelProperty(name = "actualReturnQuantity", value = "实际退货数量")
    private BigDecimal actualReturnQuantity;

    /**
     * 实际退货包装数
     */
    @ApiModelProperty(name = "actualPackageQuantity", value = "实际退货包装数")
    private BigDecimal actualPackageQuantity;

    /**
     * 实际退货金额
     */
    @ApiModelProperty(name = "actualReturnAmount", value = "实际退货金额")
    private BigDecimal actualReturnAmount;

    /**
     * 赠品是否可退
     */
    @ApiModelProperty(name = "isGiftReturn", value = "赠品是否可退")
    private Integer isGiftReturn;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;



    /**
     * 退货去税金额
     */
    @ApiModelProperty(name = "returnExceptTaxAmount", value = "退货去税金额")
    private BigDecimal returnExceptTaxAmount;

    /**
     * 退货税额
     */
    @ApiModelProperty(name = "returnTaxAmount", value = "退货税额")
    private BigDecimal returnTaxAmount;

    /**
     * 仓储库存价
     */
    @ApiModelProperty(name = "wrhPrice", value = "仓储库存价")
    private BigDecimal wrhPrice;

    /**
     * 仓储成本金额
     */
    @ApiModelProperty(name = "wrhCostAmount", value = "仓储成本金额")
    private BigDecimal wrhCostAmount;

    /**
     * 仓储成本去税金额
     */
    @ApiModelProperty(name = "wrhExceptTaxAmount", value = "仓储成本去税金额")
    private BigDecimal wrhExceptTaxAmount;

    /**
     * 仓储成本税额
     */
    @ApiModelProperty(name = "wrhTaxAmount", value = "仓储成本税额")
    private BigDecimal wrhTaxAmount;

    /**
     * 门店库存价
     */
    @ApiModelProperty(name = "storeStockPrice", value = "门店库存价")
    private BigDecimal storeStockPrice;

    /**
     * 门店成本金额
     */
    @ApiModelProperty(name = "storeCostAmount", value = "门店成本金额")
    private BigDecimal storeCostAmount;

    /**
     * 门店成本去税金额
     */
    @ApiModelProperty(name = "storeExceptTaxAmount", value = "门店成本去税金额")
    private BigDecimal storeExceptTaxAmount;

    /**
     * 门店成本税额
     */
    @ApiModelProperty(name = "storeTaxAmount", value = "门店成本税额")
    private BigDecimal storeTaxAmount;

    /**
     * 税率
     */
    @ApiModelProperty(name = "sellTax", value = "税率")
    private BigDecimal sellTax;

    /**
     * 配送价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配送价")
    private BigDecimal distributionPrice;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;

    @ApiModelProperty(value = "导入序号")
    private Integer importIndex;

    @ApiModelProperty(value = "效期码")
    private String expiry;

    @ApiModelProperty(value = "商品是否管理效期")
    private Integer isManageValidityPeriod;
}
