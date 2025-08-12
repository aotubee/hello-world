package com.edc.erp.common.model.in.customer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询客户信息 入参类
 * @author lx
 * @since 2022-10-19 10:54:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryClientDistInfoIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 客户配送信息ID */
    @ApiModelProperty(name = "distributionInfoId", value = "客户配送信息ID")
    private Long distributionInfoId;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 客户类型（批发商，加盟商） */
    @ApiModelProperty(name = "clientType", value = "客户类型（批发商，加盟商）")
    private String clientType;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    public QueryClientDistInfoIn(String clientCode, String bizOrgCode) {
        this.clientCode = clientCode;
        this.bizOrgCode = bizOrgCode;
    }
}
