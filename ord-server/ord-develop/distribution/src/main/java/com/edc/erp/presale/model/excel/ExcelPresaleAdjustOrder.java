package com.edc.erp.presale.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExcelPresaleAdjustOrder implements Serializable {
    /**
     * 主键
     */
    @Excel(name = "序号", width = 5)
    private Integer index;
    /**
     * 预售单号
     */
    @Excel(name = "调整单号", orderNum = "1", width = 20)
    private String orderNo;
    /**
     * 状态描述
     */
    @Excel(name = "订单状态", orderNum = "2", width = 10)
    private String statusDesc;
    /**
     * 门店代码
     */
    @Excel(name = "门店代码", orderNum = "3", width = 10)
    private String storeCode;
    /**
     * 门店名称
     */
    @Excel(name = "门店名称", orderNum = "4", width = 30)
    private String storeName;
    /**
     * 品项数
     */
    @Excel(name = "品项数", orderNum = "5", width = 10)
    private Integer totalSkuQty;
    /**
     * 调整数量
     */
    @Excel(name = "调整数量", orderNum = "6", width = 10)
    private BigDecimal adjustQty;
    /**
     * 调整类型
     */
    @Excel(name = "调整类型", orderNum = "7", width = 10)
    private String adjustTypeDesc;
    /**
     * 冲销标识
     */
    @Excel(name = "是否冲销", orderNum = "8", width = 10)
    private String isChargeDesc;
    /**
     * 是否冲销单描述
     */
    @Excel(name = "是否冲销单", orderNum = "9", width = 15)
    private String isChargeOrderDesc;
    /**
     * 原单号
     */
    @Excel(name = "原单号", orderNum = "10", width = 25)
    private String sourceNo;
    /**
     * 创建人
     */
    @Excel(name = "创建人", orderNum = "11", width = 20)
    private String creator;
    /**
     * 创建时间
     */
    @Excel(name = "创建时间", orderNum = "12", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 审核人
     */
    @Excel(name = "审核人", orderNum = "13", width = 20)
    private String approver;
    /**
     * 审核时间
     */
    @Excel(name = "审核时间", orderNum = "14", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;
    @Excel(name = "备注", orderNum = "15", width = 30)
    private String remark;
}
