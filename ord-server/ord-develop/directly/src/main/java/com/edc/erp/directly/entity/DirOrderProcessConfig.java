package com.edc.erp.directly.entity;


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
 *  订单类型流程配置表(OrderProcessConfig)实体类
 *
 * @author fxw
 * @since 2022-10-18 16:49:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "opt_dir_order_process_config")
@ApiModel(value = "DirOrderProcessConfig", description = " 订单类型流程配置表")
public class DirOrderProcessConfig implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 流程配置名称 */
    @ApiModelProperty(name = "processConfigName", value = "流程配置名称")
    private String processConfigName;

    /** 流程配置代码 */
    @ApiModelProperty(name = "processConfigCode", value = "流程配置代码")
    private String processConfigCode;

    /** 订单类型流程主键 */
    @ApiModelProperty(name = "orderProcessId", value = "订单类型流程主键")
    private Long orderProcessId;

    /** 是否单选 */
    @ApiModelProperty(name = "isRadio", value = "是否单选")
    private Integer isRadio;

    /** 序号 */
    @ApiModelProperty(name = "serialNumber", value = "序号")
    private Integer serialNumber;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

}
