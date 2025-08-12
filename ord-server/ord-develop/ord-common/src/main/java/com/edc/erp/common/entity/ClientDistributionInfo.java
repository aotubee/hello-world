package com.edc.erp.common.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 客户配送信息表(ClientDistributionInfo)实体类
 *
 * @author lx
 * @since 2022-10-18 18:33:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "csr_client_distribution_info")
@ApiModel(value = "ClientDistributionInfo", description = "客户配送信息表")
public class ClientDistributionInfo implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 收货人 */
    @ApiModelProperty(name = "consignee", value = "收货人")
    private String consignee;

    /** 收货人电话 */
    @ApiModelProperty(name = "consigneePhone", value = "收货人电话")
    private String consigneePhone;

    /** 省编码 */
    @ApiModelProperty(name = "provinceAreaCode", value = "省编码")
    private String provinceAreaCode;

    /** 市编码 */
    @ApiModelProperty(name = "cityAreaCode", value = "市编码")
    private String cityAreaCode;

    /** 区编码 */
    @ApiModelProperty(name = "districtAreaCode", value = "区编码")
    private String districtAreaCode;

    /** 详细地址 */
    @ApiModelProperty(name = "addressDetail", value = "详细地址")
    private String addressDetail;

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

    /** 是否删除(0：否，1：是；0：默认) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；0：默认)")
    private Integer isDelete;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

}
