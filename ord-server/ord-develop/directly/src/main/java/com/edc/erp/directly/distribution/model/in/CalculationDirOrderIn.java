package com.edc.erp.directly.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-28 09:35
 */
@Data
public class CalculationDirOrderIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "orderIdList", value = "所选订货单id集合")
    private List<Long> orderIdList;

    @ApiModelProperty(name = "storeCode", value = "门店代码", required = true)
    @NotEmpty(message = "门店代码不能为空")
    private String storeCode;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码不能为空", required = true)
    @NotEmpty(message = "组织代码不能为空")
    private String bizOrgCode;
}
