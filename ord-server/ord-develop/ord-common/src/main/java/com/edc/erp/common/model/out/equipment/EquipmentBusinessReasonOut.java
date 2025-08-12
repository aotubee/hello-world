package com.edc.erp.common.model.out.equipment;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 库存业务原因配置
 */
@Data
public class EquipmentBusinessReasonOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    /**
     * 业务原因类型
     */
    @ApiModelProperty(name = "businessReasonType", value = "业务原因类型")
    private String businessReasonType;

    /**
     * 业务原因名称
     */
    @ApiModelProperty(name = "businessReasonName", value = "业务原因名称")
    private String businessReasonName;

    /**
     * 业务原因代码
     */
    @ApiModelProperty(name = "businessReasonCode", value = "业务原因代码")
    private String businessReasonCode;

    /**
     * 业务原因维度
     */
    @ApiModelProperty(name = "businessReasonDimension", value = "业务原因维度")
    private String businessReasonDimension;

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

    /**
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 业务原因类型name
     */
    @ApiModelProperty(name = "businessReasonTypeName", value = "业务原因类型name")
    private String businessReasonTypeName;

    /**
     * 业务原因维度name
     */
    @ApiModelProperty(name = "businessReasonDimensionName", value = "业务原因维度name")
    private String businessReasonDimensionName;

}
