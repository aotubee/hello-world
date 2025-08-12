package com.edc.erp.wholesale.model.excel.returns;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 导出批发退货单明细实体类
 *
 * @author wanglidong
 * @since 2022/11/3 15:35
 */
@Data
public class ExportWholesaleReturnDetail implements Serializable {

    /**
     * 序号
     */
    @Excel(name = "序号", orderNum = "1", width = 10)
    private Integer no;

    /**
     * 商品代码
     */
    @Excel(name = "商品代码", orderNum = "2", width = 15)
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称", orderNum = "3", width = 15)
    private String goodsName;

    /**
     * 品类属性
     */
    @Excel(name = "品类属性", orderNum = "4", width = 15)
    private String goodsTypeStr;

    /**
     * 商品条码
     */
    @Excel(name = "商品条码", orderNum = "5", width = 30)
    private String barCode;

    /**
     * 包装规格
     */
    @Excel(name = "包装规格", orderNum = "6", width = 15)
    private String packageSpecification;

    /**
     * 包装单位
     */
    @Excel(name = "包装单位", orderNum = "7", width = 15)
    private String packageUnit;

    /**
     * 退货单价
     */
    @Excel(name = "退货单价", orderNum = "8", width = 15)
    private BigDecimal returnsPrice;

    /**
     * 申请数量
     */
    @Excel(name = "申请数量", orderNum = "9", width = 15)
    private Integer applyQuantity;

    /**
     * 申请包装数
     */
    @Excel(name = "申请包装数", orderNum = "10", width = 15)
    private Integer applyPackageNum;

    /**
     * 申请金额
     */
    @Excel(name = "申请金额", orderNum = "11", width = 15)
    private BigDecimal applyAmount;

    /**
     * 审核数量
     */
    @Excel(name = "审核数量", orderNum = "12", width = 15)
    private Integer checkQuantity;

    /**
     * 入库数量
     */
    @Excel(name = "入库数量", orderNum = "13", width = 15)
    private Integer storageQuantity;

    /**
     * 库存价
     */
    @Excel(name = "库存价", orderNum = "14", width = 15)
    private BigDecimal inventoryPrice;

    /**
     * 实际入库金额
     */
    @Excel(name = "实际入库金额", orderNum = "15", width = 15)
    private BigDecimal practicalStorageAmount;

    /**
     * 入库去税金额
     */
    @Excel(name = "入库去税金额", orderNum = "16", width = 15)
    private BigDecimal storageNetProfit;

    /**
     * 入库税额
     */
    @Excel(name = "入库税额", orderNum = "17", width = 15)
    private BigDecimal storageTax;

    /**
     * 成本金额
     */
    @Excel(name = "成本金额", orderNum = "18", width = 15)
    private BigDecimal costAmount;

    /**
     * 成本去税金额
     */
    @Excel(name = "成本去税金额", orderNum = "19", width = 15)
    private BigDecimal costNetProfitAmount;

    /**
     * 成本税额
     */
    @Excel(name = "成本税额", orderNum = "20", width = 15)
    private BigDecimal costTax;
}
