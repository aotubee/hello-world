package com.edc.erp.directly.dirdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * description 配货单打印出参
 *
 * @author gusiyuan
 * @since 2023/10/9 15:58
 */
@Data
public class DirDeliveryOrderPrintOut implements Serializable {

    private static final long serialVersionUID = -8071211370426941328L;
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    private Long id;

    /** 配销单号 */
    @ApiModelProperty(name = "deliveryOrderNo", value = "配销单号")
    private String deliveryOrderNo;

    /** 配销单状态 */
    @ApiModelProperty(name = "deliveryStatusCode", value = "配销单状态")
    private String deliveryStatusCode;

    /** 配销单状态 */
    @ApiModelProperty(name = "deliveryStatusName", value = "配销单状态中文")
    private String deliveryStatusName;

    /** 仓储代码 */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /** 仓位名称 */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;

    /** 门店名称 */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 实配数量 */
    @ApiModelProperty(name = "deliveryQuantity", value = "实配数量")
    private BigDecimal deliveryQuantity;

    /** 实配包装数量 */
    @ApiModelProperty(name = "deliveryPackQuantity", value = "实配包装数量")
    private BigDecimal deliveryPackQuantity;

    /** 实配金额 */
    @ApiModelProperty(name = "deliveryAmount", value = "实配金额")
    private BigDecimal deliveryAmount;

    /** 发货日期 */
    @ApiModelProperty(name = "deliveryTime", value = "发货日期")
    private LocalDateTime deliveryTime;

    /** 配货日期 */
    @ApiModelProperty(name = "distributionTime", value = "配货日期")
    private LocalDateTime distributionTime;

    /** 收货日期 */
    @ApiModelProperty(name = "receiveTime", value = "收货日期")
    private LocalDateTime receiveTime;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 创建时间 */
    @ApiModelProperty(name = "dtlPrintOuts", value = "配货单明细")
    private List<DirDeliveryOrderDtlPrintOut> dtlPrintOuts;

    /** 门店签名 */
    @ApiModelProperty(name = "signature", value = "门店签名")
    private String signature;


    /** 订货数量 */
    @ApiModelProperty(name = "orderQuantity", value = "订货数量")
    private BigDecimal orderQuantity;

    /** 订货包装数量 */
    @ApiModelProperty(name = "orderPackQuantity", value = "订货包装数量")
    private BigDecimal orderPackQuantity;
}
