package com.edc.erp.directly.returnorder.model.excel;

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
public class OrdDirReturnImportErrorResult implements Serializable {


    private static final long serialVersionUID = -3962567991216489833L;

    /**
     * 门店代码
     */
    @ExcelProperty(value = "仓储", index = 0)
    @ColumnWidth(15)
    private String warehouseCode;


    /**
     * 门店代码
     */
    @ExcelProperty(value = "仓位", index = 1)
    @ColumnWidth(15)
    private String stockCode;

    /**
     * 门店代码
     */
    @ExcelProperty(value = "门店代码", index = 2)
    @ColumnWidth(15)
    private String storeCode;

    /**
     * 商品代码
     */
    @ExcelProperty(value = "商品代码", index = 3)
    @ColumnWidth(15)
    private String goodsCode;

    /**
     * 分货数量
     */
    @ExcelProperty(value = "申请退货数量", index = 4)
    @ColumnWidth(15)
    private BigDecimal applyReturnQuantity;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 5)
    @ColumnWidth(30)
    private String remark;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 6)
    @ColumnWidth(50)
    private String errorMessage;


}
