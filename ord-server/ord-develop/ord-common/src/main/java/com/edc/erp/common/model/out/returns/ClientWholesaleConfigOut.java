package com.edc.erp.common.model.out.returns;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * @description:批发客户价格组配置详情查询出参
 * @author wld
 * @since 2022/10/27 11:23
 */
@Data
public class ClientWholesaleConfigOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户id
     */
    @ApiModelProperty(name = "clientId", value = "客户id")
    private Integer clientId;

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
     * 客户类型（0：批发商，1：加盟商）
     */
    @ApiModelProperty(name = "clientType", value = "客户类型（批发商，加盟商）")
    @NotBlank(message = "客户类型不能为空!")
    private String clientType;

    /**
     * 批发价格组
     */
    @ApiModelProperty(name = "priceGroup", value = "批发价格组")
    private String priceGroup;

    /**
     * 价格组代码
     */
    @ApiModelProperty(name = "priceGroupCode", value = "价格组代码")
    private String priceGroupCode;

    /**
     * 价格组名称
     */
    @ApiModelProperty(name = "priceGroupName", value = "价格组名称")
    private String priceGroupName;

    /**
     * 批发价格组原则
     */
    @ApiModelProperty(name = "principle", value = "批发价格组原则")
    private String principle;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;
}