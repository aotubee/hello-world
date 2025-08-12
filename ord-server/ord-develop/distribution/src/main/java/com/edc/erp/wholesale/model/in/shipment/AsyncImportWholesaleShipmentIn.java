//package com.edc.erp.wholesale.model.in.shipment;
//
//import com.alibaba.excel.annotation.ExcelProperty;
//import lombok.Data;
//
//import java.io.Serializable;
//import java.math.BigDecimal;
//
///**
// * @ClassName AysncImportWholesaleShipmentIn
// * @Description TODO
// * @Author ZhangYao
// * @CreateTime 2023/12/18 15:37
// **/
//@Data
//public class AsyncImportWholesaleShipmentIn implements Serializable {
//    private static final long serialVersionUID = -6302218045075327580L;
//
//    /** 客户代码 */
//    @ExcelProperty(value = "客户代码", index = 0)
//    private String clientCode;
//
//    /** 出库仓位 */
//    @ExcelProperty(value = "出库仓位", index = 1)
//    private String shipmentStockCode;
//
//    @ExcelProperty(value = "配货方式", index = 2)
//    private String distributionType;
//
//    /** 物流单号 */
//    @ExcelProperty(value = "商品代码", index = 3)
//    private String goodsCode;
//
//    @ExcelProperty(value = "申请数量", index = 4)
//    private Integer applyQuantity;
//
//    @ExcelProperty(value = "单价", index = 5)
//    private BigDecimal unitPrice;
//
//    @ExcelProperty(value = "来源单号", index = 6)
//    private String sourceNo;
//
//}
