package com.edc.erp.disdifferenceorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 配销差异单明细入参
 * @author weichao
 *
 */
@Data
public class OrdDisDelivDifferenceDetailIn extends Page implements Serializable {

    @ApiModelProperty(name = "diffOrderId", value = "差异单Id")
    @NotNull(message = "差异单Id")
    private Integer diffOrderId;
    @ApiModelProperty(name = "goodsCode", value = "goodsCode")
    private String goodsCode;
    @ApiModelProperty(name = "goodsName", value = "goodsName")
    private String goodsName;

}
