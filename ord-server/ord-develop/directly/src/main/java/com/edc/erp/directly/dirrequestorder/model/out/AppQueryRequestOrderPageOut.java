package com.edc.erp.directly.dirrequestorder.model.out;

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

    @ApiModelProperty(name = "requestOrderId", value = "要货单主键")
    private Long requestOrderId;

    @ApiModelProperty(name = "requestOrderNo", value = "要货单单号")
    private String requestOrderNo;

    @ApiModelProperty(name = "statusCode", value = "要货单状态")
    private String statusCode;

    @ApiModelProperty(name = "statusCodeStr", value = "要货单状态中文")
    private String statusCodeStr;

    @ApiModelProperty(name = "totalAmount", value = "要货总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(name = "skuItemNumber", value = "商品品项数")
    private Integer goodsItemNumber;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;
}
