/**
 * Copyright © 2010-2022 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 库存业务原因翻译出参
 *
 * @author: lee
 * @date: 2022-08-08
 */
@Data
public class InvBizRsnTransOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务原因名称 */
    @ApiModelProperty(name = "businessReasonName", value = "业务原因名称")
    private String businessReasonName;

    /** 业务原因代码 */
    @ApiModelProperty(name = "businessReasonCode", value = "业务原因代码")
    private String businessReasonCode;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

}
