/**
 * Copyright © 2010-2022 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 组织商品基础信息出参
 *
 * @author weichao
 */
@Data
public class OrgGoodsTransInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(value = "组织主条码")
    private String orgGoodsBar;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "商品主图")
    private String imgUrl;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

}
