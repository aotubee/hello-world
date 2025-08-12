package com.edc.erp.disdifferenceorder.model.in;

import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 保存差异单入参
 * @author weichao
 */
@Data
public class SaveDifferenceIn {

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
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

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

    /** 仓储编码 */
    @ApiModelProperty(name = "wrhCode", value = "仓储编码")
    private String wrhCode;

    /**
     * 差异单明细集合
     */
    private List<OrdDisDelivDifferenceDetail> differenceDetails;
}
