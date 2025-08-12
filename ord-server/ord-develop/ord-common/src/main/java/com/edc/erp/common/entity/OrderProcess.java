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
 * 订单类型流程(OrderProcess)实体类
 *
 * @author fxw
 * @since 2022-10-18 16:47:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "opt_order_process")
@ApiModel(value = "OrderProcess", description = "订单类型流程")
public class OrderProcess implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单类型主键 */
    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型主键")
    private Long orderTypeConfigId;

    /** 流程节点 */
    @ApiModelProperty(name = "processNodeCode", value = "流程节点")
    private String processNodeCode;

    /** 流程名称 */
    @ApiModelProperty(name = "processName", value = "流程名称")
    private String processName;

    /** 流程代码 */
    @ApiModelProperty(name = "processCode", value = "流程代码")
    private String processCode;

    /** 序号 */
    @ApiModelProperty(name = "serialNumber", value = "序号")
    private Integer serialNumber;

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

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

}
