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
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 仓储库存分配配置表(EquipmentStockAllot)实体类
 *
 * @author fxw
 * @since 2022-07-14 10:33:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "logc_equipment_stock_allot")
@ApiModel(value = "EquipmentStockAllot", description = "仓储库存分配配置表")
public class EquipmentStockAllot implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 仓储代码 */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    @NotBlank(message = "请输入仓储信息")
    private String warehouseCode;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    @NotBlank(message = "请输入仓位信息!")
    private String stockCode;

    /** 分配规则 */
    @ApiModelProperty(name = "allotRule", value = "分配规则")
    @NotBlank(message = "请选择仓储库存分配规则!")
    private String allotRule;

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

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

}
