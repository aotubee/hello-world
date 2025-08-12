package com.edc.erp.distribution.model.out;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.edc.erp.common.util.BigDecimalConverter;
import com.edc.erp.common.util.LocalDateTimeConverter;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author fxw
 * @description: 配销订货单导出
 * @since 2022/11/15 16:40
 */
@Data
public class ExcelOrderOut implements Serializable {

    private static final long serialVersionUID = -410843328412212884L;
    /**
     * 序号
     */
    @ExcelProperty(value = "序号", index = 0)
    @ColumnWidth(10)
    private Integer index;

    /**
     * 订货单单号
     */
    @ExcelProperty(value = "订货单号",index = 1)
    @ColumnWidth(30)
    private String orderNo;

    /**
     * 订货单状态中文
     */
    @ExcelProperty(value = "状态",index = 2)
    @ColumnWidth(20)
    private String orderStatusCodeStr;

    /**
     * 订单类型
     */
    @ExcelProperty(value = "订单类型",index = 3)
    @ColumnWidth(25)
    private String orderTypeName;

    /**
     * 门店代码
     */
    @ExcelProperty(value = "门店代码",index = 4)
    @ColumnWidth(20)
    private String storeCode;

    /**
     * 门店名称
     */
    @ExcelProperty(value = "门店名称",index = 5)
    @ColumnWidth(20)
    private String storeName;

    /**
     * 门店区域
     */
    @ExcelProperty(value = "门店区域",index = 6)
    @ColumnWidth(20)
    private String storeAreaStr;

    /**
     * 订货类型
     */
    @ExcelProperty(value = "订货类型",index = 7)
    @ColumnWidth(20)
    private String sourceCodeStr;

    /**
     * 截单时间
     */
    @ExcelProperty(value = "截单时间",index = 8,converter = LocalDateTimeConverter.class)
    @ColumnWidth(30)
    private LocalDateTime truncationDateTime;

    /**
     * 整单优惠金额
     *
     */
    @ExcelProperty(value = "整单优惠金额",index = 9,converter = BigDecimalConverter.class)
    @ColumnWidth(15)
    private BigDecimal discountAmount;

    /**
     * 订单应付金额
     */
    @ExcelProperty(value = "订单应付金额",index = 10,converter = BigDecimalConverter.class)
    @ColumnWidth(25)
    private BigDecimal orderAmount;

    /**
     * 订单优惠金额
     */
    @ExcelProperty(value = "订单优惠金额",index = 11,converter = BigDecimalConverter.class)
    @ColumnWidth(25)
    private BigDecimal preferentialAmount;

    /**
     * 提交人员
     */
    @ExcelProperty(value = "提交人员",index = 12)
    @ColumnWidth(20)
    private String submitter;

    /**
     * 提交时间
     */
    @ExcelProperty(value = "提交时间",index = 13,converter = LocalDateTimeConverter.class)
    @ColumnWidth(30)
    private LocalDateTime sumbitTime;

    /**
     * 要货单单号
     */
    @ExcelProperty(value = "要货单号",index = 14)
    @ColumnWidth(30)
    private String requestOrderNo;
}
