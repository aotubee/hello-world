package com.edc.erp.wholesale.model.excel.shipment;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * description 批发出货单导入模板
 *
 * @author gusiyuan
 * @since 2023/10/12 16:04
 */
@Data
public class ImportShipmentOrder implements Serializable {

    private static final long serialVersionUID = 7605070571664466693L;
    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 出库仓位 */
    @ApiModelProperty(name = "shipmentStockCode", value = "出库仓位")
    private String shipmentStockCode;

    @ApiModelProperty(name = "distributionType", value = "配货方式")
    private String distributionType;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 申请数量 */
    @ApiModelProperty(name = "applyQuantity", value = "申请数量")
    private Integer applyQuantity;

    /** 单价 */
    @ApiModelProperty(name = "unitPrice", value = "单价")
    private BigDecimal unitPrice;

    /** 来源单号 */
    @ApiModelProperty(name = "sourceNo", value = "来源单号")
    private String sourceNo;
}
