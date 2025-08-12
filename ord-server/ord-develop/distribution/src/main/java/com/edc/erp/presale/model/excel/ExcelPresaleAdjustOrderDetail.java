package com.edc.erp.presale.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ExcelPresaleAdjustOrderDetail implements Serializable {
    private static final long serialVersionUID = 5925026898794059551L;
    /**
     * 商品代码
     */
    @Excel(name = "商品代码", width = 15)
    private String goodsCode;
    /**
     * 商品名称
     */
    @Excel(name = "商品名称", orderNum = "1", width = 30)
    private String goodsName;
    /**
     * 商品条码
     */
    @Excel(name = "商品条码", orderNum = "2", width = 20)
    private String barCode;
    /**
     * 包装规格
     */
    @Excel(name = "商品规格", orderNum = "3", width = 10)
    private String packageSpecification;
    /**
     * 包装单位
     */
    @Excel(name = "规格单位", orderNum = "4", width = 10)
    private String packageUnit;
    /**
     * 当前预售数量
     */
    @Excel(name = "调整前余数", orderNum = "5", width = 15)
    private BigDecimal beforeQty;
    /**
     * 调整数量
     */
    @Excel(name = "调整数量", orderNum = "6", width = 10)
    private BigDecimal adjustQty;
    /**
     * 预售活动号
     */
    @Excel(name = "活动单号", orderNum = "7", width = 30)
    private String presaleActivityNo;
}
