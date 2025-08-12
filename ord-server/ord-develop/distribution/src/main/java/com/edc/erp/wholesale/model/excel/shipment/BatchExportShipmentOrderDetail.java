package com.edc.erp.wholesale.model.excel.shipment;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.edc.erp.common.util.LocalDateTimeConverter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 批量导出 批发出货单明细Excel实体
 * @author lx
 * @since 2023-03-20 16:34:13
 */
@Data
public class BatchExportShipmentOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 出货单号 */
    @ExcelProperty(value = "单号", index = 0)
    @ColumnWidth(value = 20)
    private String shipmentNo;

    /** 出货状态 */
    @ExcelProperty(value = "单据状态", index = 1)
    @ColumnWidth(value = 20)
    private String shipmentStatusStr;

    /** 商品代码 */
    @ExcelProperty(value = "商品代码", index = 2)
    @ColumnWidth(value = 15)
    private String goodsCode;

    /** 商品名称 */
    @ExcelProperty(value = "商品名称", index = 3)
    @ColumnWidth(value = 25)
    private String goodsName;

    /** 品类属性 */
    @ExcelProperty(value = "品类属性", index = 4)
    @ColumnWidth(value = 5)
    private String goodsTypeStr;


    /** 单价 */
    @ExcelProperty(value = "单价", index = 5)
    @ColumnWidth(value = 5)
    private BigDecimal unitPrice;

    /** 商品条码 */
    @ExcelProperty(value = "商品条码", index = 6)
    @ColumnWidth(value = 22)
    private String barCode;

    /** 包装规格 */
    @ExcelProperty(value = "包装规格", index = 7)
    @ColumnWidth(value = 5)
    private String packageSpecification;


    /** 申请数量 */
    @ExcelProperty(value = "申请数量", index = 8)
    @ColumnWidth(value = 5)
    private Integer applyQuantity;

    /** 申请包装数 */
    @ExcelProperty(value = "申请包装数", index = 9)
    @ColumnWidth(value = 8)
    private Integer applyPackageNum;

    /** 审请金额 */
    @ExcelProperty(value = "审请金额", index = 10)
    @ColumnWidth(value = 8)
    private BigDecimal applyAmount;

    /** 审核数量 */
    @ExcelProperty(value = "审核数量", index = 11)
    @ColumnWidth(value = 5)
    private Integer auditQuantity;

    /** 出库数量 */
    @ExcelProperty(value = "出库数量", index = 12)
    @ColumnWidth(value = 5)
    private Integer shipmentQuantity;

    /** 库存价 */
    @ExcelProperty(value = "库存价", index = 13)
    @ColumnWidth(value = 8)
    private BigDecimal inventoryPrice;

    /** 实际出库金额 */
    @ExcelProperty(value = "实际出库金额", index = 14)
    @ColumnWidth(value = 8)
    private BigDecimal practicalShipmentAmount;

    /** 出库去税金额 */
    @ExcelProperty(value = "出库去税金额", index = 15)
    @ColumnWidth(value = 8)
    private BigDecimal shipmentNetProfit;

    /** 出库税额 */
    @ExcelProperty(value = "出库税额", index = 16)
    @ColumnWidth(value = 8)
    private BigDecimal shipmentTax;

    /** 成本金额 */
    @ExcelProperty(value = "成本金额", index = 17)
    @ColumnWidth(value = 8)
    private BigDecimal costAmount;

    /** 成本去税金额 */
    @ExcelProperty(value = "成本去税金额", index = 18)
    @ColumnWidth(value = 8)
    private BigDecimal costNetProfitAmount;

    /** 成本税额 */
    @ExcelProperty(value = "成本税额", index = 19)
    @ColumnWidth(value = 8)
    private BigDecimal costTax;

    /** 来源单号 */
    @ExcelProperty(value = "来源单号", index = 20)
    @ColumnWidth(value = 8)
    private String sourceNo;

    @ExcelProperty(value = "退货原则", index = 21)
    @ColumnWidth(value = 8)
    private String returnPrinciple;

    /** 订单方代码 */
    @ExcelProperty(value = "订单方", index = 22)
    @ColumnWidth(value = 8)
    private String vendorCode;

    /** 采购单号 */
    @ExcelProperty(value = "采购单号", index = 23)
    @ColumnWidth(value = 8)
    private String purchaseNo;

    /** 备注 */
    @ExcelProperty(value = "备注", index = 24)
    @ColumnWidth(value = 8)
    private String remark;

    @ExcelProperty(value = "创建人", index = 25)
    @ColumnWidth(value = 10)
    private String creator;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 26, converter = LocalDateTimeConverter.class)
    @ColumnWidth(value = 15)
    private LocalDateTime createTime;

    /**
     * 收货时间
     */
    @ExcelProperty(value = "发货时间", index = 27, converter = LocalDateTimeConverter.class)
    @ColumnWidth(value = 15)
    private LocalDateTime deliveryTime;

    @ExcelProperty(value = "审核时间", index = 28, converter = LocalDateTimeConverter.class)
    @ColumnWidth(value = 15)
    private LocalDateTime auditTime;

}
