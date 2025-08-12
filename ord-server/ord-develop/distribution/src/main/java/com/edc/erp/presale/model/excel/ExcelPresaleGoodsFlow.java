package com.edc.erp.presale.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExcelPresaleGoodsFlow implements Serializable {
    private static final long serialVersionUID = -2121319330996407576L;
    @Excel(name = "门店代码", width = 10)
    private String storeCode;
    @Excel(name = "门店名称", orderNum = "1", width = 30)
    private String storeName;
    @Excel(name = "业务单号", orderNum = "2", width = 20)
    private String sourceNo;
    @Excel(name = "商品代码", orderNum = "3", width = 15)
    private String goodsCode;
    @Excel(name = "商品名称", orderNum = "4", width = 30)
    private String goodsName;
    @Excel(name = "商品条码", orderNum = "5", width = 20)
    private String barCode;
    @Excel(name = "包装规格", orderNum = "6", width = 10)
    private String packageSpecification;
    @Excel(name = "业务类型", orderNum = "7", width = 15)
    private String businessTypeDesc;
    @Excel(name = "增减类型", orderNum = "8", width = 15)
    private String actualLowering;
    @Excel(name = "数量", orderNum = "9", width = 10)
    private BigDecimal qty;
    @Excel(name = "数量平衡", orderNum = "10", width = 10)
    private BigDecimal qtyBalance;
    @Excel(name = "发生时间", orderNum = "11", width = 25, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime flowDate;
}
