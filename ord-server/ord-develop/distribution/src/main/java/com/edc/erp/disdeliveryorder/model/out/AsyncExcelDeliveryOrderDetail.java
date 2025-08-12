package com.edc.erp.disdeliveryorder.model.out;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.edc.erp.common.util.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加盟配销明细导出类
 *
 * @author lishaobo
 */
@Data
public class AsyncExcelDeliveryOrderDetail implements Serializable {

    private static final long serialVersionUID = -3751676153574360208L;

    @ExcelProperty(value = {"配销单抬头信息","单号"}, index = 0)
    @ColumnWidth(20)
    private String deliveryOrderNo;

    /** 配货单状态 */
    @ExcelProperty(value = {"配销单抬头信息","状态"}, index = 1)
    private String deliveryStatusValue;

//    @ExcelProperty(value = {"配货单抬头信息","组织信息"}, index = 1)
//    private String bizOrgCode;

    @ExcelProperty(value = {"配销单抬头信息","仓储"}, index = 2)
    private String wrhCode;

    @ExcelProperty(value = {"配销单抬头信息","仓位"}, index = 3)
    private String stockCode;

    @ExcelProperty(value = {"配销单抬头信息","配送方式"}, index = 4)
    private String distributionTypeValue;

    @ExcelProperty(value = {"配销单抬头信息","门店代码"}, index = 5)
    private String storeCode;

    @ExcelProperty(value = {"配销单抬头信息","门店名称"}, index = 6)
    @ColumnWidth(25)
    private String storeName;

    @ExcelProperty(value = {"配销单商品明细信息","商品代码"}, index = 7)
    private String goodsCode;

    @ExcelProperty(value = {"配销单商品明细信息","商品名称"}, index = 8)
    @ColumnWidth(25)
    private String goodsName;

    @ExcelProperty(value = {"配销单商品明细信息","包装规格"}, index = 9)
    private String distributionSpecification;

    @ExcelProperty(value = {"配销单商品明细信息","要货数量"}, index = 10)
    private BigDecimal orderQuantity;

    @ExcelProperty(value = {"配销单商品明细信息","要货单价"}, index = 11)
    private BigDecimal orderUnitPrice;

    /** 要货金额 */
    @ExcelProperty(value = {"配销单商品明细信息","要货金额"}, index = 12)
    private BigDecimal orderAmount;


    @ExcelProperty(value = {"配销单商品明细信息","配货数量"}, index = 13)
    private BigDecimal distributionQuantity;


    /** 配货单价 */
    @ExcelProperty(value = {"配销单商品明细信息","配货单价"}, index = 14)
    private BigDecimal distributionUnitPrice;

    @ExcelProperty(value = {"配销单商品明细信息","配货金额"}, index = 15)
    private BigDecimal distributionAmount;

    @ExcelProperty(value = {"配销单商品明细信息","实配数量"}, index = 16)
    private BigDecimal deliveryQuantity;

    @ExcelProperty(value = {"配销单商品明细信息","实配金额"}, index = 17)
    private BigDecimal deliveryAmount;


    /** 实收数量 */
    @ExcelProperty(value = {"配销单商品明细信息","实收数量"}, index = 18)
    private BigDecimal arrivalQuantity;

    /** 实收金额 */
    @ExcelProperty(value = {"配销单商品明细信息","实收金额"}, index = 19)
    private BigDecimal arrivalAmount;

    @ExcelProperty(value = {"配销单商品明细信息","订单方代码"}, index = 20)
    private String vendorCode;

    /** 是否赠品 */
    @ExcelProperty(value = {"配销单商品明细信息","是否赠品"}, index = 21)
    private String isGiftStr;

    /** 采购单号 */
    @ExcelProperty(value = {"配销单商品明细信息","采购单号"}, index = 22)
    private String purchaseNo;

    @ExcelProperty(value = {"配销单商品明细信息","效期"}, index = 23)
    private String expiry;

    @ExcelProperty(value = {"配销单商品明细信息","发票类型"}, index = 24)
    private String invoiceTypeStr;

    @ExcelProperty(value = {"配销单商品明细信息","创建人"}, index = 25)
    @ColumnWidth(15)
    private String creator;

    @ExcelProperty(value = {"配销单商品明细信息","创建时间"}, index = 26, converter = LocalDateTimeConverter.class)
    @ColumnWidth(18)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;



    @ExcelIgnore
    private String deliveryStatusCode;

    @ExcelIgnore
    private String distributionType;


    @ExcelIgnore
    private Integer isGift;

    @ExcelIgnore
    private String invoiceType;

}
