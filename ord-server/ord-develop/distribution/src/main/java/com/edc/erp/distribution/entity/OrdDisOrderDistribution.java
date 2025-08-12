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
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 配销分货单(OrdDisOrderDistribution)实体类
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_order_distribution")
@ApiModel(value = "OrdDisOrderDistribution", description = "配销分货单")
public class OrdDisOrderDistribution implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配销分货主键
     */
    @ApiModelProperty(name = "id", value = "配销分货主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配销分货单号
     */
    @ApiModelProperty(name = "distributionOrderNo", value = "配销分货单号")
    private String distributionOrderNo;

    /**
     * 配销分货状态
     */
    @ApiModelProperty(name = "distributionOrderStatus", value = "配销分货状态")
    private String distributionOrderStatus;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 仓位名称
     */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;

    /**
     * 分货总数量
     */
    @ApiModelProperty(name = "distributionTotalQuantity", value = "分货总数量")
    private BigDecimal distributionTotalQuantity;

    /**
     * 配销分货总金额
     */
    @ApiModelProperty(name = "distributionTotalAmount", value = "配销分货总金额")
    private BigDecimal distributionTotalAmount;

    /**
     * 是否即时生效
     */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否即时生效(0:否 1是)")
    private Integer isEffectiveImmediately;

    /**
     * 生效时间
     */
    @ApiModelProperty(name = "effectiveTime", value = "生效时间")
    private LocalDateTime effectiveTime;

    /**
     * 是否已完结
     */
    @ApiModelProperty(name = "isEnd", value = "是否已完结")
    private Integer isEnd;

    @ApiModelProperty(name = "distributionIdentification", value = "分货标识")
    private String distributionIdentification;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 订货单主键
     */
    @ApiModelProperty(name = "orderId", value = "订货单主键")
    private Long orderId;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

}
