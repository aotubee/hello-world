package com.edc.erp.common.model.in.purchase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 查询订单方翻译入参
 */
@Data
public class FindVendorTransIn {

    @ApiModelProperty(value = "订单方代码集合")
    private List<String> vendorCodes;

    @ApiModelProperty(value = "业务组织编码")
    private String bizOrgCode;
}
