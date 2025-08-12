package com.edc.erp.distribution.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-23 11:47
 */
@Data
public class AppTopDisOrderGoodsOut extends BaseEntity {

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    @ApiModelProperty(name = "goodsImage", value = "商品图片")
    private String goodsImage;
}
