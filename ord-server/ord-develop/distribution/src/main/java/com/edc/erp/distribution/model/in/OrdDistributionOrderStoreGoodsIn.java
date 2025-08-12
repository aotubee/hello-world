package com.edc.erp.distribution.model.in;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分货导入后转化为待入库对象
 *
 * @author yaojinpeng
 * @since 2022/10/19 15:21
 */
@Data
public class OrdDistributionOrderStoreGoodsIn extends ImportOrdDistributionStoreGoodsVO{

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
    private String orgCode;

    /**
     * 分货包装数量
     */
    private BigDecimal packingNumber;

    /** 规格 */
    private String distributionSpecification;

    /** 当前库存数量 */
    private BigDecimal wrhInvQty;
}
