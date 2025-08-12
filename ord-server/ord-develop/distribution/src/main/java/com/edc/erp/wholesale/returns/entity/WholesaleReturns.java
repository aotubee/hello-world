package com.edc.erp.wholesale.returns.entity;

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
 * 批发退货单(WholesaleReturns)实体类
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_wholesale_returns")
@ApiModel(value = "WholesaleReturns", description = "批发退货单")
public class WholesaleReturns implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 批发退货单单号
     */
    @ApiModelProperty(name = "wholesaleReturnNo", value = "批发退货单单号")
    private String wholesaleReturnNo;

    /**
     * 退货单状态
     */
    @ApiModelProperty(name = "returnStatus", value = "退货单状态")
    private String returnStatus;

    /**
     * 客户代码
     */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /**
     * 价格组代码
     */
    @ApiModelProperty(name = "priceGroupCode", value = "价格组代码")
    private String priceGroupCode;

    /**
     * 入库仓储
     */
    @ApiModelProperty(name = "storageWrh", value = "入库仓储")
    private String storageWrh;

    /**
     * 入库仓位
     */
    @ApiModelProperty(name = "storageStockCode", value = "入库仓位")
    private String storageStockCode;

    /**
     * 是否被红冲
     */
    @ApiModelProperty(name = "isReversal", value = "是否被红冲")
    private Integer isReversal;

    /**
     * 是否红冲单
     */
    @ApiModelProperty(name = "isReversalOrder", value = "是否红冲单")
    private Integer isReversalOrder;

    /**
     * 申请数量
     */
    @ApiModelProperty(name = "applicationQuantity", value = "申请数量")
    private Integer applicationQuantity;

    /**
     * 申请金额
     */
    @ApiModelProperty(name = "applicationAmount", value = "申请金额")
    private BigDecimal applicationAmount;

    /**
     * 入库数量
     */
    @ApiModelProperty(name = "storageQuantity", value = "入库数量")
    private Integer storageQuantity;

    /**
     * 入库金额
     */
    @ApiModelProperty(name = "storageAmount", value = "入库金额")
    private BigDecimal storageAmount;

    /**
     * 物流单号
     */
    @ApiModelProperty(name = "trackingNo", value = "物流单号")
    private String trackingNo;

    /**
     * 来源单号
     */
    @ApiModelProperty(name = "sourceNo", value = "来源单号")
    private String sourceNo;

    /**
     * 批发出货单单号
     */
    @ApiModelProperty(name = "wholesaleShipmentNo", value = "批发出货单单号")
    private String wholesaleShipmentNo;

    /**
     * 退货原因
     */
    @ApiModelProperty(name = "returnsReason", value = "退货原因")
    private String returnsReason;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 收货人
     */
    @ApiModelProperty(name = "consignee", value = "收货人")
    private String consignee;

    /**
     * 配送信息ID
     */
    @ApiModelProperty(name = "distributionInfoId",value="配送信息ID")
    private Integer distributionInfoId;

    /**
     * 收货人手机号
     */
    @ApiModelProperty(name = "consigneePhone", value = "收货人手机号")
    private String consigneePhone;

    /**
     * 收货详细地址
     */
    @ApiModelProperty(name = "addressDetail", value = "收货详细地址")
    private String addressDetail;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

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

    /**
     * 收货时间
     */
    @ApiModelProperty(name = "receiveTime", value = "收货时间")
    private LocalDateTime receiveTime;

    /** 审核时间 */
    @ApiModelProperty(name = "auditTime", value = "审核时间")
    private LocalDateTime auditTime;
}
