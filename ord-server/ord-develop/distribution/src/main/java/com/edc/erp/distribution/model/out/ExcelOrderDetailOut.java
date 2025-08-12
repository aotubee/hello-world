package com.edc.erp.distribution.model.out;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 订货单明细导出类
 * @since 2022/11/15 12:28
 */
@Data
public class ExcelOrderDetailOut {

    @Excel(name = "序号")
    private Integer index;

    @Excel(name = "商品代码")
    private String goodsCode;

    @Excel(name = "商品名称", width = 30)
    private String goodsName;

    @Excel(name = "商品条码", width = 20)
    private String barCode;

    @Excel(name = "包装规格")
    private String distributionSpecification;

    @Excel(name = "订货数量", numFormat = "#.##")
    private BigDecimal quantity;

    @Excel(name = "订货包装数", numFormat = "#.##")
    private BigDecimal packageQuantity;

    @Excel(name = "支付单价", numFormat = "#.####")
    private BigDecimal orderUnitPrice;

    @Excel(name = "支付金额", numFormat = "#.####")
    private BigDecimal payUnitTotalPrice;

    @Excel(name = "实付单价", numFormat = "#.####")
    private BigDecimal realUnitPrice;

    @Excel(name = "实付金额", numFormat = "#.####")
    private BigDecimal realUnitTotalPrice;

    @Excel(name = "配销价", numFormat = "#.####")
    private BigDecimal originalPrice;

    @Excel(name = "配货金额", numFormat = "#.####")
    private BigDecimal originalUnitTotalPrice;

    @Excel(name = "活动价", numFormat = "#.####")
    private BigDecimal activityUnitPrice;

    @Excel(name = "活动类型")
    private String activityType;

    @Excel(name = "活动单号", width = 20)
    private String activityCode;

    @Excel(name = "赠品标识")
    private String isGiftStr;

    @Excel(name = "包装单位")
    private String distributionSpecificationUnit;

    @Excel(name = "品类属性")
    private String goodsTypeStr;

    @Excel(name = "仓位")
    private String positionName;

    @Excel(name = "配送方式")
    private String distributionType;

    @Excel(name = "商品小分类", width = 25)
    private String smallSortStr;

    @Excel(name = "是否可退")
    private String allowDistributionReturnStr;

    @Excel(name = "建议零售价", numFormat = "#.####", width = 20)
    private BigDecimal suggestedRetailPrice;
}
