package com.edc.erp.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 直营配货明细导出类
 *
 * @author weichao
 */
@Data
public class ExcelPresaleOrderOut implements Serializable {

    private static final long serialVersionUID = -6813391333434401115L;
    @Excel(name = "序号", width = 5)
    private Integer index;
    @Excel(name = "所属区域", orderNum = "1", width = 10)
    private String belongArea;
    @Excel(name = "预售订单单号", orderNum = "2", width = 20)
    private String presaleOrderNo;
    @Excel(name = "状态", orderNum = "3", width = 10)
    private String statusStr;
    @Excel(name = "门店代码", orderNum = "4", width = 10)
    private String storeCode;
    @Excel(name = "门店名称", orderNum = "5", width = 25)
    private String storeName;
    @ExcelProperty(value = "品项数")
    @Excel(name = "品项数", orderNum = "6", width = 10)
    private Integer totalSkuQty;
    @Excel(name = "商品数", orderNum = "7", width = 10)
    private BigDecimal totalGoodsQty;
    @Excel(name = "订单金额", orderNum = "8", width = 10)
    private BigDecimal totalOrderAmount;
    @Excel(name = "优惠金额", orderNum = "9", width = 10)
    private BigDecimal totalDiscountAmount;
    @Excel(name = "应付金额", orderNum = "10", width = 10)
    private BigDecimal totalPayAmount;
    /**
     * 活动单号
     */
//    @ApiModelProperty(name = "presaleActivityNo", value = "活动单号")
//    private String presaleActivityNo;

//    /**
//     * 支付时间
//     */
//    @ExcelProperty(value = "支付时间")
//    private LocalDateTime payTime;
    @Excel(name = "创建人", orderNum = "11", width = 10)
    private String creator;
    @Excel(name = "创建时间", orderNum = "12", width = 20)
    private String createTimeStr;
}
