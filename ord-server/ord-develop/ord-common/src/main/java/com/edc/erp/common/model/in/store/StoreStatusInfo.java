/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.in.store;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *  门店业务查询入参
 *
 * @author weichao
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreStatusInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 门店类型
     */
    @ApiModelProperty(name = "storeProperty", value = "门店类型")
    private String storeProperty;

    /**
     * 业务类型
     */
    @ApiModelProperty(name = "businessType", value = "业务类型")
    private String businessType;
}
