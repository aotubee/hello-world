package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * 中科退货单入参
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-24 18:19
 */
@Data
public class ZKReturnOrderIn extends BaseEntity {

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
     * 退货原因
     */
    private String return_reason;

    /**
     * 备注
     */
    private String memo;

    private List<ZKReturnOrderDetailIn> detail_list;

}
