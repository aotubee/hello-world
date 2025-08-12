package com.edc.erp.distribution.model.in;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 单店导入分货入参
 *
 * @author yaojinpeng
 * @since 2022/10/19 12:10
 */
@Data
public class ImportStoreGoodsIn extends BaseEntity {

    /**
     * 商品sku
     */
    private String skuCode;


    /**
     * 分货数量
     */
    private BigDecimal distributionQuantity;
}
