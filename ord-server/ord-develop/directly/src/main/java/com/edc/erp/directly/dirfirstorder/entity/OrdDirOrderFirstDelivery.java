package com.edc.erp.directly.dirfirstorder.entity;


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
 * 配货单与铺货单关联表(OrdDirOrderFirstDelivery)实体类
 *
 * @author weichao
 * @since 2022-11-10 14:09:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_order_first_delivery")
@ApiModel(value = "OrdDirOrderFirstDelivery", description = "配货单与铺货单关联表")
public class OrdDirOrderFirstDelivery implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键id */
    @ApiModelProperty(name = "id", value = "主键id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 铺货单主键 */
    @ApiModelProperty(name = "firstOrderId", value = "铺货单主键")
    private Long firstOrderId;

    /** 配货单主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键")
    private Long deliveryOrderId;

    /** 创建者 */
    @ApiModelProperty(name = "creator", value = "创建者")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private Date createTime;

}
