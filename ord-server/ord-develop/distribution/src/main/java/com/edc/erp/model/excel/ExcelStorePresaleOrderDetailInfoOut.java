package com.edc.erp.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName StorePresaleOrderInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/30 15:09
 **/
@Data
public class ExcelStorePresaleOrderDetailInfoOut implements Serializable {
    private static final long serialVersionUID = 2552110065425516978L;
    @Excel(name = "序号", width = 5)
    private Integer index;
    @Excel(name = "商品代码", orderNum = "1", width = 10)
    private String goodsCode;
    @Excel(name = "商品名称", orderNum = "2", width = 25)
    private String goodsName;
    @Excel(name = "商品条码", orderNum = "3", width = 15)
    private String barCode;
    @Excel(name = "包装规格", orderNum = "4", width = 10)
    private String packageSpecification;
    @Excel(name = "订货数量", orderNum = "5", width = 10)
    private Integer goodsQuantity;
    @Excel(name = "订货包装数", orderNum = "6", width = 15)
    private BigDecimal packageQuantity;
    @Excel(name = "包装价", orderNum = "7", width = 10)
    private BigDecimal distributionPrice;
    @Excel(name = "配销金额", orderNum = "8", width = 10)
    private BigDecimal distributionAmount;
    // @Excel(name = "应付金额", orderNum = "9", width = 10)
    // private BigDecimal payAmount;
    @Excel(name = "赠品标识", orderNum = "9", width = 10)
    private String giftStr;
    @Excel(name = "预售活动号", orderNum = "10", width = 20)
    private String presaleActivityNo;
}
