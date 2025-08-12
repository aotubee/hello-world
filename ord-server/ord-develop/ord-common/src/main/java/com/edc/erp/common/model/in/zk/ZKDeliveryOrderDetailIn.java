package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 中科要货单接口入参-明细
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-24 18:02
 */
@Data
public class ZKDeliveryOrderDetailIn extends BaseEntity {

    /**
     * 优店单号
     */
    private String source_order_no;

    /**
     * 货号
     */
    private String item_no;

    /**
     * 数量
     */
    private BigDecimal real_qty;

    /**
     * 价格
     */
    private BigDecimal valid_price;

    /**
     * 金额
     */
    private BigDecimal sub_amt;

    /**
     * 备注
     */
    private String memo;

    /**
     * 行号
     */
    private Integer line;
}
