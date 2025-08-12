package com.edc.erp.distribution.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-23 11:53
 */
@Data
public class AppQueryDisOrderIn extends BaseEntity {

    @ApiModelProperty(name = "storeCode", value = "门店代码", required = true)
    @NotEmpty(message = "门店代码不能为空")
    private String storeCode;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码不能为空", required = true)
    @NotEmpty(message = "组织代码不能为空")
    private String bizOrgCode;

    @ApiModelProperty(name = "beginTime", value = "开始时间")
    private String beginTime;

    @ApiModelProperty(name = "endTime", value = "结束时间")
    private String endTime;

    @ApiModelProperty(name = "orderStatusCodeList", value = "订货单状态集合")
    private List<String> orderStatusCodeList;

    @ApiModelProperty(name = "orderIdentification", value = "门店标识")
    private String orderIdentification;
}
