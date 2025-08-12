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
public class EmpowerGroupDetailOut implements Serializable {

    private static final long serialVersionUID = 6396538960566248006L;

    @ApiModelProperty(value = "组类型(purchase:采购组；logistics：物流组; operation：运营组)")
    private String groupType;

    @ApiModelProperty(value = "组明细代码")
    private String code;
}
