package com.edc.erp.presale.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExcelOrdDisPresaleAssetsDetail implements Serializable {
    private static final long serialVersionUID = 8242197452460910036L;
    @Excel(name = "门店代码", width = 10)
    private String storeCode;
    @Excel(name = "门店名称", orderNum = "1", width = 30)
    private String storeName;
    @Excel(name = "活动单号", orderNum = "2", width = 20)
    private String presaleActivityNo;
    @Excel(name = "状态", orderNum = "3", width = 15)
    private String statusStr;
    @Excel(name = "商品代码", orderNum = "4", width = 15)
    private String goodsCode;
    @Excel(name = "商品名称", orderNum = "5", width = 30)
    private String goodsName;
    @Excel(name = "商品条码", orderNum = "6", width = 20)
    private String barCode;
    @Excel(name = "包装规格", orderNum = "7", width = 15)
    private String packageSpecification;
    @Excel(name = "已订包装数", orderNum = "8", width = 20)
    private BigDecimal packageQuantity;
    @Excel(name = "已订货量", orderNum = "9", width = 15)
    private BigDecimal orderQuantity;
    @Excel(name = "剩余数量", orderNum = "10", width = 20)
    private BigDecimal surplusQuantity;
    @Excel(name = "剩余包装数", orderNum = "11", width = 20)
    private String surplusPackageQuantity;
    @Excel(name = "订货开始时间", orderNum = "12", width = 25, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginOrderDate;
    @Excel(name = "订货结束时间", orderNum = "13", width = 25, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endOrderDate;
}
