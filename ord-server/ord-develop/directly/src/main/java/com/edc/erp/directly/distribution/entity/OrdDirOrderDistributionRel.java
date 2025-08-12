package com.edc.erp.directly.distribution.entity;


import com.edc.plugins.common.model.BaseEntity;
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
 * 直营分货单与直营订货单关联表(DirOrderDistributionRel)实体类
 *
 * @author yaojinpeng
 * @since 2022-10-24 09:50:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_order_distribution_rel")
@ApiModel(value = "DirOrderDistributionRel", description = "直营分货单与直营订货单关联表")
public class OrdDirOrderDistributionRel extends BaseEntity implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键id */
    @ApiModelProperty(name = "id", value = "主键id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 分货单主键 */
    @ApiModelProperty(name = "distributionOrderId", value = "分货单主键")
    private Long distributionOrderId;

    /** 订货单主键 */
    @ApiModelProperty(name = "orderId", value = "订货单主键")
    private Long orderId;

    /** 创建者 */
    @ApiModelProperty(name = "creator", value = "创建者")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

}
