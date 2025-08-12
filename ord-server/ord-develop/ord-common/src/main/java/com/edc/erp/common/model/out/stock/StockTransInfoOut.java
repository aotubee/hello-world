/**
 * Copyright © 2010-2022 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.out.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 仓位翻译出参
 *
 * @author: gusiyuan
 * @date: 2022-07-25
 */
@Data
public class StockTransInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "仓位代码")
    private String stockCode;

    @ApiModelProperty(value = "仓位名称")
    private String stockName;

    @ApiModelProperty(value = "是否启用")
    private Integer isEnable;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

    @ApiModelProperty(value = "仓位id")
    private Integer id;
}
