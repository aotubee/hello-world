package com.edc.erp.directly.distribution.model.out;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 直营分货门店商品导出类
 *
 * @author lixuejun
 */
@Data
public class ExportOrdDirOrderDistributionDetail {

    /**
     * 门店代码
     */
    @Excel(name = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @Excel(name = "门店名称", orderNum = "2", width = 30)
    private String storeName;

    /**
     * 商品代码
     */
    @Excel(name = "商品代码", orderNum = "3", width = 15)
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称", orderNum = "4", width = 30)
    private String goodsName;

    /**
     * 配货规格
     */
    @Excel(name = "配货规格", orderNum = "5", width = 15)
    private String distributionSpecification;

    /**
     * 当前库存
     */
    @Excel(name = "当前库存", orderNum = "6", width = 15)
    private BigDecimal wrhInvQty;

    /**
     * 直营分货包装数
     */
    @Excel(name = "直营分货包装数", orderNum = "7", width = 15)
    private BigDecimal packingNumber;

    /**
     * 直营分货数量
     */
    @Excel(name = "直营分货数量", orderNum = "8", width = 15)
    private BigDecimal distributionQuantity;

    /**
     * 直营分货金额
     */
    @Excel(name = "直营分货金额", orderNum = "9", width = 15)
    private BigDecimal distributionAmount;

    /**
     * 直营价
     */
    @Excel(name = "直营价", orderNum = "10", width = 15)
    private BigDecimal originalPrice;

}
