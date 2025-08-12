package com.edc.erp.directly.distribution.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author lx
 * @since 2022-11-17 10:09:26
 */
@Data
public class ExportOrdDirOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门店代码
     */
    @Excel(name = "门店代码", orderNum = "1")
    private String storeCode;

    /**
     * 门店名称
     */
    @Excel(name = "门店名称", orderNum = "2", width = 25)
    private String storeName;

    /**
     * 商品sku
     */
    @Excel(name = "商品代码", orderNum = "3")
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称", orderNum = "4", width = 25)
    private String goodsName;

    /**
     * 规格
     */
    @Excel(name = "配货规格", orderNum = "5")
    private String distributionSpecification;

    /**
     * 当前库存
     */
    @Excel(name = "当前库存",orderNum = "6",numFormat = "0.00")
    private BigDecimal wrhInvQty;

    /**
     * 分货包装数
     */
    @Excel(name = "分货包装数", orderNum = "7")
    private BigDecimal packingNumber;

    /**
     * 分配数量
     */
    @Excel(name = "分货数量", orderNum = "8")
    private BigDecimal distributionQuantity;

    /**
     * 分货金额
     */
    @Excel(name = "分货金额", orderNum = "9", numFormat = "0.00")
    private BigDecimal distributionAmount;


    /**
     * 商品原价（配送价）
     */
    @Excel(name = "配送价", orderNum = "10", numFormat = "0.0000")
    private BigDecimal originalPrice;
}


