package com.edc.erp.directly.distribution.model.out;


import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台分货单表头出参对象
 *
 * @author yaojinpeng
 * @since 2022/10/18 15:56
 */
@Data
public class BackHeaderOrdDistributionOrderOut extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 分货单主键
     */
    @ApiModelProperty(name = "distributionOrderId", value = "分货单主键")
    private Long distributionOrderId;

    /**
     * 分货单号
     */
    @ApiModelProperty(name = "distributionOrderNo", value = "分货单号")
    private String distributionOrderNo;

    /**
     * 分货单状态
     */
    @ApiModelProperty(name = "distributionOrderStatus", value = "分货单状态")
    private String distributionOrderStatus;

    /**
     * 分货单状态中文值
     */
    @ApiModelProperty(name = "distributionOrderStatusValue", value = "分货单状态中文值")
    private String distributionOrderStatusValue;

    /**
     * 提交人
     */
    @ApiModelProperty(name = "creatorId", value = "提交人")
    private String creatorId;

    /**
     * 提交时间
     */
    @ApiModelProperty(name = "createTime", value = "提交时间")
    private LocalDateTime createTime;

    /**
     * 总分货数量
     */
    @ApiModelProperty(name = "totalDistributionQuantity", value = "总分货数量")
    private BigDecimal totalDistributionQuantity;

    /**
     * 总分货金额
     */
    @ApiModelProperty(name = "distributionTotalAmount", value = "总分货金额")
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
     * 是否立即生效
     */
    @ApiModelProperty(name = "isEnd", value = "是否立即生效")
    private Integer isEnd;
}
