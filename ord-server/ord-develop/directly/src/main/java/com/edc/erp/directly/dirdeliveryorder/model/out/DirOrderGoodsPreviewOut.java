package com.edc.erp.directly.dirdeliveryorder.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单追踪-商品预览图
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-05-19 11:41
 */
@Data
public class DirOrderGoodsPreviewOut extends BaseEntity {

    @ApiModelProperty(name = "name", value = "商品名称")
    private String name;

    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;

}
