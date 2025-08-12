package com.edc.erp.disrequestorder.entity;


import java.util.Date;
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
 * 集货单与配销单关联表(OrdDisDelivRequestDelivery)实体类
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_deliv_request_delivery")
@ApiModel(value = "OrdDisDelivRequestDelivery", description = "集货单与配销单关联表")
public class OrdDisDelivRequestDelivery implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 集货单主键 */
    @ApiModelProperty(name = "requestOrderId", value = "集货单主键")
    private Long requestOrderId;

    /** 配销单主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配销单主键")
    private Long deliveryOrderId;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

}
