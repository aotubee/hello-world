package com.edc.erp.disdeliveryorder.model.out;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fxw
 * @description: 配销单明细出参
 * @since 2022/10/25 15:17
 */
@Data
@ApiModel(value = "OrdDisDeliveryDetailOut",description = "配销单明细出参")
public class OrdDisDeliveryDetailOut extends OrdDisDeliveryDetail implements Serializable {
    private static final long serialVersionUID = -2343584929512966421L;

    private String  goodsTypeStr;

    /**
     * 品类
     */
    @ApiModelProperty(name = "sortName", value = "品类名称")
    private String sortName;

    /** 配销方式 */
    @ApiModelProperty(name = "distributionType", value = "配销方式")
    private String distributionType;

    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;
}
