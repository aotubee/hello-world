package com.edc.erp.disdifferenceorder.model.out;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 配销差异单导出类
 *
 * @author weichao
 */
@Data
public class ExcelDisDifferenceDetail {

    @Excel(name = "序号")
    private Integer index;

    /** 商品代码 */
    @Excel(name = "商品代码", orderNum = "2")
    private String goodsCode;

    /** 商品条码 */
    @Excel(name = "商品条码", orderNum = "3", width = 25)
    private String barCode;

    /** 商品名称 */
    @Excel(name = "商品名称", orderNum = "4", width = 18)
    private String goodsName;

    /** 品类属性 */
    @Excel(name = "品类属性", orderNum = "5", width = 25)

    private String goodsTypeStr;
    /** 包装规格 */
    @Excel(name = "包装规格", orderNum = "6")
    private String distributionSpecification;

    /** 包装单位 */
    @Excel(name = "包装单位", orderNum = "7")
    private String distributionSpecificationUnit;

    /** 配货单价 */
    @Excel(name = "配销单价", orderNum = "12")
    private BigDecimal distributionUnitPrice;

    /** 申请差异数量 */
    @Excel(name = "申请差异数量", orderNum = "13")
    private BigDecimal applyDifferenceQuantity;

    /** 申请差异金额 */
    @Excel(name = "申请差异金额", orderNum = "14")
    private BigDecimal applyDifferenceAmount;

    /** 批准差异数量 */
    @Excel(name = "批准差异数量", orderNum = "15")
    private BigDecimal approvalDifferenceQuantity;

    /** 批准差异金额 */
    @Excel(name = "批准差异金额", orderNum = "16")
    private BigDecimal differenceAmount;

    /** 批准差异去税金额 */
    @Excel(name = "批准差异去税金额", orderNum = "17")
    private BigDecimal approvalDifferenceExceptTaxAmount;

    /** 批准差异税额 */
    @Excel(name = "批准差异税额", orderNum = "18")
    private BigDecimal approvalDifferenceTaxAmount;

    /** 仓储库存价 */
    @Excel(name = "仓储库存价", orderNum = "19")
    private BigDecimal wrhPrice;

    /** 仓储成本金额 */
    @Excel(name = "仓储成本金额", orderNum = "20")
    private BigDecimal wrhCostAmount;

    /** 仓储成本去税金额 */
    @Excel(name = "仓储成本去税金额", orderNum = "21")
    private BigDecimal wrhExceptTaxAmount;

    /** 仓储成本税额 */
    @Excel(name = "仓储成本税额", orderNum = "22")
    private BigDecimal wrhTaxAmount;

    /** 门店库存价 */
    @Excel(name = "门店库存价", orderNum = "23")
    private BigDecimal storeStockPrice;

    /** 门店成本金额 */
    @Excel(name = "门店成本金额", orderNum = "24")
    private BigDecimal storeCostAmount;

    /** 门店成本去税金额 */
    @Excel(name = "门店成本去税金额", orderNum = "25")
    private BigDecimal storeExceptTaxAmount;

    /** 门店成本税额 */
    @Excel(name = "门店成本税额", orderNum = "26")
    private BigDecimal storeTaxAmount;

    /** 税率 */
    @Excel(name = "税率", orderNum = "27")
    private BigDecimal sellTax;

    /** 是否赠品 */
    @Excel(name = "是否赠品", orderNum = "28")
    private String isGift;

    /** 配销价 */
    @Excel(name = "配销价", orderNum = "29")
    private BigDecimal distributionPrice;

    /** 订单方 */
    @Excel(name = "订单方", orderNum = "30")
    private String vendorCode;
}
