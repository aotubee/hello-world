package com.edc.erp.disdeliveryorder.entity;


import com.edc.plugins.common.model.BaseEntity;
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
import java.time.LocalDateTime;


/**
 * 配销捞单池(OrdDisSalvageDelivPond)实体类
 *
 * @author zy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_salvage_deliv_pond")
@ApiModel(value = "OrdDisSalvageDelivPond", description = "配销捞单池")
public class OrdDisSalvageDelivPond extends BaseEntity {

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单类型id
     */
    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型id")
    private Integer orderTypeConfigId;

    /**
     * 截单时间
     */
    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private LocalDateTime truncationDateTime;

    /**
     * 捞单时间
     */
    @ApiModelProperty(name = "salvageTime", value = "捞单时间")
    private LocalDateTime salvageTime;

    /**
     * 捞单状态
     */
    @ApiModelProperty(name = "salvageStatus", value = "捞单状态")
    private String salvageStatus;

    /**
     * 等待执行总数
     */
    @ApiModelProperty(name = "waitExecutionTotal", value = "等待执行总数")
    private Integer waitExecutionTotal;

    /**
     * 实际执行总数
     */
    @ApiModelProperty(name = "executionTotal", value = "实际执行总数")
    private Integer executionTotal;


    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

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

}
