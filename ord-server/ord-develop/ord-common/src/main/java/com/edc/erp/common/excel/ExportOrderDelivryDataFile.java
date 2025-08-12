package com.edc.erp.common.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 配销/配货错误结果文件
 */
@Data
public class ExportOrderDelivryDataFile implements Serializable {

    private static final long serialVersionUID = 7455878497961716082L;
    /**
     *处理状态
     */
    @Excel(name = "处理状态")
    private String status;
    /**
     * 结果说明
     */
    @Excel(name = "结果说明")
    private String remark;

    /**
     * 单号
     */
    @Excel(name = "单号")
    private String orderNo;

    /**
     * 行号
     */
    @Excel(name = "行号")
    private Integer lineNo;

    /**
     * 商品代码
     */
    @Excel(name = "商品代码")
    private String goodsCode;
    
    @Excel(name = "实配数量")
    private BigDecimal amount;

}

