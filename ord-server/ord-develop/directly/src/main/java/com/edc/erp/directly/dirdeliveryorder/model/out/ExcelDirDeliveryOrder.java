package com.edc.erp.directly.dirdeliveryorder.model.out;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.edc.erp.common.util.BigDecimalAmountConverter;
import com.edc.erp.common.util.BigDecimalConverter;
import com.edc.erp.common.util.LocalDateTimeConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description: 配货单导出
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
public class ExcelDirDeliveryOrder {

    @ExcelProperty(value = "序号",index = 0)
    private Integer index;

    /** 配货单号 */
    @ExcelProperty(value = "配货单号", index = 1)
    @ColumnWidth(25)
    private String deliveryOrderNo;

    /** 配货单状态 */
    @ExcelProperty(value = "状态", index = 2)
    private String deliveryStatusValue;


    /** 红冲标识 */
    @ExcelProperty(value = "冲销标识", index = 3)
    private String  isReversal;

    /** 是否红冲单 */
    @ExcelProperty(value = "是否冲销单", index = 4)
    private String  isReversalOrder;



    /** 仓位*/
    @ExcelProperty(value = "仓位", index = 5)
    private String stockCode;

    /** 门店代码 */
    @ExcelProperty(value = "门店代码", index = 6)
    private String storeCode;

    /** 门店名称 */
    @ExcelProperty(value = "门店名称", index = 7)
    private String storeName;

    /** 配货方式 */
    @ExcelProperty(value = "配送方式", index = 8)
    private String distributionTypeValue;

    /** 商品品项数 */
    @ExcelProperty(value = "商品品项数", index = 9)
    private Integer skuCount;

    /** 要货数量 */
    @ExcelProperty(value = "要货数量",index = 10,converter = BigDecimalConverter.class)
    private BigDecimal orderQuantity;

    /** 要货金额 */
    @ExcelProperty(value = "要货金额",index = 11,converter = BigDecimalAmountConverter.class)
    private BigDecimal orderAmount;

    /** 配货数量 */
    @ExcelProperty(value = "配货数量", index = 12,converter = BigDecimalConverter.class)
    private BigDecimal distributionQuantity;

    /**配货金额 */
    @ExcelProperty(value = "配货金额", index = 13,converter = BigDecimalAmountConverter.class)
    private BigDecimal distributionAmount;

    /** 实配数量 */
    @ExcelProperty(value = "实配数量", index = 14,converter = BigDecimalConverter.class)
    private BigDecimal deliveryQuantity;

    /** 实配金额 */
    @ExcelProperty(value = "实配金额", index = 15,converter = BigDecimalAmountConverter.class)
    private BigDecimal deliveryAmount;

    /** 要货单号 */
    @ExcelProperty(value = "要货单号", index = 16)
    private String requestOrderNo;

    /** 差异单号 */
    @ExcelProperty(value = "差异单号", index = 17)
    private String differenceOrderNo;

    /** 物流单号 */
    @ExcelProperty(value = "物流单号",index = 18)
    private String logisticsNo;

    /** 来源单号 */
    @ExcelProperty(value = "来源单号", index = 19)
    private String sourceNo;

    /** 海鼎单号 */
    @ExcelProperty(value = "海鼎单号", index = 20)
    private String hdDeliveryNo;

    /**
     * 转单优先级
     */
    @ExcelProperty(value = "转单优先级", index = 21)
    private String orderPriority;


    /** 创建人 */
    @ExcelProperty(value = "创建人", index = 22)
    private String creator;

    /** 创建时间 */
    @ExcelProperty(value = "创建时间", index = 23,converter = LocalDateTimeConverter.class)
    private LocalDateTime createTime;

    /**
     * 发货时间
     */
    @ExcelProperty(value = "发货时间", index = 24,converter = LocalDateTimeConverter.class)
    private LocalDateTime deliveryTime;

    /** 备注 */
    @ExcelProperty(value = "备注", index = 25)
    private String remark;
}
