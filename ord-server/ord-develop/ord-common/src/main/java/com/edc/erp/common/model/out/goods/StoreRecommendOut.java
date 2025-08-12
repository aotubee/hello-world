/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * PpGoodsTagOut返回类
 *
 * @author liwenqiang
 */
@Data
public class StoreRecommendOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签代码
     */
    @ApiModelProperty(name = "tagCode", value = "标签代码")
    private String tagCode;

    /**
     * 标签名称
     */
    @ApiModelProperty(name = "tagName", value = "标签名称")
    private String tagName;

    /**
     * 标签类型
     */
    @ApiModelProperty(name = "type", value = "标签类型：0-属性标签，1-活动标签")
    private Integer type;

}