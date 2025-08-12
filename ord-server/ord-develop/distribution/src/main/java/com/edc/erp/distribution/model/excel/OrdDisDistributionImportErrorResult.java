package com.edc.erp.distribution.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description:
 * @Author: ZhangYao
 * @Date: 2023/8/31 18:58
 **/
@Data
public class OrdDisDistributionImportErrorResult implements Serializable {


    private static final long serialVersionUID = -6956812842102637719L;
    /**
     * 门店代码
     */
    @ExcelProperty(value = "门店代码", index = 0)
    @ColumnWidth(15)
    private String storeCode;

    /**
     * 商品代码
     */
    @ExcelProperty(value = "商品代码", index = 1)
    @ColumnWidth(15)
    private String goodsCode;

    /**
     * 分货数量
     */
    @ExcelProperty(value = "分货数量", index = 2)
    @ColumnWidth(15)
    private BigDecimal distributionQuantity;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 3)
    @ColumnWidth(50)
    private String errorMessage;


}
