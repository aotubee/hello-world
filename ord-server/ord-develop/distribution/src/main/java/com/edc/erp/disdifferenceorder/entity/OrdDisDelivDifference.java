package com.edc.erp.disdifferenceorder.entity;


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
 * 配销差异单(OrdDisDelivDifference)实体类
 *
 * @author weichao
 * @since 2022-10-24 11:16:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_deliv_difference")
@ApiModel(value = "OrdDisDelivDifference", description = "配销差异单")
public class OrdDisDelivDifference implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 配销差异单号 */
    @ApiModelProperty(name = "differenceNo", value = "配销差异单号")
    private String differenceNo;

    /** 海鼎差异单号 */
    @ApiModelProperty(name = "hdDifferenceNo", value = "海鼎差异单号")
    private String hdDifferenceNo;

    /** 配销差异单状态 */
    @ApiModelProperty(name = "differenceStatus", value = "配销差异单状态")
    private String differenceStatus;

    /** 是否红冲单 */
    @ApiModelProperty(name = "isReversalOrder", value = "是否红冲单")
    private Integer  isReversalOrder;

    /** 是否被红冲 */
    @ApiModelProperty(name = "isRedRush", value = "是否被红冲")
    private Integer  isReversal;
    /** 配销差异类型 */
    @ApiModelProperty(name = "differenceType", value = "配销差异类型")
    private String differenceType;

    /** 配销单号 */
    @ApiModelProperty(name = "deliveryOrderNo", value = "配销单号")
    private String deliveryOrderNo;

    /** 海鼎配货单号 */
    @ApiModelProperty(name = "hdDeliveryNo", value = "海鼎配货单号")
    private String hdDeliveryNo;

    /** 仓位代码 */
    @ApiModelProperty(name = "position", value = "仓位代码")
    private String position;

    /** 物流单号 */
    @ApiModelProperty(name = "logisticsNo", value = "物流单号")
    private String logisticsNo;

    /** 门店名称 */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 申请总差异数量 */
    @ApiModelProperty(name = "totalApplyDifferenceQuantity", value = "申请总差异数量")
    private BigDecimal totalApplyDifferenceQuantity;

    /** 申请总差异金额 */
    @ApiModelProperty(name = "totalApplyDifferenceAmount", value = "申请总差异金额")
    private BigDecimal totalApplyDifferenceAmount;

    /** 批准总差异数量 */
    @ApiModelProperty(name = "totalApprovalDifferenceQuantity", value = "批准总差异数量")
    private BigDecimal totalApprovalDifferenceQuantity;

    /** 批准总差异金额 */
    @ApiModelProperty(name = "totalApprovalDifferenceAmount", value = "批准总差异金额")
    private BigDecimal totalApprovalDifferenceAmount;

    /** 发货日期 */
    @ApiModelProperty(name = "deliveryTime", value = "发货日期")
    private LocalDateTime deliveryTime;

    /** 收货日期 */
    @ApiModelProperty(name = "receiveTime", value = "收货日期")
    private LocalDateTime receiveTime;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

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

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /** 来源单号 */
    @ApiModelProperty(name = "sourceNo", value = "来源单号")
    private String sourceNo;

    /** 仓储代码 */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;

    /** 批准日期 */
    @ApiModelProperty(name = "approvalTime", value = "批准日期")
    private LocalDateTime approvalTime;
}
