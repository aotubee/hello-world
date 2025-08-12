package com.edc.erp.distribution.entity;


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
 * 配销订单追踪表(OrdDisOrderTrack)实体类
 *
 * @author fxw
 * @since 2022-10-18 14:17:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_order_track")
@ApiModel(value = "OrdDisOrderTrack", description = "配销订单追踪表")
public class OrdDisOrderTrack implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 订单编号 */
    @ApiModelProperty(name = "orderNo", value = "订单编号")
    private String orderNo;

    /** 订单状态 */
    @ApiModelProperty(name = "orderStatus", value = "订单状态")
    private String orderStatus;

    /** 跟踪日志 */
    @ApiModelProperty(name = "trackLog", value = "跟踪日志")
    private String trackLog;

    /** 业务类型 */
    @ApiModelProperty(name = "businessType", value = "业务类型")
    private String businessType;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 部门id */
    @ApiModelProperty(name = "deptId", value = "部门id")
    private Integer deptId;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

}
