/**
 * Copyright © 2010-2022 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.out.purchase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单方信息（翻译用）
 *
 * @author: gusiyuan
 * @date: 2022-10-12
 */
@Data
public class VendorTransInfoVO {

    @ApiModelProperty(value = "订单方id")
    private Integer vendorId;

    /**
     * 订单方代码
     */
    @ApiModelProperty(value = "订单方代码")
    private String vendorCode;
    /**
     * 订单方名称
     */
    @ApiModelProperty(value = "订单方名称")
    private String vendorName;

}
