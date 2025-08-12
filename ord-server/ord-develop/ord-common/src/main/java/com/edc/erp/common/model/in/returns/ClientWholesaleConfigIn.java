package com.edc.erp.common.model.in.returns;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:批发客户价格组配置详情查询入参
 * @author wld
 * @since 2022/10/27 14:02
 */
@Data
public class ClientWholesaleConfigIn extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户代码
     */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /**
     * 客户名称
     */
    @ApiModelProperty(name = "clientName", value = "客户名称")
    private String clientName;

    /**
     * 批发价格组(Code+Name)
     */
    @ApiModelProperty(name = "priceGroup", value = "批发价格组")
    private String priceGroup;

    /**
     * 批发价格组代码
     */
    @ApiModelProperty(name = "priceGroupCode", value = "批发价格组代码")
    private String priceGroupCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;
}