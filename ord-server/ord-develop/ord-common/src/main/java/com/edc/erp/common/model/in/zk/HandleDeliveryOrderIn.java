package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配货单回传通知
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-24 15:38
 */
@Data
public class HandleDeliveryOrderIn extends BaseEntity {

    @ApiModelProperty(name = "deliveryOrderNo",required = true, value = "配货单号")
    private String deliveryOrderNo;

    @ApiModelProperty(name = "handleResult",required = true, value = "处理结果,0=出库发货；1=整单缺货")
    private Integer handleResult;

    @ApiModelProperty(name = "deliveryOrderNo",required = true, value = "处理时间")
    private LocalDateTime handleTime;

    @ApiModelProperty(name = "orgCode",required = true, value = "组织代码")
    private String orgCode;

    @ApiModelProperty(name = "goodsDetailList", required = true,value = "商品明细集合")
    private List<HandleDeliveryOrderDetailIn> goodsDetailList;
}
