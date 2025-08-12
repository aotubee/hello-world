/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.directly.dirdeliveryorder.model.out;

import com.edc.erp.common.util.BigDecimalSerialize;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.reducestock.common.util.BigDecimalSerialize2;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author weichao
 *
 */
@Data
public class DirDeliveryOrderOut extends OrdDirDelivery implements Serializable {

    /**
     * 仓位中文值
     */
    @ApiModelProperty(name = "stockName", value = "仓位中文值")
    private String stockName;

    /** 仓储名称 */
    @ApiModelProperty(name = "wrhCodeName", value = "仓储名称")
    private String wrhCodeName;

    /**
     * 配送方式中文
     */
    @ApiModelProperty(name = "distributionTypeValue", value = "配送方式中文")
    private String distributionTypeValue;
    /**
     * 配货单状态中文值
     */
    @ApiModelProperty(name = "deliveryStatusValue", value = "配货单状态中文值")
    private String deliveryStatusValue;

    @ApiModelProperty(name = "isCanMatchOrder", value = "是否能匹配订货订单")
    private Integer isCanMatchOrder;

    @ApiModelProperty(name = "isCanReleaseOrderAmount", value = "是否能释放订货订单金额")
    private Integer isCanReleaseOrderAmount;

    @ApiModelProperty(name = "ordDirDeliveryDetailList", value = "配货单明细")
    private List<OrdDirDeliveryDetail> ordDirDeliveryDetailList;

    @ApiModelProperty(name = "shortOrderType", value = "订单类型简称")
    private String shortOrderType;


    @ApiModelProperty(name = "isSigning", value = "是否已签收，1:是,0:否")
    private Integer isSigning;


    @ApiModelProperty(name = "isCanTake", value = "是否能收货，1:是,0:否")
    private Integer isCanTake;

    @ApiModelProperty(name = "totalArrivalQuantity",value = "总实收数量")
    private BigDecimal totalArrivalQuantity;

    @ApiModelProperty(name = "totalArrivalAmount",value = "总实收金额")
    private BigDecimal totalArrivalAmount;

    @ApiModelProperty(value = "红冲单ID")
    private Long reversalOrderId;

    @ApiModelProperty(value = "红冲单单号")
    private String reversalOrderNo;

    @ApiModelProperty(value = "原单ID")
    private Long sourceOrderId;
}
