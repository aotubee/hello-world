package com.edc.erp.upperlowerlimit.model.out;


import java.util.Date;
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
 * 补货门店禁用范围表(ReplenishmentStoreDisabledRange)实体类
 *
 * @author weichao
 * @since 2022-10-18 19:44:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "gc_replenishment_store_disabled_range")
@ApiModel(value = "ReplenishmentStoreDisabledRange", description = "补货门店禁用范围表")
public class ReplenishmentStoreDisabledRange implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 门店名称 */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /** 开始时间 */
    @ApiModelProperty(name = "beginTime", value = "开始时间")
    private LocalDateTime beginTime;

    /** 结束时间 */
    @ApiModelProperty(name = "endTime", value = "结束时间")
    private LocalDateTime endTime;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

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

    /** 是否作废 */
    @ApiModelProperty(name = "isInvalid", value = "是否作废")
    private Integer isInvalid;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /**
     * 组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

}
