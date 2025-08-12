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
import java.util.Date;


/**
 * 订单类型流程选项配置(OrderProcessConfigItem)实体类
 *
 * @author fxw
 * @since 2022-10-18 16:50:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "opt_order_process_config_item")
@ApiModel(value = "OrderProcessConfigItem", description = "订单类型流程选项配置")
public class OrderProcessConfigItem implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 选项名称 */
    @ApiModelProperty(name = "itemName", value = "选项名称")
    private String itemName;

    /** 选项代码 */
    @ApiModelProperty(name = "itemCode", value = "选项代码")
    private String itemCode;

    /** 订单流程配置主键 */
    @ApiModelProperty(name = "orderProcessConfigId", value = "订单流程配置主键")
    private Long orderProcessConfigId;

    /** 选项类型（1:选择，2:文本） */
    @ApiModelProperty(name = "itemType", value = "选项类型（1:选择，2:文本）")
    private Integer itemType;

    /** 文本值 */
    @ApiModelProperty(name = "itemValue", value = "文本值")
    private String itemValue;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private Date createTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

}
