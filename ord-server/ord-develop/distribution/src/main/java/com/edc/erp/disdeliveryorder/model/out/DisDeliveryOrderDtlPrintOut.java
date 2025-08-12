package com.edc.erp.disdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * description 配销单明细打印出参
 *
 * @author gusiyuan
 * @since 2023/10/9 16:23
 */
@Data
public class DisDeliveryOrderDtlPrintOut implements Serializable {

    private static final long serialVersionUID = -6190749581824399608L;

    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    private Long id;

    /** 配销单主键 */
    @ApiModelProperty(name = "deliveryOrderId", value = "配销单主键")
    private Long deliveryOrderId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品条形码 */
    @ApiModelProperty(name = "barCode", value = "商品条形码")
    private String barCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;

    /** 实配数量 */
    @ApiModelProperty(name = "deliveryQuantity", value = "实配数量")
    private BigDecimal deliveryQuantity;

    /** 实配包装数 */
    @ApiModelProperty(name = "deliveryPackageQuantity", value = "实配包装数")
    private BigDecimal deliveryPackageQuantity;

    /** 实配金额 */
    @ApiModelProperty(name = "deliveryAmount", value = "实配金额")
    private BigDecimal deliveryAmount;

    /** 商品配货规格（1*12） */
    @ApiModelProperty(name = "distributionSpecification", value = "商品配货规格（1*12）")
    private String distributionSpecification;

    /** 商品配货规格单位（件/箱） */
    @ApiModelProperty(name = "distributionSpecificationUnit", value = "商品配货规格单位（件/箱）")
    private String distributionSpecificationUnit;
    /** 要货数量 */
    @ApiModelProperty(name = "orderQuantity", value = "订货数量")
    private BigDecimal orderQuantity;

    /** 要货包装数 */
    @ApiModelProperty(name = "orderPackageQuantity", value = "订货包装数")
    private BigDecimal orderPackageQuantity;

    /** 配货单价 */
    @ApiModelProperty(name = "distributionUnitPrice", value = "配货单价")
    private BigDecimal distributionUnitPrice;

    /** 保质期 */
    @ApiModelProperty(name = "expirationDate", value = "保质期")
    private String expirationDate;

    /** 生产日期 */
    @ApiModelProperty(name = "produceDate", value = "生产日期")
    private LocalDate produceDate;


    /** 集货单价 */
    @ApiModelProperty(name = "orderUnitPrice", value = "集货单价")
    private BigDecimal orderUnitPrice;
}
