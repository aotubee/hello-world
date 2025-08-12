package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * 中科要货单接口入参
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-24 17:46
 */
@Data
public class ZKDeliveryOrderIn extends BaseEntity {

    /**
     * 优店单号
     */
    private String source_order_no;

    /**
     * 库存类型
     */
    private String stock_type;

    /**
     * 仓库
     */
    private String branch_no;

    /**
     * 要货门店
     */
    private String d_branch_no;

    /**
     * 制单人
     */
    private String oper_id;


    /**
     * 配送方式
     */
    private String delivery_type;

    /**
     * 备注
     */
    private String memo;


    /**
     * 订单明细
     */
    private List<ZKDeliveryOrderDetailIn> detail_list;
}
