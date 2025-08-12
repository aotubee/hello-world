package com.edc.erp.common.model.out.goodsdistributionplan;

import com.edc.erp.common.model.entity.GoodsDistributionPlanGroupDetails;
import lombok.Data;

import java.io.Serializable;

/**
* @return: 商品配送方案订单方出参
* @Author: fxw
* @Date: 2022/11/23
*/
@Data
public class GoodsDistributionPlanGroupDetailsVendorOut extends GoodsDistributionPlanGroupDetails implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 订单方id
     */
    private Integer vendorId;
}
