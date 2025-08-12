package com.edc.erp.directly.dirdeliveryorder.model.out;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fxw
 * @description: 配货单明细出参
 * @since 2022/10/25 15:17
 */
@Data
@ApiModel(value = "OrdDirDeliveryDetailOut",description = "配货单明细出参")
public class OrdDirDeliveryDetailOut extends OrdDirDeliveryDetail implements Serializable {
    private static final long serialVersionUID = -2343584929512966421L;

    private String  goodsTypeStr;

    /**
     * 品类
     */
    @ApiModelProperty(name = "sortName", value = "品类名称")
    private String sortName;

    /** 配货方式 */
    @ApiModelProperty(name = "distributionType", value = "配货方式")
    private String distributionType;
    /**
     * 配送方式中文
     */
    @ApiModelProperty(name = "distributionTypeValue", value = "配送方式中文")
    private String distributionTypeValue;

    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;

}
