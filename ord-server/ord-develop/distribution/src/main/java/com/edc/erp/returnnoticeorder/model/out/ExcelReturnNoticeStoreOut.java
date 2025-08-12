package com.edc.erp.returnnoticeorder.model.out;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 导出退货通知单门店明细出参
 *
 * @author yaojinpeng
 * @since 2022/10/29 16:13
 */
@Data
public class ExcelReturnNoticeStoreOut {

    /**
     * 序号
     */
    @Excel(name = "序号")
    private Integer index;

    /**
     * 门店代码
     */
    @Excel(name = "门店代码", orderNum = "1", width = 15)
    private String storeCode;

    /**
     * 门店名称
     */
    @Excel(name = "门店名称", orderNum = "2", width = 25)
    private String storeName;


    /**
     * 可退数量
     */
    @Excel(name = "可退数量", orderNum = "3", width = 15)
    private BigDecimal qty;
}
