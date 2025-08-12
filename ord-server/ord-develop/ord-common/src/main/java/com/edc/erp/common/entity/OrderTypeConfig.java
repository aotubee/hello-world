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
import java.math.BigDecimal;
import java.util.Date;


/**
 * 订单类型设置表(OrderTypeConfig)实体类
 *
 * @author fxw
 * @since 2022-10-18 16:54:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "opt_order_type_config")
@ApiModel(value = "OrderTypeConfig", description = "订单类型设置表")
public class OrderTypeConfig implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 订单类型代码 */
    @ApiModelProperty(name = "orderTypeCode", value = "订单类型代码")
    private String orderTypeCode;

    /** 订单类型名称 */
    @ApiModelProperty(name = "orderTypeName", value = "订单类型名称")
    private String orderTypeName;

    /** 订单类型简称 */
    @ApiModelProperty(name = "shortOrderType", value = "订单类型简称")
    private String shortOrderType;

    /** 单据业务类型 */
    @ApiModelProperty(name = "orderBusinessType", value = "单据业务类型")
    private String orderBusinessType;

    /** 商品组合主键 */
    @ApiModelProperty(name = "goodsCombinationId", value = "商品组合主键")
    private Long goodsCombinationId;

    /** 截单时间 */
    @ApiModelProperty(name = "truncationTimePoint", value = "截单时间")
    private String truncationTimePoint;

    /** 起送额 */
    @ApiModelProperty(name = "minimumOrderAmount", value = "起送额")
    private BigDecimal minimumOrderAmount;

    /** 要货周期 */
    @ApiModelProperty(name = "orderPeriod", value = "要货周期")
    private String orderPeriod;

    /** 描述 */
    @ApiModelProperty(name = "remark", value = "描述")
    private String remark;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private Date createTime;

    /** 修改人 */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private Date updateTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

}
