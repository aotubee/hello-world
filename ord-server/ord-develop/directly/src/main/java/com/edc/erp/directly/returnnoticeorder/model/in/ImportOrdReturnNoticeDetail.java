package com.edc.erp.directly.returnnoticeorder.model.in;

import lombok.Data;

/**
 * 退货通知单导入门店明细
 *
 * @author lishaobo
 * @since 20232/05/13 17:30
 */
@Data
public class ImportOrdReturnNoticeDetail {

    /**
     * 商品代码
     */
    private String goodsCode;

    /**
     * 门店代码
     */
    private String storeCode;

    /**
     * 限量退货可退数量
     */
    private Integer qty;
}
