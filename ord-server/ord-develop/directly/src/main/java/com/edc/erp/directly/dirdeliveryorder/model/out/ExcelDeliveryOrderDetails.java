package com.edc.erp.directly.dirdeliveryorder.model.out;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 直营配货明细导出类
 *
 * @author weichao
 */
@Data
public class ExcelDeliveryOrderDetails implements Serializable {

    @Excel(name = "序号")
    private Integer index;

    @Excel(name = "商品代码", orderNum = "1")
    private String goodsCode;

    @Excel(name = "商品名称", orderNum = "2", width = 25)
    private String goodsName;

    @Excel(name = "品类属性", orderNum = "3")
    private String goodsTypeStr;

    @Excel(name = "商品条码", orderNum = "4", width = 18)
    private String barCode;

    @Excel(name = "包装规格", orderNum = "5")
    private String distributionSpecification;

    @Excel(name = "包装单位", orderNum = "6")
    private String distributionSpecificationUnit;

    @Excel(name = "要货数量", orderNum = "7")
    private BigDecimal orderQuantity;

    @Excel(name = "要货包装数", orderNum = "8", width = 12)
    private BigDecimal orderPackageQuantity;

    @Excel(name = "要货单价", orderNum = "9")
    private BigDecimal orderUnitPrice;

    @Excel(name = "要货金额", orderNum = "10")
    private BigDecimal orderAmount;

    @Excel(name = "配货数量", orderNum = "11")
    private BigDecimal distributionQuantity;

    @Excel(name = "配货包装数", orderNum = "12", width = 12)
    private BigDecimal distributionPackageQuantity;

    @Excel(name = "配货单价", orderNum = "13")
    private BigDecimal distributionUnitPrice;

    @Excel(name = "配货金额", orderNum = "14")
    private BigDecimal distributionAmount;

    @Excel(name = "实配数量", orderNum = "15")
    private BigDecimal deliveryQuantity;

    @Excel(name = "实配包装数", orderNum = "16", width = 12)
    private BigDecimal deliveryPackageQuantity;

    @Excel(name = "实配金额", orderNum = "17", width = 12)
    private BigDecimal deliveryAmount;

    @Excel(name = "实收数量", orderNum = "18")
    private BigDecimal arrivalQuantity;

    @Excel(name = "实收包装数", orderNum = "19", width = 12)
    private BigDecimal arrivalPackageQuantity;

    @Excel(name = "实收金额", orderNum = "20", width = 12)
    private BigDecimal arrivalAmount;

    /**
     * 配送方式
     */
    @Excel(name = "配送方式", orderNum = "21")
    private String distributionTypeValue;

    /**
     * 品类
     */
    @Excel(name = "小分类", orderNum = "22", width = 25)
    private String sortName;

    /**
     * 订单方代码
     */
    @Excel(name = "订单方", orderNum = "23")
    private String vendorCode;

    /**
     * 配货去税金额
     */
    @Excel(name = "配货去税金额", orderNum = "24")
    private BigDecimal distributionExceptTaxAmount;

    /**
     * 配货税额
     */
    @Excel(name = "配货税额", orderNum = "25")
    private BigDecimal distributionTaxAmount;

    /**
     * 仓储库存价
     */
    @Excel(name = "仓储库存价", orderNum = "26")
    private BigDecimal wrhPrice;

    /**
     * 仓储成本金额
     */
    @Excel(name = "仓储成本金额", orderNum = "27")
    private BigDecimal wrhCostAmount;

    /**
     * 仓储成本去税金额
     */
    @Excel(name = "仓储成本去税金额", orderNum = "28")
    private BigDecimal wrhExceptTaxAmount;

    /**
     * 仓储成本税额
     */
    @Excel(name = "仓储成本税额", orderNum = "29")
    private BigDecimal wrhTaxAmount;

    /**
     * 门店库存价
     */
    @Excel(name = "门店库存价", orderNum = "30")
    private BigDecimal storeStockPrice;

    /**
     * 门店成本金额
     */
    @Excel(name = "门店成本金额", orderNum = "31")
    private BigDecimal storeCostAmount;

    /**
     * 门店成本去税金额
     */
    @Excel(name = "门店成本去税金额", orderNum = "32")
    private BigDecimal storeExceptTaxAmount;

    /**
     * 门店成本税额
     */
    @Excel(name = "门店成本税额", orderNum = "33")
    private BigDecimal storeTaxAmount;

    /**
     * 税率
     */
    @Excel(name = "税率", orderNum = "34")
    private BigDecimal sellTax;

    /**
     * 税率
     */
    @Excel(name = "是否赠品", orderNum = "35")
    private String isGiftStr ;

    /**
     * 配货价
     */
    @Excel(name = "配送价", orderNum = "36")
    private BigDecimal distributionPrice;

    /**
     * 缺货数
     */
    @Excel(name = "缺货数", orderNum = "37")
    private BigDecimal stockoutQuantity;

    @Excel(name = "采购单号", orderNum = "38")
    private String purchaseNo;

    @Excel(name = "效期", orderNum = "39")
    private String expiry;
    /**
     * 税率
     */
    @Excel(name = "备注", orderNum = "40")
    private String remark ;

    @Excel(name = "发票类型", orderNum = "41")
    private String invoiceTypeStr ;
}
