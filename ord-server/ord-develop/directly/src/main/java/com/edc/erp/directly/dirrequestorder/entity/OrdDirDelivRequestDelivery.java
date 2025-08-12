package com.edc.erp.directly.dirrequestorder.entity;


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
 * 要货单与配货单关联表(OrdDirDelivRequestDelivery)实体类
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_deliv_request_delivery")
@ApiModel(value = "OrdDirDelivRequestDelivery", description = "要货单与配货单关联表")
public class OrdDirDelivRequestDelivery implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 要货单单号主键 */
    @ApiModelProperty(name = "requestOrderId", value = "要货单单号主键")
    private Long requestOrderId;

    /** 配货单号主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单号主键")
    private Long deliveryOrderId;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

}
