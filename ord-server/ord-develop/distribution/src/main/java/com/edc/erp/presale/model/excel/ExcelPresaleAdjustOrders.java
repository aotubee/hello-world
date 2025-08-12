package com.edc.erp.presale.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExcelPresaleAdjustOrders implements Serializable {
    private static final long serialVersionUID = 249301228318561165L;
    /**
     * 预售单号
     */
    @Excel(name = "调整单号", width = 20)
    private String orderNo;
    /**
     * 状态描述
     */
    @Excel(name = "订单状态", orderNum = "1", width = 10)
    private String statusDesc;
    /**
     * 门店代码
     */
    @Excel(name = "门店代码", orderNum = "2", width = 10)
    private String storeCode;
    /**
     * 门店名称
     */
    @Excel(name = "门店名称", orderNum = "3", width = 30)
    private String storeName;
    /**
     * 调整类型
     */
    @Excel(name = "调整类型", orderNum = "4", width = 10)
    private String adjustTypeDesc;
    /**
     * 冲销标识
     */
    @Excel(name = "是否冲销", orderNum = "5", width = 10)
    private String isChargeDesc;
    /**
     * 是否冲销单描述
     */
    @Excel(name = "是否冲销单", orderNum = "6", width = 15)
    private String isChargeOrderDesc;
    /**
     * 原单号
     */
    @Excel(name = "原单号", orderNum = "7", width = 25)
    private String sourceNo;
    /**
     * 创建人
     */
    @Excel(name = "创建人", orderNum = "8", width = 20)
    private String creator;
    /**
     * 创建时间
     */
    @Excel(name = "创建时间", orderNum = "9", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 审核人
     */
    @Excel(name = "审核人", orderNum = "10", width = 20)
    private String approver;
    /**
     * 审核时间
     */
    @Excel(name = "审核时间", orderNum = "11", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;
    /**
     * 备注
     */
    @Excel(name = "备注", orderNum = "12", width = 30)
    private String remark;
    /**
     * 商品代码
     */
    @Excel(name = "商品代码", orderNum = "13", width = 15)
    private String goodsCode;
    /**
     * 商品名称
     */
    @Excel(name = "商品名称", orderNum = "14", width = 30)
    private String goodsName;
    /**
     * 商品条码
     */
    @Excel(name = "商品条码", orderNum = "15", width = 20)
    private String barCode;
    /**
     * 包装规格
     */
    @Excel(name = "商品规格", orderNum = "16", width = 10)
    private String packageSpecification;
    /**
     * 包装单位
     */
    @Excel(name = "规格单位", orderNum = "17", width = 10)
    private String packageUnit;
    /**
     * 当前预售数量
     */
    @Excel(name = "调整前余数", orderNum = "18", width = 15)
    private BigDecimal beforeQty;
    /**
     * 调整数量
     */
    @Excel(name = "调整数量", orderNum = "19", width = 10)
    private BigDecimal adjustQty;
    /**
     * 预售活动号
     */
    @Excel(name = "活动单号", orderNum = "20", width = 30)
    private String presaleActivityNo;
}
