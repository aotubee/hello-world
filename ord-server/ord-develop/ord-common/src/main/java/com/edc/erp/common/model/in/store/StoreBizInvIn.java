/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.model.in.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 根据门店code+商品codes获取业务可用库存入参
 *
 * @author: zhaolei
 * @date: 2022-08-20
 */
@Data
public class StoreBizInvIn implements Serializable {

    private static final long serialVersionUID = 590367413578909856L;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码", required = true)
    @NotEmpty(message = "业务组织代码不能为空")
    private String bizOrgCode;

    /** 门店代码(erp) */
    @ApiModelProperty(name = "storeCode", value = "门店代码(erp)", required = true)
    @NotEmpty(message = "门店代码不能为空")
    private String storeCode;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCodes", value = "商品代码集合", required = true)
    @NotNull(message = "商品代码集合不能为空")
    private List<String> goodsCodes;
}
