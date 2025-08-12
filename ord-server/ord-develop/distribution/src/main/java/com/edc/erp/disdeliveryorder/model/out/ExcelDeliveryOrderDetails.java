package com.edc.erp.disdeliveryorder.model.out;

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

    @ExcelProperty(value = "序号")
    private Integer index;

    @ExcelProperty(value = "商品代码", index = 1)
    private String goodsCode;

    @ExcelProperty(value = "商品名称", index = 2)
    private String goodsName;

    @ExcelProperty(value = "品类属性", index = 3)
    private String goodsTypeStr;

    @ExcelProperty(value = "商品条码", index = 4)
    private String barCode;

    @ExcelProperty(value = "包装规格", index = 5)
    private String distributionSpecification;

    @ExcelProperty(value = "包装单位", index = 6)
    private String distributionSpecificationUnit;

    @ExcelProperty(value = "要货数量", index = 7)
    private BigDecimal orderQuantity;

    @ExcelProperty(value = "要货包装数", index = 8)
    private BigDecimal orderPackageQuantity;

    @ExcelProperty(value = "要货单价", index = 9)
    private BigDecimal orderUnitPrice;

    @ExcelProperty(value = "要货金额", index = 10)
    private BigDecimal orderAmount;

    @ExcelProperty(value = "配货数量", index = 11)
    private BigDecimal distributionQuantity;

    @ExcelProperty(value = "配货包装数", index = 12)
    private BigDecimal distributionPackageQuantity;

    @ExcelProperty(value = "配货单价", index = 13)
    private BigDecimal distributionUnitPrice;

    @ExcelProperty(value = "配货金额", index = 14)
    private BigDecimal distributionAmount;

    @ExcelProperty(value = "实配数量", index = 15)
    private BigDecimal deliveryQuantity;

    @ExcelProperty(value = "实配包装数", index = 16)
    private BigDecimal deliveryPackageQuantity;

    @ExcelProperty(value = "实配金额", index = 17)
    private BigDecimal deliveryAmount;

    @ExcelProperty(value = "实收数量", index = 18)
    private BigDecimal arrivalQuantity;

    @ExcelProperty(value = "实收包装数", index = 19)
    private BigDecimal arrivalPackageQuantity;

    @ExcelProperty(value = "实收金额", index = 20)
    private BigDecimal arrivalAmount;

    /**
     * 配送方式
     */
    @ExcelProperty(value = "配送方式", index = 21)
    private String distributionTypeValue;

    /**
     * 品类
     */
    @ExcelProperty(value = "小分类", index = 22)
    private String sortName;

    /**
     * 订单方代码
     */
    @ExcelProperty(value = "订单方", index = 23)
    private String vendorCode;

    /**
     * 配货去税金额
     */
    @ExcelProperty(value = "配货去税金额", index = 24)
    private BigDecimal distributionExceptTaxAmount;

    /**
     * 配货税额
     */
    @ExcelProperty(value = "配货税额", index = 25)
    private BigDecimal distributionTaxAmount;

    /**
     * 仓储库存价
     */
    @ExcelProperty(value = "仓储库存价", index = 26)
    private BigDecimal wrhPrice;

    /**
     * 仓储成本金额
     */
    @ExcelProperty(value = "仓储成本金额", index = 27)
    private BigDecimal wrhCostAmount;

    /**
     * 仓储成本去税金额
     */
    @ExcelProperty(value = "仓储成本去税金额", index = 28)
    private BigDecimal wrhExceptTaxAmount;

    /**
     * 仓储成本税额
     */
    @ExcelProperty(value = "仓储成本税额", index = 29)
    private BigDecimal wrhTaxAmount;

    /**
     * 门店库存价
     */
    @ExcelProperty(value = "门店库存价", index = 30)
    private BigDecimal storeStockPrice;

    /**
     * 门店成本金额
     */
    @ExcelProperty(value = "门店成本金额", index = 31)
    private BigDecimal storeCostAmount;

    /**
     * 门店成本去税金额
     */
    @ExcelProperty(value = "门店成本去税金额", index = 32)
    private BigDecimal storeExceptTaxAmount;

    /**
     * 门店成本税额
     */
    @ExcelProperty(value = "门店成本税额", index = 33)
    private BigDecimal storeTaxAmount;

    /**
     * 税率
     */
    @ExcelProperty(value = "税率", index = 34)
    private BigDecimal sellTax;

    /**
     * 税率
     */
    @ExcelProperty(value = "是否赠品", index = 35)
    private String isGiftStr ;

    /**
     * 配货价
     */
    @ExcelProperty(value = "配送价", index = 36)
    private BigDecimal distributionPrice;

    /**
     * 缺货数
     */
    @ExcelProperty(value = "缺货数", index = 37)
    private BigDecimal stockoutQuantity;
    @ExcelProperty(value = "采购单号", index = 38)
    private String purchaseNo;

    @ExcelProperty(value = "效期", index = 39)
    private String expiry;
    /**
     * 税率
     */
    @ExcelProperty(value = "备注", index = 40)
    private String remark ;

    @ExcelProperty(value = "发票类型", index = 41)
    private String invoiceTypeStr ;
}
