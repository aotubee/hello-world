package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * description 授权组
 *
 * @author gusiyuan
 * @since 2024/11/13 11:05
 */
@Data
public class EmpowerGroupOut implements Serializable {

    private static final long serialVersionUID = 4037570423415338126L;

    @ApiModelProperty(value = "组类型")
    private String groupType;

    @ApiModelProperty(value = "组编码")
    private String groupCode;
}
