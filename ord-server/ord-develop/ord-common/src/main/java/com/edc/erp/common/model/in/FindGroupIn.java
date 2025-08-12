package com.edc.erp.common.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * description 根据类型和组获取授权组明细入参
 *
 * @author gusiyuan
 * @since 2024/11/12 17:28
 */
@Data
public class FindGroupIn implements Serializable {

    private static final long serialVersionUID = 6573956594180994916L;

    @ApiModelProperty(value = "组类型(purchase:采购组；logistics：物流组; operation：运营组)")
    private String groupType;

    @ApiModelProperty(value = "组代码集合")
    private List<String> groupCodes;

    @ApiModelProperty(value = "所属组织")
    private String bizOrgCode;
}
