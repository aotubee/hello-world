package com.edc.erp.returnorder.entity;


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
 * 退货单(DisReturn)实体类
 *
 * @author yaojinpeng
 * @since 2022-10-24 15:35:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_return")
@ApiModel(value = "DisReturn", description = "退货单")
public class OrdDisReturn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 退货单号
     */
    @ApiModelProperty(name = "returnOrderNo", value = "退货单号")
    private String returnOrderNo;

    /**
     * 退货状态
     */
    @ApiModelProperty(name = "returnStatus", value = "退货状态")
    private String returnStatus;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 配送方式
     */
    @ApiModelProperty(name = "distributionType", value = "配送方式")
    private String distributionType;


    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 品项数
     */
    @ApiModelProperty(name = "skuCount", value = "品项数")
    private Integer skuCount;

    /**
     * 退货类型
     */
    @ApiModelProperty(name = "returnType", value = "退货类型")
    private String returnType;

    /**
     * 退货通知单主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
    private Integer returnNoticeOrderId;

    /**
     * 提交时间
     */
    @ApiModelProperty(name = "submitTime", value = "提交时间")
    private LocalDateTime submitTime;

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
     * 申请退货数量
     */
    @ApiModelProperty(name = "applyReturnQuantity", value = "申请退货数量")
    private BigDecimal applyReturnQuantity;

    /**
     * 申请退货金额
     */
    @ApiModelProperty(name = "applyReturnAmount", value = "申请退货金额")
    private BigDecimal applyReturnAmount;

    /**
     * 审核退货数量
     */
    @ApiModelProperty(name = "auditReturnQuantity", value = "审核退货数量")
    private BigDecimal auditReturnQuantity;

    /**
     * 审核退货金额
     */
    @ApiModelProperty(name = "auditReturnAmount", value = "审核退货金额")
    private BigDecimal auditReturnAmount;

    /**
     * 实际退货数量
     */
    @ApiModelProperty(name = "actualReturnQuantity", value = "实际退货数量")
    private BigDecimal actualReturnQuantity;

    /**
     * 实际退货金额
     */
    @ApiModelProperty(name = "actualReturnAmount", value = "实际退货金额")
    private BigDecimal actualReturnAmount;

    /**
     * 是否被红冲
     */
    @ApiModelProperty(name = "isReversal", value = "是否被红冲")
    private Integer isReversal;

    /**
     * 是否红冲单
     */
    @ApiModelProperty(name = "isRedRush", value = "是否红冲单")
    private Integer isReversalOrder;

    /**
     * 来源退货单号
     */
    @ApiModelProperty(name = "sourceReturnNo", value = "来源退货单号")
    private String sourceReturnNo;

    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;


    /**
     * 海鼎退货单号
     */
    @ApiModelProperty(name = "hdReturnNo", value = "海鼎退货单号")
    private String hdReturnNo;

    /**
     * 物流单号
     */
    @ApiModelProperty(name = "logisticsNo", value = "物流单号")
    private String logisticsNo;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /**
     * 修改人
     */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 退货单原因
     */
    @ApiModelProperty(name = "returnOrderReason", value = "退货单原因")
    private String returnOrderReason;

    /**
     * 收货时间
     */
    @ApiModelProperty(name = "receiveTime", value = "收货时间")
    private LocalDateTime receiveTime;

    /**
     * app备注
     */
    @ApiModelProperty(name = "appRemark", value = "app备注")
    private String appRemark;

    @ApiModelProperty(name = "deliveryOrderNo", value = "配货单单号")
    private String deliveryOrderNo;

//    /**
//     * 配货方式
//     */
//    @ApiModelProperty(name = "distributionWay", value = "配货方式")
//    private String distributionWay;

    @ApiModelProperty(name = "auditor", value = "审核人")
    private String auditor;

    @ApiModelProperty(name = "auditTime", value = "审核时间")
    private LocalDateTime auditTime;
}
