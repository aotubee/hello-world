package com.edc.erp.directly.returnorder.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 直营配货退货单明细导出类
 *
 * @author yaojinpeng
 * @since 2022/10/27 21:14
 */
@Data
public class ExportOrdDirReturnDetail {
    /**
     *
     */
    @Excel(name = "序号")
    private Integer index;
    /**
     * 商品代码
     */
    @Excel(name = "商品代码")
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称")
    private String goodsName;

    /**
     * 商品条码
     */
    @Excel(name = "商品条码")
    private String barCode;

    /**
     * 包装规格
     */
    @Excel(name = "包装规格")
    private String distributionSpecification;

    /**
     * 单位
     */
    @Excel(name = "单位")
    private String distributionSpecificationUnit;

    /**
     * 品类属性
     */
    @Excel(name = "品类属性")
    private String goodsType;

    /**
     * 退货单价
     */
    @Excel(name = "退货单价")
    private BigDecimal returnUnitPrice;

    /**
     * 申请退货数量
     */
    @Excel(name = "申请数量")
    private BigDecimal applyReturnQuantity;

    /**
     * 申请退货包装数
     */
    @Excel(name = "申请包装数")
    private BigDecimal applyPackageQuantity;

    /**
     * 申请退货金额
     */
    @Excel(name = "申请退货金额")
    private BigDecimal applyReturnAmount;

    /**
     * 审核退货数量
     */
    @Excel(name = "审核数量")
    private BigDecimal auditReturnQuantity;

    /**
     * 审核退货包装数
     */
    @Excel(name = "审核包装数")
    private BigDecimal auditPackageQuantity;

    /**
     * 审核退货金额
     */
    @Excel(name = "审核金额")
    private BigDecimal auditReturnAmount;

    /**
     * 实际退货数量
     */
    @Excel(name = "退货数量")
    private BigDecimal actualReturnQuantity;

    /**
     * 实际退货包装数
     */
    @Excel(name = "退货包装数")
    private BigDecimal actualPackageQuantity;

    /**
     * 实际退货金额
     */
    @Excel(name = "退货金额")
    private BigDecimal actualReturnAmount;



    /**
     * 退货去税金额
     */
    @Excel(name = "退货去税金额")
    private BigDecimal returnExceptTaxAmount;

    /**
     * 退货税额
     */
    @Excel(name = "退货税额")
    private BigDecimal returnTaxAmount;

    /**
     * 仓储库存价
     */
    @Excel(name = "仓储库存价")
    private BigDecimal wrhPrice;

    /**
     * 仓储成本金额
     */
    @Excel(name = "仓储成本金额")
    private BigDecimal wrhCostAmount;

    /**
     * 仓储成本去税金额
     */
    @Excel(name = "仓储成本去税金额")
    private BigDecimal wrhExceptTaxAmount;

    /**
     * 仓储成本税额
     */
    @Excel(name = "仓储成本税额")
    private BigDecimal wrhTaxAmount;

    /**
     * 门店库存价
     */
    @Excel(name = "门店库存价")
    private BigDecimal storeStockPrice;

    /**
     * 门店成本金额
     */
    @Excel(name = "门店成本金额")
    private BigDecimal storeCostAmount;

    /**
     * 门店成本去税金额
     */
    @Excel(name = "门店成本去税金额")
    private BigDecimal storeExceptTaxAmount;

    /**
     * 门店成本税额
     */
    @Excel(name = "门店成本税额")
    private BigDecimal storeTaxAmount;

    /**
     * 税率
     */
    @Excel(name = "税率")
    private BigDecimal sellTax;

    /**
     * 配送价
     */
    @Excel(name = "配送价")
    private BigDecimal distributionPrice;


    /**
     * 门店退货原因
     */
    @Excel(name = "门店退货原因")
    private String returnReason;

    /**
     * 订单方
     *
     */
    @Excel(name = "订单方")
    private String vendorCode;

    /**
     * 门店退货原因
     */
    @Excel(name = "备注")
    private String remark;

}

