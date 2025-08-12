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
 * 配销单签收附件表(OrdDisDeliveryOrderAttachment)实体类
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_delivery_order_attachment")
@ApiModel(value = "OrdDisDeliveryOrderAttachment", description = "配销单签收附件表")
public class OrdDisDeliveryOrderAttachment implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 配销单主键 */
    @ApiModelProperty(name = "disDeliveryOrderId", value = "配销单主键")
    private Long disDeliveryOrderId;

    /** 附件地址 */
    @ApiModelProperty(name = "attachmentUrl", value = "附件地址")
    private String attachmentUrl;

    /** 附件类型，1：签收，2：收货 */
    @ApiModelProperty(name = "attachmentType", value = "附件类型，1：签收，2：收货")
    private Integer attachmentType;

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
