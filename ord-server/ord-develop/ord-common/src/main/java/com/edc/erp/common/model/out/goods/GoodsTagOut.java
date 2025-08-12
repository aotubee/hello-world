package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fxw
 * @description: 商品标签出参
 * @since 2022/10/19 18:31
 */
@Data
public class GoodsTagOut implements Serializable {
    private static final long serialVersionUID = -4343008017448234065L;

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
