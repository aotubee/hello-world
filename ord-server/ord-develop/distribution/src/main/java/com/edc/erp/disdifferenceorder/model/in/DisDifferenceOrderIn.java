/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.disdifferenceorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 差异单入参查询类
 * @author weichao
 */
@Data
public class DisDifferenceOrderIn extends Page implements Serializable {

    /**
     * 差异单号
     */
    @ApiModelProperty(name = "differenceNo", value = "差异单号")
    private String differenceNo;

    /** 配销差异类型 */
    @ApiModelProperty(name = "differenceType", value = "配销差异类型")
    private String differenceType;
    /**
     * 差异单状态
     */
    @ApiModelProperty(name = "differenceStatus", value = "差异单状态")
    private String differenceStatus;

    /**
     * 配货单号
     */
    @ApiModelProperty(name = "deliveryOrderNo", value = "配货单号")
    private String deliveryOrderNo;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "position", value = "仓位代码")
    private String position;

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
     * 组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

    /**
     * 開始时间
     */
    @ApiModelProperty(name = "createStartTime", value = "開始时间")
    private String createStartTime;

    /**
     * 結束时间
     */
    @ApiModelProperty(name = "createEndTime", value = "結束时间")
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
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 是否红冲单 */
    @ApiModelProperty(name = "isReversalOrder", value = "是否红冲单")
    private Integer  isReversalOrder;

    /** 是否被红冲 */
    @ApiModelProperty(name = "isRedRush", value = "是否被红冲")
    private Integer  isReversal;

    /** 创建月份 */
    @ApiModelProperty(name = "createMonth", value = "创建月份")
    private String createMonth;

    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;

    /** 物流单号 */
    @ApiModelProperty(name = "logisticsNo", value = "物流单号")
    private String logisticsNo;

    /**
     * 批准开始时间
     */
    @ApiModelProperty(name = "approvalTimeBegin", value = "批准开始时间")
    private LocalDateTime approvalTimeBegin;

    /**
     * 发货结束时间
     */
    @ApiModelProperty(name = "approvalTimeEnd", value = "批准结束时间")
    private LocalDateTime approvalTimeEnd;

    /** 仓位代码集合 */
    @ApiModelProperty(name = "stockCodeList", value = "仓位代码集合")
    private List<String> stockCodeList;
}
