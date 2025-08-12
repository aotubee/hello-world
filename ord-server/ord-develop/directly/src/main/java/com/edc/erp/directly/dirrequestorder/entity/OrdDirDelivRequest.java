package com.edc.erp.directly.dirrequestorder.entity;


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
import java.time.LocalDateTime;



/**
 * 要货单(OrdDirDelivRequest)实体类
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_deliv_request")
@ApiModel(value = "OrdDirDelivRequest", description = "要货单")
public class OrdDirDelivRequest implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 订单类型主键 */
    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型主键")
    private Integer orderTypeConfigId;

    /** 订货周期主键 */
    @ApiModelProperty(name = "orderCycleId", value = "订货周期主键")
    private Integer orderCycleId;

    /** 截单时间 */
    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private LocalDateTime truncationDateTime;

    /** 要货单单号 */
    @ApiModelProperty(name = "requestOrderNo", value = "要货单单号")
    private String requestOrderNo;

    /** 状态 */
    @ApiModelProperty(name = "statusCode", value = "状态")
    private String statusCode;

    /** 总金额 */
    @ApiModelProperty(name = "totalAmount", value = "总金额")
    private BigDecimal totalAmount;

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

    /** 修改人 */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

}
