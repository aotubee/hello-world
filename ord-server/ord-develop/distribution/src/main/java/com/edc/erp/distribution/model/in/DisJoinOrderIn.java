package com.edc.erp.distribution.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 作废分货单关联的订货单列表使用的入参类
 * @author lee
 */
@Data
public class DisJoinOrderIn extends Page implements Serializable {

    @ApiModelProperty(name = "disId", value = "分货单主键")
    private Long disId;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织code")
    private String bizOrgCode;

}
