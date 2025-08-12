package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author wei
 */
@Data
public class DeliveryIn {

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    @NotBlank(message = "商品代码不能为空")
    private String goodsCode;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    @NotBlank(message = "门店代码不能为空")
    private String storeCode;

    @ApiModelProperty(name = "stockCode", value = "仓位")
    @NotBlank(message = "仓位代码不能为空")
    private String stockCode;

    @ApiModelProperty(name = "wrhCode", value = "仓储")
    @NotBlank(message = "仓储代码不能为空")
    private String wrhCode;

    private String bizOrgCode;

    /**
     * 配货方式
     */
    @ApiModelProperty(name = "distributionType", value = "配货方式")
    @NotBlank(message = "配货方式不能为空")
    private String distributionType;

}
