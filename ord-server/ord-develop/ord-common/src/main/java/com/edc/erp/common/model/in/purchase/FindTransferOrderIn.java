package com.edc.erp.common.model.in.purchase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author LZQ
 * @date 2023年01月11日 17:05
 * 查询中转发送采购的配销、配货单 入参
 */
@Data
@Builder
public class FindTransferOrderIn implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单配送方式
     */
    @ApiModelProperty(name = "distributionType", value = "订单配送方式")
    private String distributionType;

    /**
     * 订单状态
     */
    @ApiModelProperty(name = "deliveryStatusCode", value = "订单状态")
    private String deliveryStatusCode;

    /**
     * 集货单、要货单截止时间
     */
    @ApiModelProperty(name = "truncationDateTime", value = "集货单、要货单截止时间")
    private List<String> truncationDateTime;

    /**
     * 业务组织代码
     *
     */
    @ApiModelProperty(name = "bizOrgCode",value = "业务组织代码")
    private String bizOrgCode;
}
