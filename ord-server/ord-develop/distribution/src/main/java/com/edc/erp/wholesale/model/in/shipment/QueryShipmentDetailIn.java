package com.edc.erp.wholesale.model.in.shipment;

import com.edc.erp.wholesale.model.out.shipment.ShipmentWithDetailOut;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 出货单明细入参类
 * @author lx
 * @since 2022-11-01 12:07:47
 */
@Data
public class QueryShipmentDetailIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 批发出货单id */
    @ApiModelProperty(name = "wholesaleShipmentId", value = "批发出货单id")
    private Long wholesaleShipmentId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /** 批发出货单 集合 */
    @ApiModelProperty(name = "shipmentWithDetailOuts", value = "批发出货单 集合")
    private List<ShipmentWithDetailOut> shipmentWithDetailOuts;
}
