package com.edc.erp.common.model.in.customer;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName QueryClientInfoIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/11 10:20
 **/
@Data
public class QueryClientInfoIn implements Serializable {
    private static final long serialVersionUID = -7401818687243655409L;

    /** 客户配送信息ID */
    @ApiModelProperty(name = "distributionInfoId", value = "客户配送信息ID")
    private Long distributionInfoId;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 收货人 */
    @ApiModelProperty(name = "consignee", value = "收货人")
    private String consignee;

    /** 收货人电话 */
    @ApiModelProperty(name = "consigneePhone", value = "收货人电话")
    private String consigneePhone;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
