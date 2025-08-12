package com.edc.erp.directly.dirfirstorder.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description:
 * @Author: ZhangYao
 * @Date: 2023/8/31 18:58
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdDirFirstOrderImportErrorResult implements Serializable {


    private static final long serialVersionUID = -5698828259525231268L;

    /**
     * 商品代码
     */
    @ExcelProperty(value = "商品代码", index = 0)
    @ColumnWidth(15)
    private String goodsCode;

    /**
     * 分货数量
     */
    @ExcelProperty(value = "分货数量", index = 1)
    @ColumnWidth(15)
    private Integer distributionNum;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 2)
    @ColumnWidth(50)
    private String errorMessage;


}
