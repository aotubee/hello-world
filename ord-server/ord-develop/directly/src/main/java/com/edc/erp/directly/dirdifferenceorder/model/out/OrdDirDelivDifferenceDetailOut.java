package com.edc.erp.directly.dirdifferenceorder.model.out;


import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 直营配货差异单详情出参
 * @author weichao
 */
@Data
public class OrdDirDelivDifferenceDetailOut extends OrdDirDelivDifferenceDetail {

    /** 品类属性中文 */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文")
    private String goodsTypeStr;

    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;
}
