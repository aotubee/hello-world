/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.disdeliveryorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配销单入参查询类
 * @author liwenqiang
 */
@Data
public class DisDeliveryOrderIn extends Page implements Serializable {

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    @NotEmpty
    private String goodsCode;

    /**
     * 配货单号
     */
    @ApiModelProperty(name = "deliveryOrderNo", value = "配销单号")
    private String deliveryOrderNo;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /** 仓储代码 */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;


    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 出货类型
     */
    @ApiModelProperty(name = "shipmentTypeCode", value = "出货类型")
    private String shipmentTypeCode;

    /** 海鼎单号 */
    @ApiModelProperty(name = "hdDeliveryNo", value = "海鼎单号")
    private String hdDeliveryNo;

    /** 物流单号 */
    @ApiModelProperty(name = "logisticsNo", value = "物流单号")
    private String logisticsNo;

    /**
     * 配销单状态
     */
    @ApiModelProperty(name = "deliveryStatusCode", value = "配货单状态")
    private String deliveryStatusCode;

    /**
     * 差异单号
     */
    @ApiModelProperty(name = "differenceOrderNo", value = "差异单号")
    private String differenceOrderNo;

    /**
     * 订货订单单号
     */
    @ApiModelProperty(name = "purchaseOrderNo", value = "订货订单单号")
    private String purchaseOrderNo;

    /**
     * 创建开始时间
     */
    @ApiModelProperty(name = "createStartTime", value = "创建开始时间")
    private String createStartTime;

    /**
     * 创建结束时间
     */
    @ApiModelProperty(name = "createEndTime", value = "创建结束时间")
    private String createEndTime;

    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

    /**
     * 门店erp代码
     */
    private List<String> storeCodeList;
    /**
     * 组织代码
     */
    private String bizOrgCode;

    @ApiModelProperty(name = "requestOrderNo", value = "集货单号")
    private String requestOrderNo;
    /**
     * 配销方式
     */
    @ApiModelProperty(name = "distributionType", value = "配销方式")
    private String distributionType;

    /** 是否红冲单 */
    @ApiModelProperty(name = "isReversalOrder", value = "是否红冲单")
    private Integer isReversalOrder;

    /** 是否被红冲 */
    @ApiModelProperty(name = "isReversal", value = "是否被红冲")
    private Integer isReversal;

    /** 创建月份 */
    @ApiModelProperty(name = "createMonth", value = "创建月份")
    private String createMonth;

    /**
     * 发货开始时间
     */
    @ApiModelProperty(name = "deliveryTimeBegin", value = "发货开始时间")
    private LocalDateTime deliveryTimeBegin;

    /**
     * 发货结束时间
     */
    @ApiModelProperty(name = "deliveryTimeEnd", value = "发货结束时间")
    private LocalDateTime deliveryTimeEnd;

    /**
     * 仓位代码集合
     */
    @ApiModelProperty(name = "stockCodeList", value = "仓位代码集合")
    private List<String> stockCodeList;


    /**
     * 发货开始时间
     */
    @ApiModelProperty(name = "receiveTimeBegin", value = "收货开始时间")
    private LocalDateTime receiveTimeBegin;

    /**
     * 发货结束时间
     */
    @ApiModelProperty(name = "receiveTimeEnd", value = "收货结束时间")
    private LocalDateTime receiveTimeEnd;
}
