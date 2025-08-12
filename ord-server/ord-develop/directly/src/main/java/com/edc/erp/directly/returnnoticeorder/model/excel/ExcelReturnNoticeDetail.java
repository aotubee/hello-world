package com.edc.erp.directly.returnnoticeorder.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退货通知单商品明细导出
 *
 * @author yaojinpeng
 * @since 2022/10/29 14:51
 */

@Data
public class ExcelReturnNoticeDetail {

    /**
     * 序号
     */
    @Excel(name = "序号")
    private Integer index;

    /**
     * 商品代码
     */
    @Excel(name = "商品代码", orderNum = "1", width = 15)
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称",orderNum = "2", width = 25)
    private String goodsName;


    /**
     * 配货规格
     */
    @Excel(name = "配货规格", orderNum = "3", width = 15)
    private String specification;

    /**
     * 商品小分类
     */
    @Excel(name = "商品小分类", orderNum = "4", width = 15)
    private String sortName;

    /**
     * 门店代码
     */
    @Excel(name = "门店代码", orderNum = "5", width = 15)
    private String storeCode;

    /**
     * 门店名称
     */
    @Excel(name = "门店名称", orderNum = "6", width = 15)
    private String storeName;

    /**
     * 可退数量
     */
    @Excel(name = "可退数量", orderNum = "7", width = 15, format = "0.0000")
    private BigDecimal returnQty;

}
