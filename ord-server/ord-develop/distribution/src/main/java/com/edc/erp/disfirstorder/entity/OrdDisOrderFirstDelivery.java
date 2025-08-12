package com.edc.erp.disfirstorder.entity;


import java.time.LocalDateTime;

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


/**
 * 配销配货单与配销铺货单关联表(OrdDisOrderFirstDelivery)实体类
 *
 * @author weichao
 * @since 2022-10-10 16:18:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_order_first_delivery")
@ApiModel(value = "OrdDisOrderFirstDelivery", description = "配销配货单与配销铺货单关联表")
public class OrdDisOrderFirstDelivery implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键id */
    @ApiModelProperty(name = "id", value = "主键id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配销铺货单主键 */
    @ApiModelProperty(name = "firstOrderId", value = "配销铺货单主键")
    private Long firstOrderId;

    /** 配销配货单主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配销配货单主键")
    private Long deliveryOrderId;

    /** 创建者 */
    @ApiModelProperty(name = "creator", value = "创建者")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

}
