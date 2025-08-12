package com.edc.erp.directly.dirfirstorder.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 铺货单明细导出模板
 * @author weichao
 */
@Data
public class ExportFirstOrderDetail {

    @Excel(name = "序号")
    private Integer index;

    @Excel(name = "商品代码", orderNum = "1")
    private String goodsCode;

    @Excel(name = "商品名称", orderNum = "2", width = 25)
    private String goodsName;

    @Excel(name = "商品条码", orderNum = "3")
    private String barCode;

    @Excel(name = "配货规格", orderNum = "4")
    private String qpcStr;

    @Excel(name = "配货价", orderNum = "5", numFormat = "#.####")
    private BigDecimal distributionPrice;

    @Excel(name = "铺货包装数", orderNum = "6", numFormat = "#.##")
    private BigDecimal distributionPackageNum;

    @Excel(name = "铺货数量", orderNum = "7", numFormat = "0")
    private Integer num;

    @Excel(name = "铺货金额", orderNum = "8", numFormat = "#.####")
    private BigDecimal amount;

    @Excel(name = "仓位", orderNum = "9", width = 25)
    private String warehouseStr;

    @Excel(name = "小分类", orderNum = "10", width = 25)
    private String sortStr;
}
