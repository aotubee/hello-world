package com.edc.erp.returnnoticeorder.model.in;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货通知单导入门店明细
 *
 * @author yaojinpeng
 * @since 2022/10/29 17:30
 */
@Data
public class ImportOrdReturnNoticeStore {

    /**
     * 门店代码
     */
    private String storeCode;

;

    /**
     * 限量退货可退数量
     */
    private BigDecimal qty;
}
