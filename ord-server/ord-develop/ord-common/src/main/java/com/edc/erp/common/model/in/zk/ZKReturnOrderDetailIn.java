package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 中科退货单入参-明细
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-24 18:21
 */
@Data
public class ZKReturnOrderDetailIn extends BaseEntity {

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
     * 退货原因
     */
    private String return_reason;

    /**
     * 备注
     */
    private String memo;


    /**
     * 行号
     */
    private Integer line;
}
