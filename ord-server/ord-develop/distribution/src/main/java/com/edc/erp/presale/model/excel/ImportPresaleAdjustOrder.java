package com.edc.erp.presale.model.excel;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ImportPresaleAdjustOrder implements Serializable {
    private static final long serialVersionUID = 3762876180686476941L;
    /**
     * 门店代码
     */
    private String storeCode;
    /**
     * 活动单号
     */
    private String presaleActivityNo;
    /**
     * 商品代码
     */
    private String goodsCode;
    /**
     * 调整类型
     */
    private String adjustType;
    /**
     * 调整数量
     */
    private BigDecimal adjustQty;
}
