package com.edc.erp.disdifferenceorder.model.out;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.edc.erp.common.util.BigDecimalAmountConverter;
import com.edc.erp.common.util.BigDecimalConverter;
import com.edc.erp.common.util.LocalDateTimeConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配销差异单列表导出类
 *
 * @author weichao
 */
@Data
public class ExcelDisDifferenceOrder {

    @ExcelProperty(value = "序号")
    private Integer index;

    /** 配销差异单号 */
    @ExcelProperty(value = "差异单号", index = 1)
    @ColumnWidth(25)
    private String differenceNo;
    /**
     * 差异单状态中文
     */
    @ExcelProperty(value = "状态", index = 2)
    @ColumnWidth(25)
    private String differenceStatusName;

    /** 是否红冲 */
    @ExcelProperty(value = "冲销标识", index = 3)
    private String isRedRush;

    /** 是否红冲单 */
    @ExcelProperty(value = "是否冲销单", index = 4)
    private String isReversalOrder;

    /** 差异类型 */
    @ExcelProperty(value = "差异类型", index = 5)
    private String differenceTypeName;

    /**
     * 仓位信息
     */
    @ExcelProperty(value = "仓位信息", index = 6)
    private String stockName;

    /** 门店代码 */
    @ExcelProperty(value = "门店代码", index = 7)
    private String storeCode;

    /** 门店名称 */
    @ExcelProperty(value = "门店名称", index = 8)
    private String storeName;

    /** 品项数 */
    @ExcelProperty(value = "商品品项数", index = 9)
    private Integer goodsSize;

    /** 申请总差异数量 */
    @ExcelProperty(value = "申请差异数量", index = 10,converter = BigDecimalConverter.class)
    private BigDecimal totalApplyDifferenceQuantity;

    /** 申请总差异金额 */
    @ExcelProperty(value = "申请差异金额", index = 11,converter = BigDecimalAmountConverter.class)
    private BigDecimal totalApplyDifferenceAmount;

    /** 批准总差异数量 */
    @ExcelProperty(value = "批准差异数量", index = 12,converter = BigDecimalConverter.class)
    private BigDecimal totalApprovalDifferenceQuantity;

    /** 批准总差异金额 */
    @ExcelProperty(value = "批准差异金额", index = 13,converter = BigDecimalAmountConverter.class)
    private BigDecimal totalApprovalDifferenceAmount;

    /** 配销单号 */
    @ExcelProperty(value = "配销单号", index = 14)
    private String deliveryOrderNo;

    /** 物流单号 */
    @ExcelProperty(value = "物流单号",index = 15)
    private String logisticsNo;

    /** 来源单号 */
    @ExcelProperty(value = "来源单号", index = 16)
    private String sourceNo;

    /** 海鼎差异单号 */
    @ExcelProperty(value = "海鼎单号", index = 17)
    private String hdDifferenceNo;
    /** 创建人 */
    @ExcelProperty(value = "创建人", index = 18)
    private String creator;

    /** 创建时间 */
    @ExcelProperty(value = "创建时间", index = 19,converter = LocalDateTimeConverter.class)
    private LocalDateTime createTime;

    /**
     * 批准时间
     */
    @ExcelProperty(value = "批准时间", index = 20,converter = LocalDateTimeConverter.class)
    private LocalDateTime approvalTime;

    /** 备注 */
    @ExcelProperty(value = "备注", index = 21)
    private String remark;
}
