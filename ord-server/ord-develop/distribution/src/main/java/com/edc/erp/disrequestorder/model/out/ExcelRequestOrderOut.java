/**
 * Copyright © 2010-2020 Everyday Chain. All rights reserved.
 */
package com.edc.erp.disrequestorder.model.out;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.edc.erp.common.util.BigDecimalAmountConverter;
import com.edc.erp.common.util.LocalDateTimeConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 集货单列表导出
 *
 * @author: lee
 */
@Data
public class ExcelRequestOrderOut {

    @ExcelProperty(value = "序号",index = 0)
    private Integer index;

    @ExcelProperty(value = "集货单单号", index = 1)
    @ColumnWidth(15)
    private String requestOrderNo;

    @ExcelProperty(value = "状态",index = 2)
    @ColumnWidth(15)
    private String statusCodeStr;

    @ExcelProperty(value = "订单类型", index = 3)
    @ColumnWidth(15)
    private String shortOrderType;

    @ExcelProperty(value = "门店代码", index = 4)
    private String storeCode;

    @ExcelProperty(value = "门店名称", index = 5)
    @ColumnWidth(25)
    private String storeName;

    @ExcelProperty(value = "门店区域", index = 6)
    @ColumnWidth(15)
    private String storeArea;

    @ExcelProperty(value = "截单时间", index = 7,converter = LocalDateTimeConverter.class)
    @ColumnWidth(25)
    private LocalDateTime truncationDateTime;

    @ExcelProperty(value = "总金额", index = 8,converter = BigDecimalAmountConverter.class)
    @ColumnWidth(15)
    private BigDecimal totalAmount;

    @ExcelProperty(value = "配货单号", index = 9)
    @ColumnWidth(30)
    private String deliveryOrderNos;

    @ExcelProperty(value = "订货单号", index = 10)
    @ColumnWidth(30)
    private String orderNos;

    @ExcelProperty(value = "创建时间", index = 11,converter = LocalDateTimeConverter.class)
    @ColumnWidth(25)
    private LocalDateTime createTime;

}
