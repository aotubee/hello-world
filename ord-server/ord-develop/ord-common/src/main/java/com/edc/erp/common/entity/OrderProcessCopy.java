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
 * 订单类型流程副本(OrderProcessCopy)实体类
 *
 * @author fxw
 * @since 2022-10-18 19:21:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "opt_order_process_copy")
@ApiModel(value = "OrderProcessCopy", description = "订单类型流程副本")
public class OrderProcessCopy implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 流程名称 */
    @ApiModelProperty(name = "processName", value = "流程名称")
    private String processName;

    /** 序号 */
    @ApiModelProperty(name = "serialNumber", value = "序号")
    private Integer serialNumber;

    /** 组织代码 */
    @ApiModelProperty(name = "orgCode", value = "组织代码")
    private String orgCode;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 订单类型简称 */
    @ApiModelProperty(name = "shortOrderType", value = "订单类型简称")
    private String shortOrderType;

    /** 订货周期主键 */
    @ApiModelProperty(name = "orderCycleId", value = "订货周期主键")
    private Integer orderCycleId;

    /** 流程代码 */
    @ApiModelProperty(name = "progressCode", value = "流程代码")
    private String progressCode;

    /** 配置选项 */
    @ApiModelProperty(name = "processConfigItem", value = "配置选项")
    private String processConfigItem;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

}
