package com.edc.erp.wholesale.model.excel.returns;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 导入批发退货单详情实体类
 *
 * @author wanglidong
 * @since 2022/11/3 11:30
 */
@Data
public class ImportWholesaleReturnDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "商品代码", index = 0)
    private String goodsCode;

    @ExcelProperty(value = "申请数量", index = 1)
    private Integer applyQuantity;

    @ExcelProperty(value = "单价", index = 2)
    private BigDecimal unitPrice;
}
