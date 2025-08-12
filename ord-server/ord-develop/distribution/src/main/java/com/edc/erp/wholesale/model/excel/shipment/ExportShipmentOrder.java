package com.edc.erp.wholesale.model.excel.shipment;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 批发出货单导入类
 * @author lx
 * @since 2022-11-03 16:11:09
 */
@Data
public class ExportShipmentOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品代码 */
    @Excel(name = "商品代码",orderNum="1",width = 15)
    private String goodsCode;

    /** 商品名称 */
    @Excel(name = "商品名称",orderNum="2",width = 25)
    private String goodsName;

    /** 品类属性 */
    @Excel(name = "品类属性",orderNum="3",width = 5)
    private String goodsTypeStr;

    /** 商品条码 */
    @Excel(name = "商品条码",orderNum="4",width = 22)
    private String barCode;

    /** 包装规格 */
    @Excel(name = "包装规格",orderNum="5",width = 5)
    private String packageSpecification;

    /** 包装单位 */
    @Excel(name = "包装单位",orderNum="6",width = 5)
    private String packageUnit;

    /** 申请数量 */
    @Excel(name = "申请数量",orderNum="7",width = 5)
    private Integer applyQuantity;

    /** 申请包装数 */
    @Excel(name = "申请包装数",orderNum="8",width = 8)
    private Integer applyPackageNum;

    /** 单价 */
    @Excel(name = "单价",orderNum="9",width = 8)
    private BigDecimal unitPrice;

    /** 审请金额 */
    @Excel(name = "审请金额",orderNum="10",width = 8)
    private BigDecimal applyAmount;

    /** 审核数量 */
    @Excel(name = "审核数量",orderNum="11",width = 5)
    private Integer auditQuantity;

    /** 出库数量 */
    @Excel(name = "出库数量",orderNum="12",width = 5)
    private Integer shipmentQuantity;

    /** 库存价 */
    @Excel(name = "库存价",orderNum="13",width = 8)
    private BigDecimal inventoryPrice;

    /** 实际出库金额 */
    @Excel(name = "实际出库金额",orderNum="14",width = 8)
    private BigDecimal practicalShipmentAmount;

    /** 出库去税金额 */
    @Excel(name = "出库去税金额",orderNum="15",width = 8)
    private BigDecimal shipmentNetProfit;

    /** 出库税额 */
    @Excel(name = "出库税额",orderNum="16",width = 8)
    private BigDecimal shipmentTax;

    /** 成本金额 */
    @Excel(name = "成本金额",orderNum="17",width = 8)
    private BigDecimal costAmount;

    /** 成本去税金额 */
    @Excel(name = "成本去税金额",orderNum="18",width = 8)
    private BigDecimal costNetProfitAmount;

    /** 成本税额 */
    @Excel(name = "成本税额",orderNum="19",width = 8)
    private BigDecimal costTax;


}
