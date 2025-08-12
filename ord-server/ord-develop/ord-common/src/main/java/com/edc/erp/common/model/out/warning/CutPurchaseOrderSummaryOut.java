package com.edc.erp.common.model.out.warning;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-02-25 16:44
 */
@Data
public class CutPurchaseOrderSummaryOut extends BaseEntity {

    /**
     * 商品总数量（散件）
     */
    private BigDecimal totalQuantity;

    /**
     * 商品总包装数（整件）
     */
    private BigDecimal totalPackageQuantity;

    /**
     * 总门店数
     */
    private Integer totalStoreQuantity;

    /**
     * 总付款金额
     */
    private BigDecimal totalPayAmount;

    /**
     * 消息头
     */
    private String titleOrgName;
}
