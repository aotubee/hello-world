package com.edc.erp.disdifferenceorder.model.out;

import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 配销差异单详情出参
 * @author weichao
 */
@Data
public class OrdDisDelivDifferenceDetailOut extends OrdDisDelivDifferenceDetail {
    /** 品类属性中文 */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文")
    private String goodsTypeStr;

    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;
}
