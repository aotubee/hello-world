/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.common.model.out.store;


import com.edc.erp.common.model.entity.StoreInfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author
 */
@Data
public class StoreAndClientInfoOut extends StoreInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    @ApiModelProperty(name = "clientName", value = "客户名称")
    private String clientName;
}