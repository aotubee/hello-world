package com.edc.erp.wholesale.model.excel.shipment;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * description 批发出货单错误信息导出
 *
 * @author gusiyuan
 * @since 2023/10/12 18:41
 */
@Data
public class ExportErrorWholesaleShipmentInfo implements Serializable {

    private static final long serialVersionUID = 2838934363293897254L;

    /** 客户代码 */
    @Excel(name = "客户代码",orderNum="1",width = 15)
    private String clientCode;

    /** 出库仓位 */
    @Excel(name = "出库仓位",orderNum="2",width = 10)
    private String shipmentStockCode;

    /** 商品代码 */
    @Excel(name = "商品代码",orderNum="3",width = 10)
    private String goodsCode;

    /** 申请数量 */
    @Excel(name = "申请数量",orderNum="4",width = 10)
    private Integer applyQuantity;

    /** 单价 */
    @Excel(name = "单价",orderNum="5",width = 10)
    private BigDecimal unitPrice;

    /** 来源单号 */
    @Excel(name = "来源单号",orderNum="6",width = 15)
    private String sourceNo;

    /** 错误信息 */
    @Excel(name = "错误信息",orderNum="7",width = 30)
    private String errMsg;
}
