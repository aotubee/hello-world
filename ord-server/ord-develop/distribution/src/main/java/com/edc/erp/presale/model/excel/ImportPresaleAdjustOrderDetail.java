package com.edc.erp.presale.model.excel;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ImportPresaleAdjustOrderDetail implements Serializable {
    private static final long serialVersionUID = 7024113967016724013L;
    /**
     * 活动单号
     */
    private String presaleActivityNo;
    /**
     * 商品代码
     */
    private String goodsCode;
    /**
     * 调整数量
     */
    private BigDecimal adjustQty;
}
