package com.edc.erp.directly.dirdeliveryorder.entity;


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
 * 配货单签收附件表(OrdDirDeliveryOrderAttachment)实体类
 *
 * @author weichao
 * @since 2022-11-22 10:46:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_delivery_order_attachment")
@ApiModel(value = "OrdDirDeliveryOrderAttachment", description = "配货单签收附件表")
public class OrdDirDeliveryOrderAttachment implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 配货单主键 */
    @ApiModelProperty(name = "dirDeliveryOrderId", value = "配货单主键")
    private Long dirDeliveryOrderId;

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
