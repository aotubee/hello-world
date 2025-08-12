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
 * 仓储表(LogcWarehouseInfo)实体类
 *
 * @author lx
 * @since 2022-10-21 16:43:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "warehouse_info")
@ApiModel(value = "WarehouseInfo", description = "仓储表")
public class WarehouseInfo implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 仓储代码 */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /** 仓储名称 */
    @ApiModelProperty(name = "warehouseName", value = "仓储名称")
    private String warehouseName;

    /** 是否启用 */
    @ApiModelProperty(name = "isEnable", value = "是否启用")
    private Integer isEnable;

    /** 联系人 */
    @ApiModelProperty(name = "contact", value = "联系人")
    private String contact;

    /** 联系电话 */
    @ApiModelProperty(name = "contactPhone", value = "联系电话")
    private String contactPhone;

    /** 地址 */
    @ApiModelProperty(name = "address", value = "地址")
    private String address;

    /** 物流商ID */
    @ApiModelProperty(name = "logcId", value = "物流商ID")
    private Integer logcId;

    /** 配送中心ID */
    @ApiModelProperty(name = "dcId", value = "配送中心ID")
    private Integer dcId;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织")
    private String bizOrgCode;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

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

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

}
