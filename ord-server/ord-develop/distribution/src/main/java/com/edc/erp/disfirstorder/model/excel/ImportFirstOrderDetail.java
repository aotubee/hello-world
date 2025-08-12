/**
 * Copyright © 2010-2021 Everyday Chain. All rights reserved.
 */
package com.edc.erp.disfirstorder.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 铺货单导入明细
 *
 * @author weichao
 */
@Data
public class ImportFirstOrderDetail {

    private Long detailId;

    /**
     * 商品code
     */
    @ExcelProperty(value = "商品代码",index = 0)
    private String goodsCode;

    /**
     * 铺货数量
     */
    @ExcelProperty(value = "铺货数量",index = 1)
    private Integer distributionNum;

}
