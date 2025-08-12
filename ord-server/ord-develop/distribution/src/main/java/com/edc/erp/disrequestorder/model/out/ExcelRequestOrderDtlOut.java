/**
 * Copyright © 2010-2020 Everyday Chain. All rights reserved.
 */
package com.edc.erp.disrequestorder.model.out;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 集货单明细导出
 *
 * @author weichao
 */
@Data
public class ExcelRequestOrderDtlOut {

    @Excel(name = "序号")
    private Integer index;

    @Excel(name = "商品代码", orderNum = "1", width = 15)
    private String goodsCode;

    @Excel(name = "商品名称",orderNum = "2", width = 25)
    private String goodsName;

    @Excel(name = "商品条码", orderNum = "3", width = 15)
    private String barCode;

    @Excel(name = "包装规格", orderNum = "4")
    private String distributionSpecification;

    @Excel(name = "集货数量", orderNum = "5", width = 15, numFormat = "0")
    private BigDecimal quantity;

    @Excel(name = "集货包装数", orderNum = "6", width = 15, numFormat = "0")
    private BigDecimal packageQuantity;

    @Excel(name = "集货单价", orderNum = "7", numFormat = "0.0000")
    private BigDecimal price;

    @Excel(name = "集货金额", orderNum = "8", width = 15, numFormat = "0.0000")
    private BigDecimal requestOrderAmount;

    @Excel(name = "配销价", orderNum = "9", width = 15, numFormat = "0.0000")
    private BigDecimal originalUnitPrice;

    @Excel(name = "活动单号", orderNum = "10")
    private String activityNo;

    @Excel(name = "赠品标识", orderNum = "11", replace = {"赠品_1","主商品_0"})
    private Integer isGift;

    @Excel(name = "包装单位", orderNum = "12")
    private String distributionSpecificationUnit;

    /** 品类属性 */
    @Excel(name = "品类属性", orderNum = "13")
    private String goodsType;

    /** 仓位代码 */
    @Excel(name = "仓位", orderNum = "14")
    private String stock;

    @Excel(name = "配送方式", orderNum = "15")
    private String distributionType;

    @Excel(name = "小分类", orderNum = "16", width = 25)
    private String sortName;
    /** 是否可退 */
    @Excel(name = "是否可退", orderNum = "17")
    private String allowDistributionReturn;
}
