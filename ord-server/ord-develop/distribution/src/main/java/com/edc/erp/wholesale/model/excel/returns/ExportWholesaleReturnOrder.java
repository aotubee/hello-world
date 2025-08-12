package com.edc.erp.wholesale.model.excel.returns;

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
public class ExportWholesaleReturnOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 出货单号 */
    @ExcelProperty(value = "退货单号",index = 0)
    @ColumnWidth(value = 20)
    private String wholesaleReturnNo;

    /** 出货状态 */
    @ExcelProperty(value = "单据状态",index = 1)
    @ColumnWidth(value = 15)
    private String returnStatusStr;

    @ExcelProperty(value = "客户信息",index=2)
    @ColumnWidth(value = 20)
    private String clientMessage;

    @ExcelProperty(value = "价格组信息",index=3)
    @ColumnWidth(value = 20)
    private String priceGroup;

    @ExcelProperty(value = "入库仓储",index=4)
    @ColumnWidth(value = 20)
    private String storageWrhStr;

    @ExcelProperty(value = "入库仓位",index=5)
    @ColumnWidth(value = 15)
    private String storageStockCodeStr;

    @ExcelProperty(value = "物流单号",index=6)
    @ColumnWidth(value = 15)
    private String trackingNo;

    @ExcelProperty(value = "申请数量",index=7)
    @ColumnWidth(value = 8)
    private Integer applicationQuantity;

    @ExcelProperty(value = "申请金额",index=8)
    @ColumnWidth(value = 8)
    private BigDecimal applicationAmount;

    @ExcelProperty(value = "入库数量",index=9)
    @ColumnWidth(value = 8)
    private Integer storageQuantity;

    @ExcelProperty(value = "入库金额",index=10)
    @ColumnWidth(value = 8)
    private BigDecimal storageAmount;


    @ExcelProperty(value = "备注",index=11)
    @ColumnWidth(value = 20)
    private String remark;

    @ExcelProperty(value = "关联出库单号",index=12)
    @ColumnWidth(value = 20)
    private String wholesaleShipmentNo;


    @ExcelProperty(value = "是否冲销单",index=13)
    @ColumnWidth(value = 8)
    private String isReversalOrderStr;

    @ExcelProperty(value = "冲销标识",index=14)
    @ColumnWidth(value = 8)
    private String isReversalStr;

    @ExcelProperty(value = "来源单号",index=15)
    @ColumnWidth(value = 20)
    private String sourceNo;

    @ExcelProperty(value = "收货时间",index=16)
    @ColumnWidth(value = 15)
    private LocalDateTime receiveTime;

    @ExcelProperty(value = "审核时间",index=16)
    @ColumnWidth(value = 15)
    private LocalDateTime auditTime;
}
