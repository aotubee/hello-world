package com.edc.erp.directly.returnorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @return: 退货单明细出参对象
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
public class ReturnDetailOut implements Serializable {

    private static final long serialVersionUID = -162773650354453890L;

    /**
     * 退货单id
     */
    @ApiModelProperty(name = "returnOrderId", value = "退货单id")
    private Integer returnOrderId;
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
     * 退货状态中文值
     */
    @ApiModelProperty(name = "returnStatusValue", value = "退货状态中文值")
    private String returnStatusValue;

    /**
     * 退货类型
     */
    @ApiModelProperty(name = "returnType", value = "退货类型")
    private String returnType;

    /**
     * 退货类型中文值
     */
    @ApiModelProperty(name = "returnTypeValue", value = "退货类型中文值")
    private String returnTypeValue;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeCode", value = "门店名称")
    private String storeName;

    /**
     * 商品集合
     */
    @ApiModelProperty(name = "goodsInfo", value = "商品集合")
    private List<OrdDirReturnDetailOut> goodsInfo;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remak", value = "备注")
    private String remark;

    @ApiModelProperty(name = "hdReturnNo", value = "海鼎退货单号")
    private String hdReturnNo;

    /**
     * 仓储
     */
    @ApiModelProperty(name = "wrhCode", value = "仓储")
    private String warehouseCode;

    /**
     * 仓位
     */
    @ApiModelProperty(name = "stockCode", value = "仓位")
    private String stockCode;

    /**
     * 仓位
     */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;

    /**
     * 仓位
     */
    @ApiModelProperty(name = "sourceReturnNo", value = "来源单号")
    private String sourceReturnNo;

    /**
     * 退货单原因
     */
    @ApiModelProperty(name = "returnOrderReason", value = "退货单原因")
    private String returnOrderReason;

    /**
     * 退货单原因中文值
     */
    @ApiModelProperty(name = "returnOrderReasonValue", value = "退货单原因中文值")
    private String returnOrderReasonValue;

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
     * 品项数
     */
    @ApiModelProperty(name = "skuCount", value = "品项数")
    private Integer skuCount;

    /**
     * 申请品项数
     */
    @ApiModelProperty(name = "applySkuCount", value = "申请品项数")
    private Integer applySkuCount;


    /**
     * 实际品项数
     */
    @ApiModelProperty(name = "applySkuCount", value = "实际品项数")
    private Integer actualSkuCount;


    /**
     * 申请退货数量
     */
    @ApiModelProperty(name = "applyReturnQuantity", value = "申请退货数量")
    private BigDecimal applyReturnQuantity;

    /** 申请退货金额 */
    @ApiModelProperty(name = "applyReturnAmount", value = "申请退货金额")
    private BigDecimal applyReturnAmount;

    /** 审核退货数量 */
    @ApiModelProperty(name = "auditReturnQuantity", value = "审核退货数量")
    private BigDecimal auditReturnQuantity;

    /** 审核退货金额 */
    @ApiModelProperty(name = "auditReturnAmount", value = "审核退货金额")
    private BigDecimal auditReturnAmount;

    /** 实际退货数量 */
    @ApiModelProperty(name = "actualReturnQuantity", value = "实际退货数量")
    private BigDecimal actualReturnQuantity;

    /** 实际退货金额 */
    @ApiModelProperty(name = "actualReturnAmount", value = "实际退货金额")
    private BigDecimal actualReturnAmount;

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
     * 退货通知单单号
     */
    @ApiModelProperty(name = "returnNoticeNo", value = "退货通知单单号")
    private String returnNoticeOrderNo;

    /** 退货通知单主键 */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单id")
    private Integer returnNoticeOrderId;

    /**
     * 提交时间
     */
    @ApiModelProperty(name = "submitTime", value = "提交时间")
    private LocalDateTime submitTime;

    /** 物流单号 */
    @ApiModelProperty(name = "logisticsNo", value = "物流单号")
    private String logisticsNo;

    /**
     * 收货时间
     */
    @ApiModelProperty(name = "receiveTime", value = "收货时间")
    private LocalDateTime receiveTime;

    /**
     * 配送方式
     */
    @ApiModelProperty(name = "distributionType", value = "配送方式")
    private String distributionType;

    @ApiModelProperty(name = "appRemark", value = "app备注")
    private String appRemark;

    @ApiModelProperty(name = "deliveryOrderNo", value = "配货单单号")
    private String deliveryOrderNo;

}
