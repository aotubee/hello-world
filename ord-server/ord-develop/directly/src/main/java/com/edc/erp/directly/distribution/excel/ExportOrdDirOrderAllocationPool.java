package com.edc.erp.directly.distribution.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
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
public class ExportOrdDirOrderAllocationPool implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 门店代码
     */
    @Excel(name = "门店代码", orderNum = "1")
    private String storeCode;

    /**
     * 门店名称
     */
    @Excel(name = "门店名称", orderNum = "2")
    private String storeName;

    /**
     * 商品代码
     */
    @Excel(name = "商品代码", orderNum = "3")
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称", orderNum = "4")
    private String goodsName;

    /**
     * 单日平均销售数量
     */
    @Excel(name = "单日平均销售数量", orderNum = "5")
    private BigDecimal averageDailySalesQuantity;

    /**
     * 门店下单量
     */
    @Excel(name = "门店下单量", orderNum = "6")
    private BigDecimal storeOrderQuantity;

    /**
     * 补单量
     */
    @Excel(name = "补单量", orderNum = "7")
    private BigDecimal supplementQuantity;

    /**
     * 补单量
     */
    @Excel(name = "可用库存", orderNum = "8")
    private BigDecimal businessQty;


    /**
     * 基础陈列量
     */
    @Excel(name = "基础陈列量", orderNum = "9")
    private BigDecimal baseDisplayQuantity;



    /**
     * 计算订货数量
     */
    @Excel(name = "计算订货数量", orderNum = "10")
    private BigDecimal computeOrderQuantity;

    /**
     * 建议订货数量
     */
    @Excel(name = "建议订货数量", orderNum = "11")
    private BigDecimal recommendOrderQuantity;

    /**
     * 商品等级
     */
    @Excel(name = "商品等级", orderNum = "12")
    private String goodsLevel;

    /**
     * 叫货周期
     */
    @Excel(name = "叫货周期", orderNum = "13")
    private String deliveryCycle;

    /**
     * 配送规格
     */
    @Excel(name = "配送规格", orderNum = "14")
    private String distributionSpec;

    /**
     * 截单时间
     */
    @Excel(name = "截单时间", orderNum = "15")
    private String truncationDateTimeStr;


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

    /**
     * 状态
     */
    @Excel(name = "状态", orderNum = "19")
    private String statusStr;

}

