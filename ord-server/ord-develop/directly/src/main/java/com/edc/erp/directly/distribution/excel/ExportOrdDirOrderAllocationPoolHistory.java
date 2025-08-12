package com.edc.erp.directly.distribution.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 门店订货调配表实体类
 *
 * @author wanglidong
 * @since 2022-11-15 16:23:10
 */
@Data
public class ExportOrdDirOrderAllocationPoolHistory implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 状态
     */
    @Excel(name = "状态", orderNum = "1")
    private String statusStr;

    /**
     * 门店代码
     */
    @Excel(name = "门店代码", orderNum = "2")
    private String storeCode;

    /**
     * 门店名称
     */
    @Excel(name = "门店名称", orderNum = "3")
    private String storeName;

    /**
     * 截单时间
     */
    @Excel(name = "截单时间", orderNum = "4")
    private String truncationDateTimeStr;

    /**
     * 叫货周期
     */
    @Excel(name = "叫货周期", orderNum = "5")
    private String deliveryCycle;

    /**
     * 商品代码
     */
    @Excel(name = "商品代码", orderNum = "6")
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称", orderNum = "7")
    private String goodsName;

    /**
     * 配送规格
     */
    @Excel(name = "配送规格", orderNum = "8")
    private String distributionSpec;

    /**
     * 商品等级
     */
    @Excel(name = "商品等级", orderNum = "9")
    private String goodsLevel;

    /**
     * 基础陈列量
     */
    @Excel(name = "基础陈列量", orderNum = "10")
    private BigDecimal baseDisplayQuantity;

    /**
     * 单日平均销售数量
     */
    @Excel(name = "单日平均销售数量", orderNum = "11")
    private BigDecimal averageDailySalesQuantity;

    /**
     * 计算订货数量
     */
    @Excel(name = "计算订货数量", orderNum = "12")
    private BigDecimal computeOrderQuantity;

    /**
     * 建议订货数量
     */
    @Excel(name = "建议订货数量", orderNum = "13")
    private BigDecimal recommendOrderQuantity;

    /**
     * 门店下单量
     */
    @Excel(name = "门店下单量", orderNum = "14")
    private BigDecimal storeOrderQuantity;

    /**
     * 补单量
     */
    @Excel(name = "补单量", orderNum = "15")
    private BigDecimal supplementQuantity;


    /**
     * 备注
     */
    @Excel(name = "差异说明", orderNum = "16")
    private String remark;

    /**
     * 备注
     */
    @Excel(name = "操作人", orderNum = "17")
    private String updater;

    /**
     * 备注
     */
    @Excel(name = "操作时间", orderNum = "18")
    private String updateTimeStr;

}

