package com.edc.erp.common.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description:客户资料表(ClientInfo)实体类
 * @author wld
 * @since 2022/10/31 20:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "ClientInfo", description = "客户资料表")
public class ClientInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 客户名称 */
    @ApiModelProperty(name = "clientName", value = "客户名称")
    @NotBlank(message = "客户名称不能为空!")
    private String clientName;

    /** 客户类型（0：批发商，1：加盟商） */
    @ApiModelProperty(name = "clientType", value = "客户类型（批发商，加盟商）")
    @NotBlank(message = "客户类型不能为空!")
    private String clientType;

    /** 客户地址 */
    @ApiModelProperty(name = "clientAddress", value = "客户地址")
    private String clientAddress;

    /** 客户身份证号 */
    @ApiModelProperty(name = "clientIdNumber", value = "客户身份证号")
    private String clientIdNumber;

    /** 联系人 */
    @ApiModelProperty(name = "contactPerson", value = "联系人")
    @NotBlank(message = "联系人不能为空!")
    private String contactPerson;

    /** 联系电话 */
    @ApiModelProperty(name = "contactPhone", value = "联系电话")
    @NotBlank(message = "联系电话不能为空!")
    private String contactPhone;

    /** E_Mail地址 */
    @ApiModelProperty(name = "email", value = "E_Mail地址")
    private String email;

    /** 财务编码 */
    @ApiModelProperty(name = "financialCode", value = "财务编码")
    private String financialCode;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改人 */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /** 是否启用(0：否，1：是；默认1) */
    @ApiModelProperty(name = "isEnable", value = "是否启用(0：否，1：是；默认1)")
    private Integer isEnable;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** 配送方式 */
    @ApiModelProperty(name = "distributionWay", value = "配送方式")
    @NotBlank(message = "配送方式不能为空!")
    private String distributionWay;

    /** 是否拥有门店 */
    @ApiModelProperty(name = "isPossessStore", value = "是否拥有门店")
    @NotNull(message = "是否拥有门店不能为空!")
    private Integer isPossessStore;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 经纬度 */
    @ApiModelProperty(name = "lngAndLat", value = "经纬度")
    private String lngAndLat;
}