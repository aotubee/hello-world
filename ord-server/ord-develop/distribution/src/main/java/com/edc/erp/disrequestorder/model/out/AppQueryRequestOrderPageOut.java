package com.edc.erp.disrequestorder.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Description
 *
 * @author :weichao
 */
@Data
public class AppQueryRequestOrderPageOut extends BaseEntity {

    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型主键")
    private Long orderTypeConfigId;

    @ApiModelProperty(name = "shortOrderType", value = "订单类型简称")
    private String shortOrderType;

    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private LocalDateTime truncationDateTime;

    @ApiModelProperty(name = "requestOrderId", value = "集货单主键")
    private Long requestOrderId;

    @ApiModelProperty(name = "requestOrderNo", value = "集货单单号")
    private String requestOrderNo;

    @ApiModelProperty(name = "statusCode", value = "集货单状态")
    private String statusCode;

    @ApiModelProperty(name = "statusCodeStr", value = "集货单状态中文")
    private String statusCodeStr;

    @ApiModelProperty(name = "totalAmount", value = "集货总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(name = "skuItemNumber", value = "商品品项数")
    private Integer goodsItemNumber;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;
}
