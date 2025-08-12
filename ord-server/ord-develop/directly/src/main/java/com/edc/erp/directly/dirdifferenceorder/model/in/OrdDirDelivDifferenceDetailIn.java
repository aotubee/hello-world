package com.edc.erp.directly.dirdifferenceorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 直营配货差异单明细入参
 * @author weichao
 *
 */
@Data
public class OrdDirDelivDifferenceDetailIn extends Page implements Serializable {

    @ApiModelProperty(name = "diffOrderId", value = "差异单Id")
    @NotNull(message = "差异单Id")
    private Integer diffOrderId;
    @ApiModelProperty(name = "goodsCode", value = "goodsCode")
    private String goodsCode;
    @ApiModelProperty(name = "goodsName", value = "goodsName")
    private String goodsName;

}
