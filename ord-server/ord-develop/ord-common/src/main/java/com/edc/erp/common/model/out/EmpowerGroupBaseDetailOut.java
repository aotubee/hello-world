package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * description 数据组明细出参
 *
 * @author gusiyuan
 * @since 2024/11/13 18:41
 */
@Data
public class EmpowerGroupBaseDetailOut implements Serializable {

    private static final long serialVersionUID = 5078517379810807284L;

    @ApiModelProperty(value = "组明细名称")
    private String name;

    @ApiModelProperty(value = "组明细代码")
    private String code;
}
