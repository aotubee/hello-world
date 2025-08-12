/**
 * Copyright © 2010-2022 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.in.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 查询协议商品列表入参
 *
 * @author: gusiyuan
 * @date: 2022-09-01
 */
@Data
public class FindForPurGoodsIn implements Serializable {

    private static final long serialVersionUID = 8761238745883079332L;
    @ApiModelProperty(value = "商品代码集合", required = true)
    @NotNull(message = "商品代码集合不能为空")
    private List<String> goodsCodes;

    @ApiModelProperty(value = "业务组织代码")
    String bizOrgCode;

}
