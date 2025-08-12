package com.edc.erp.directly.returnorder.model.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.edc.erp.common.util.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 直营配货退货明细导出类
 *
 * @author lishaobo
 */
@Data
public class AsyncExcelReturnOrderDetail implements Serializable {

    private static final long serialVersionUID = -3751676153574360208L;

    @ExcelProperty(value = {"配货退货单抬头信息","单号"}, index = 0)
    @ColumnWidth(20)
    private String returnOrderNo;

    @ExcelProperty(value = {"配货退货单抬头信息","组织信息"}, index = 1)
    private String bizOrgCode;

    @ExcelProperty(value = {"配货退货单抬头信息","仓储"}, index = 2)
    private String wrhCode;

    @ExcelProperty(value = {"配货退货单抬头信息","仓位"}, index = 3)
    private String stockCode;

    @ExcelProperty(value = {"配货退货单抬头信息","门店代码"}, index = 4)
    private String storeCode;

    @ExcelProperty(value = {"配货退货单抬头信息","门店名称"}, index = 5)
    @ColumnWidth(25)
    private String storeName;

    @ExcelIgnore
    private String returnStatus;

    @ExcelProperty(value = {"配货退货单抬头信息","状态"}, index = 6)
    private String returnStatusValue;

    @ExcelIgnore
    private String returnType;

    @ExcelProperty(value = {"配货退货单抬头信息","退货类型"}, index = 7)
    private String returnTypeValue;

    @ExcelProperty(value = {"配货退货单抬头信息","退货原因"}, index = 8)
    private String returnOrderReason;

    @ExcelProperty(value = {"配货退货单抬头信息","创建人"}, index = 9)
    @ColumnWidth(15)
    private String creator;

    @ExcelProperty(value = {"配货退货单抬头信息","创建时间"}, index = 10, converter = LocalDateTimeConverter.class)
    @ColumnWidth(18)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ExcelProperty(value = {"配货退货单抬头信息","备注"}, index = 11)
    @ColumnWidth(25)
    private String remark;

    @ExcelProperty(value = {"配货退货单商品明细信息","序号"}, index = 12)
    private Integer line;

    @ExcelProperty(value = {"配货退货单商品明细信息","商品代码"}, index = 13)
    private String goodsCode;

//    @ExcelProperty(value = {"配货退货单商品明细信息","申请数量"}, index = 14)
//    private BigDecimal applyReturnQuantity;
//
//    @ExcelProperty(value = {"配货退货单商品明细信息","申请包装数量"}, index = 15)
//    @ColumnWidth(15)
//    private BigDecimal applyPackageQuantity;

    @ExcelProperty(value = {"配货退货单商品明细信息","审批数量"}, index = 14)
    private BigDecimal auditReturnQuantity;

    @ExcelProperty(value = {"配货退货单商品明细信息","审批包装数量"}, index = 15)
    @ColumnWidth(15)
    private BigDecimal auditPackageQuantity;

    @ExcelProperty(value = {"配货退货单商品明细信息","退货单价"}, index = 16)
    private BigDecimal returnUnitPrice;

    @ExcelProperty(value = {"配货退货单商品明细信息","审批退货金额"}, index = 17)
    private BigDecimal auditReturnAmount;

    @ExcelProperty(value = {"配货退货单商品明细信息","订单方代码"}, index = 18)
    private String vendorCode;

    @ExcelProperty(value = {"配货退货单商品明细信息","门店退货原因"}, index = 19)
    private String returnReason;
}
