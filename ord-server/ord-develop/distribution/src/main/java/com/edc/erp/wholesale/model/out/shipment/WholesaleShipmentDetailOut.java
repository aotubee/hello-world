package com.edc.erp.wholesale.model.out.shipment;

import com.edc.erp.common.model.out.goods.StandardSpecOut;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 批发出货单明细结果集
 * @author lx
 * @since 2022-10-26 16:53:59
 */
@Data
public class WholesaleShipmentDetailOut extends WholesaleShipmentDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 品类属性 - 中文 */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性 - 中文")
    private String goodsTypeStr;

    @ApiModelProperty(name = "businessQty",value = "业务可用库存")
    private BigDecimal businessQty;

    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;

    @ApiModelProperty(name = "standardSpecs", value = "规格集合")
    private List<StandardSpecOut> standardSpecs;
}
