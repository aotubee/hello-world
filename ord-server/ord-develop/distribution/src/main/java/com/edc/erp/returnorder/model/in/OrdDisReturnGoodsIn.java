package com.edc.erp.returnorder.model.in;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退货导入后转化对象
 *
 * @author yaojinpeng
 * @since 2022/10/26 17:42
 */
@Data
public class OrdDisReturnGoodsIn extends ImportOrdReturnGoodsVO{
    /**
     * 门店主键
     */
    private Integer storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品原价（配送价）
     */
    private BigDecimal originalPrice;

    /**
     * 分货数量
     */
    private BigDecimal distributionQuantity;

    /**
     * 仓位代码
     */
    private String storageCode;

    /**
     * 仓位名称
     */
    private String storageName;

    /**
     * 第几行
     */
    private String rowIndex;

    /**
     * 组织代码
     */
    private String bizOrgCode;

    /**
     * 分货包装数量
     */
    private BigDecimal distributionPackageQuantity;
}
