package com.edc.erp.wholesale.model.excel.shipment;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 批量导出 批发出货单Excel实体
 * @author lx
 * @since 2023-03-20 16:34:13
 */
@Data
public class BatchExportShipmentOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 出货单号 */
    @ExcelProperty(value = "单号",index = 0)
    @ColumnWidth(value = 20)
    private String shipmentNo;

    /** 出货状态 */
    @ExcelProperty(value = "单据状态",index = 1)
    @ColumnWidth(value = 15)
    private String shipmentStatusStr;

    @ExcelProperty(value = "客户信息",index=2)
    @ColumnWidth(value = 20)
    private String clientCodeStr;

    @ExcelProperty(value = "价格组信息",index=3)
    @ColumnWidth(value = 20)
    private String priceGroupCodeStr;

    @ExcelProperty(value = "收货地址",index=4)
    @ColumnWidth(value = 20)
    private String addressDetail;

    @ExcelProperty(value = "出库仓储",index=5)
    @ColumnWidth(value = 15)
    private String warehouseStr;

    @ExcelProperty(value = "出库仓位",index=6)
    @ColumnWidth(value = 15)
    private String stockStr;

    @ExcelProperty(value = "配送方式",index=7)
    @ColumnWidth(value = 10)
    private String distributionWayStr;

    @ExcelProperty(value = "物流单号",index=8)
    @ColumnWidth(value = 15)
    private String trackingNo;

    @ExcelProperty(value = "配货方式",index=9)
    @ColumnWidth(value = 8)
    private String distributionTypeStr;

    @ExcelProperty(value = "司机信息",index=10)
    @ColumnWidth(value = 15)
    private String driverInfo;

    @ExcelProperty(value = "申请数量",index=11)
    @ColumnWidth(value = 8)
    private Integer applicationQuantity;

    @ExcelProperty(value = "申请金额",index=12)
    @ColumnWidth(value = 8)
    private BigDecimal applicationAmount;

    @ExcelProperty(value = "审核数量",index=13)
    @ColumnWidth(value = 8)
    private Integer auditQuantity;

    @ExcelProperty(value = "审核金额",index=14)
    @ColumnWidth(value = 8)
    private BigDecimal auditAmount;

    @ExcelProperty(value = "出库数量",index=15)
    @ColumnWidth(value = 8)
    private Integer shipmentQuantity;

    @ExcelProperty(value = "实际出库金额",index=16)
    @ColumnWidth(value = 8)
    private BigDecimal practicalShipmentAmount;

    @ExcelProperty(value = "备注",index=17)
    @ColumnWidth(value = 10)
    private String remark;

    @ExcelProperty(value = "是否冲销单",index=18)
    @ColumnWidth(value = 8)
    private String isReversalOrderStr;

    @ExcelProperty(value = "冲销标识",index=19)
    @ColumnWidth(value = 8)
    private String isReversalStr;

    @ExcelProperty(value = "来源单号",index=20)
    @ColumnWidth(value = 15)
    private String sourceNo;

    @ExcelProperty(value = "发货日期",index=21)
    @ColumnWidth(value = 15)
    private LocalDateTime deliveryTime;

    @ExcelProperty(value = "审核时间",index=22)
    @ColumnWidth(value = 15)
    private LocalDateTime auditTime;
}
