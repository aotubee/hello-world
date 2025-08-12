package com.edc.erp.wholesale.model.in.shipment;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 批发出货单查询入参
 * @author lx
 * @since 2022-10-18 14:27:20
 */
@Data
public class QueryShipmentIn extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 出货单号 */
    @ApiModelProperty(name = "shipmentNo", value = "出货单号")
    private String shipmentNo;

    /** 出货状态 */
    @ApiModelProperty(name = "shipmentStatus", value = "出货状态")
    private String shipmentStatus;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 开始创建时间 */
    @ApiModelProperty(value = "创建时间起")
    private String beginCreateTime;

    /** 截至创建时间 */
    @ApiModelProperty(value = "创建时间止")
    private String endCreateTime;

    /** 出库仓储 */
    @ApiModelProperty(name = "shipmentWrh", value = "出库仓储")
    private String shipmentWrh;

    /** 出库仓位 */
    @ApiModelProperty(name = "shipmentStockCode", value = "出库仓位")
    private String shipmentStockCode;

    /** 是否红冲单 */
    @ApiModelProperty(name = "isReversalOrder", value = "是否红冲单")
    private Integer isReversalOrder;

    /** 是否被红冲 */
    @ApiModelProperty(name = "isReversal", value = "是否被红冲")
    private Integer isReversal;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 批发出货单id集合 */
    @ApiModelProperty(name = "wholesaleShipmentIds", value = "批发出货单id集合")
    private List<Long> wholesaleShipmentIds;

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

    @ApiModelProperty(name = "sourceNo", value = "来源单号")
    private String sourceNo;

    @ApiModelProperty(name = "trackingNo", value = "物流单号")
    private String trackingNo;

    @ApiModelProperty(name = "distributionType", value = "配货方式")
    private String distributionType;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;


    @ApiModelProperty(name = "pushPurTimeBegin", value = "推送采购时间开始")
    private LocalDateTime pushPurTimeBegin;

    @ApiModelProperty(name = "pushPurTimeEnd", value = "推送采购时间结束")
    private LocalDateTime pushPurTimeEnd;
}
