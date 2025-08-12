package com.edc.erp.directly.distribution.model.in;

import com.alibaba.excel.annotation.ExcelProperty;
import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店分货导入接受参数VO
 *
 * @author yaojinpeng
 * @since 2022/10/19 15:22
 */
@Data
public class ImportOrdOrderStoreGoodsVO extends BaseEntity {

    /**
     * 门店代码
     */
    @ExcelProperty(value = "门店代码",index = 0)
    private String storeCode;

    /**
     * 商品sku
     */
    @ExcelProperty(value = "商品sku",index = 1)
    private String skuCode;

    /**
     * 分货数量
     */
    @ExcelProperty(value = "分货数量",index = 2)
    private BigDecimal distributionQuantity;

}
