package com.edc.erp.disdeliveryorder.entity;


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
 * 配销单收货心跳检测(OrdDisDeliveryOrderHeartRateMonitor)实体类
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_delivery_order_heart_rate_monitor")
@ApiModel(value = "OrdDisDeliveryOrderHeartRateMonitor", description = "配销单收货心跳检测")
public class OrdDisDeliveryOrderHeartRateMonitor implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 配货单主键 */
    @ApiModelProperty(name = "disDeliveryOrderId", value = "配货单主键")
    private Long disDeliveryOrderId;

    /** 上一次心跳时间 */
    @ApiModelProperty(name = "lastHeartbeatTime", value = "上一次心跳时间")
    private LocalDateTime lastHeartbeatTime;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

}
