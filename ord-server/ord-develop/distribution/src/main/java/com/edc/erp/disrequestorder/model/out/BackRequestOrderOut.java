package com.edc.erp.disrequestorder.model.out;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.distribution.entity.OrdDisOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description
 *
 * @author weichao
 */
@Data
public class BackRequestOrderOut extends AppQueryRequestOrderPageOut {

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

    @ApiModelProperty(name = "deliveryOrderNos", value = "配货单号")
    private String deliveryOrderNos;

    @ApiModelProperty(name = "orderNos", value = "订货单号")
    private String orderNos;

    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "skuNumber", value = "商品总数量")
    private BigDecimal skuNumber;

    @ApiModelProperty(name = "orderTypeCode", value = "订单类型代码")
    private String orderTypeCode;

    @ApiModelProperty(name = "orderTypeName", value = "订单类型名称")
    private String orderTypeName;
    @ApiModelProperty(name = "orderDetailOutList", value = "要货单明细集合")
    private List<BackRequestOrderDetailOut> orderDetailOutList;

    @ApiModelProperty(name = "ordDisOrders", value = "订货单集合")
    private List<OrdDisOrder> ordDisOrders;

    @ApiModelProperty(name = "ordDisDeliveries", value = "配货单集合集合")
    private List<OrdDisDelivery> ordDisDeliveries;

}
