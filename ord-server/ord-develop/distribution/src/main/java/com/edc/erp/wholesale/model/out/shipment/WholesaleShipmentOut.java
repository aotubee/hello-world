package com.edc.erp.wholesale.model.out.shipment;

import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 批发出货单 查询入参类
 * @author lx
 * @since 2022-10-18 14:37:31
 */
@Data
public class WholesaleShipmentOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    private Long id;

    /** 出货单号 */
    @ApiModelProperty(name = "shipmentNo", value = "出货单号")
    private String shipmentNo;

    /** 出货状态 */
    @ApiModelProperty(name = "shipmentStatus", value = "出货状态")
    private String shipmentStatus;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 价格组代码 */
    @ApiModelProperty(name = "priceGroupCode", value = "价格组代码")
    private String priceGroupCode;

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

    /** 申请数量 */
    @ApiModelProperty(name = "applicationQuantity", value = "申请数量")
    private Integer applicationQuantity;

    /** 申请金额 */
    @ApiModelProperty(name = "applicationAmount", value = "申请金额")
    private BigDecimal applicationAmount;

    /** 审核数量 */
    @ApiModelProperty(name = "auditQuantity", value = "审核数量")
    private Integer auditQuantity;

    /** 审核金额 */
    @ApiModelProperty(name = "auditAmount", value = "审核金额")
    private BigDecimal auditAmount;

    /** 物流单号 */
    @ApiModelProperty(name = "trackingNo", value = "物流单号")
    private String trackingNo;

    /** 来源单号 */
    @ApiModelProperty(name = "sourceNo", value = "来源单号")
    private String sourceNo;

    /** 出库数量 */
    @ApiModelProperty(name = "shipmentQuantity", value = "出库数量")
    private Integer shipmentQuantity;

    /** 实际出库金额 */
    @ApiModelProperty(name = "practicalShipmentAmount", value = "实际出库金额")
    private BigDecimal practicalShipmentAmount;


    /** 出货状态 - 中文 */
    @ApiModelProperty(name = "shipmentStatusStr", value = "出货状态 - 中文")
    private String shipmentStatusStr;

    @ApiModelProperty(name = "distributionTypeStr", value = "配货方式-中文")
    private String distributionTypeStr;

    /** 客户配送信息对象 */
    @ApiModelProperty(name = "clientDistInfoOut",value = "客户配送信息对象")
    private ClientDistInfoOut clientDistInfoOut;

    /** 出库仓储名称 */
    @ApiModelProperty(name = "warehouseName", value = "出库仓储名称")
    private String warehouseName;

    /** 出库仓位名称 */
    @ApiModelProperty(name = "stockName", value = "出库仓位名称")
    private String stockName;

    /** 配送信息ID */
    @ApiModelProperty(name = "distributionInfoId", value = "配送信息ID")
    private Integer distributionInfoId;

    /** 收货人 */
    @ApiModelProperty(name = "consignee", value = "收货人")
    private String consignee;

    /** 收货人电话 */
    @ApiModelProperty(name = "consigneePhone", value = "收货人电话")
    private String consigneePhone;

    /** 详细地址 */
    @ApiModelProperty(name = "addressDetail", value = "详细地址")
    private String addressDetail;


    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 修改人 */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 发货日期 */
    @ApiModelProperty(name = "deliveryTime", value = "发货日期")
    private LocalDateTime deliveryTime;


    @ApiModelProperty(name = "distributionType", value = "配货方式")
    private String distributionType;

    @ApiModelProperty(name = "driverInfo", value = "司机信息")
    private String driverInfo;

    @ApiModelProperty(name = "auditTime", value = "审核时间")
    private LocalDateTime auditTime;

    /** 审核时间 */
    @ApiModelProperty(name = "auditTimeStr", value = "审核时间中文")
    private String auditTimeStr;

    /** 推送采购时间 */
    @ApiModelProperty(name = "pushPurTime", value = "推送采购时间")
    private LocalDateTime pushPurTime;

    /** 推送采购时间 */
    @ApiModelProperty(name = "pushPurProgress", value = "推送采购进度")
    private Integer pushPurProgress;
}
