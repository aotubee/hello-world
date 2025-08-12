package com.edc.erp.wholesale.model.out.shipment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName TransferShipmentPushPurchaseOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/13 9:47
 **/
@Data
public class TransferShipmentPushPurchaseOut implements Serializable {
    private static final long serialVersionUID = 1550027346263262829L;

    /** 批发出货单id */
    @ApiModelProperty(name = "wholesaleShipmentId", value = "批发出货单id")
    private Long wholesaleShipmentId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

    /** 审核数量 */
    @ApiModelProperty(name = "auditQuantity", value = "审核数量")
    private Integer auditQuantity;

    @ApiModelProperty(value = "stockCode", name = "仓位代码")
    private String stockCode;

    @ApiModelProperty(value = "warehouseCode", name = "仓储代码")
    private String warehouseCode;

    @ApiModelProperty(value = "applyQuantity", name = "申请数")
    private String applyQuantity;

    @ApiModelProperty(value = "distributionType", name = "配送方式")
    private String distributionType;

    @ApiModelProperty(value = "line", name = "行号")
    private Integer line;

    @ApiModelProperty(value = "订单优先级")
    private String orderPriority;
}
