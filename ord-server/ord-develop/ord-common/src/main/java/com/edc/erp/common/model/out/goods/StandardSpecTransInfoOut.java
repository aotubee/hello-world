/**
 * Copyright © 2010-2022 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 规格翻译用信息
 *
 * @author: gusiyuan
 * @date: 2022-07-22
 */
@Data
public class StandardSpecTransInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "规格说明")
    private String qpcStr;

    @ApiModelProperty(value = "规格数量")
    private Integer qpc;

    @ApiModelProperty(value = "单位ID")
    private Integer unitId;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

}
