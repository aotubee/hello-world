package com.edc.erp.directly.distribution.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 直营订货单详情导出实体类
 *
 * @author wanglidong
 * @since 2022/11/16 9:49
 */
@Data
public class ExcelOrdDirOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 序号
     */
    @Excel(name = "序号", orderNum = "1", width = 10)
    private Integer no;

    /**
     * 商品代码
     */
    @Excel(name = "商品代码", orderNum = "2", width = 20)
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称", orderNum = "3", width = 20)
    private String goodsName;

    /**
     * 品类属性(需要转中文)
     */
    @Excel(name = "品类属性", orderNum = "4", width = 20)
    private String goodsTypeStr;

    /**
     * 商品条码
     */
    @Excel(name = "商品条码", orderNum = "5", width = 20)
    private String barCode;

    /**
     * 商品配货规格单位（件/箱）
     */
    @Excel(name = "商品配货规格单位", orderNum = "6", width = 20)
    private String distributionSpecificationUnit;

    /**
     * 订货数量
     */
    @Excel(name = "订货数量", orderNum = "7", width = 20)
    private BigDecimal quantity;

    /**
     * 订货包装数量
     */
    @Excel(name = "订货包装数量", orderNum = "8", width = 20)
    private BigDecimal packageQuantity;

    /**
     * 订货单价
     */
    @Excel(name = "订货单价", orderNum = "9", width = 20)
    private BigDecimal orderUnitPrice;

    /**
     * 订货金额
     */
    @Excel(name = "订货金额", orderNum = "10", width = 20)
    private BigDecimal orderAmount;

    /**
     * 配送价（原价）
     */
    @Excel(name = "配送价", orderNum = "11", width = 20)
    private BigDecimal originalPrice;

    /**
     * 配送金额
     */
    @Excel(name = "配送金额", orderNum = "12", width = 20)
    private BigDecimal originaAmount;

    /**
     * 活动单价
     */
    @Excel(name = "活动单价", orderNum = "13", width = 20)
    private BigDecimal activityUnitPrice;

    /**
     * 活动类型
     */
    @Excel(name = "活动类型", orderNum = "14", width = 20)
    private String activityType;

    /**
     * 活动单号
     */
    @Excel(name = "活动单号", orderNum = "15", width = 20)
    private String activityNo;

    /**
     * 是否赠品(需要转中文)
     */
    @Excel(name = "是否赠品", orderNum = "16", width = 20)
    private String isGiftStr;

    /**
     * 包装单位
     */
    @Excel(name = "包装单位", orderNum = "17", width = 20)
    private String specificationUnit;

    /**
     * 仓位代码(需要转中文)
     */
    @Excel(name = "仓位代码", orderNum = "18", width = 20)
    private String positionStr;

    /**
     * 配送方式(需要转中文)
     */
    @Excel(name = "配送方式", orderNum = "19", width = 20)
    private String distributionTypeStr;

    /**
     * 商品小分类(需要转中文)
     */
    @Excel(name = "商品小分类", orderNum = "20", width = 30)
    private String smallSortStr;

    /**
     * 是否可退(需要转中文)
     */
    @Excel(name = "是否可退", orderNum = "21", width = 20)
    private String allowDistributionReturnStr;

    /**
     * 建议零售价
     */
    @Excel(name = "建议零售价", orderNum = "22", width = 20)
    private BigDecimal suggestedRetailPrice;
}
