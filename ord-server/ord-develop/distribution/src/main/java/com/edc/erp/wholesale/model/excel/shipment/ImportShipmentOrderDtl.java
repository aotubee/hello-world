package com.edc.erp.wholesale.model.excel.shipment;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 批发出货单导入类
 * @author lx
 * @since 2022-11-03 17:40:42
 */
@Data
public class ImportShipmentOrderDtl implements Serializable {

    private static final long serialVersionUID = 1L;


    /** 商品代码 */
    @ExcelProperty(value = "商品代码",index = 0)
    private String goodsCode;

    /** 申请数量 */
    @ExcelProperty(value = "申请批发数量",index = 1)
    private Integer applyQuantity;

    @ExcelProperty(value = "单价", index = 2)
    private BigDecimal unitPrice;
}
