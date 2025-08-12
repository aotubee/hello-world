package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-07-14 09:50
 */
@Data
public class HandleDeliveryOrderAuditIn extends BaseEntity {

    @ApiModelProperty(name = "deliveryOrderNo", required = true, value = "配货单号")
    private String deliveryOrderNo;

    @ApiModelProperty(name = "auditResult", required = true, value = "审核结果,0=审核通过；1=审核未通过")
    private Integer auditResult;

    @ApiModelProperty(name = "auditTime", required = true, value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(name = "orgCode", required = true, value = "组织代码")
    private String orgCode;

    @ApiModelProperty(name = "handleDeliveryOrderDetailAuditList", required = true, value = "商品明细集合")
    private List<HandleDeliveryOrderDetailAuditIn> handleDeliveryOrderDetailAuditList;
}
