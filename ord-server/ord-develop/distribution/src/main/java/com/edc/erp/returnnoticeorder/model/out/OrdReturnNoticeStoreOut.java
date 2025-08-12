package com.edc.erp.returnnoticeorder.model.out;

import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeStore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * PpReturnNoticeStoreOut返回类
 *
 * @author yaojinpeng
 * @since 2022/10/21 16:28
 */
@Data
public class OrdReturnNoticeStoreOut extends OrdDisReturnNoticeStore implements Serializable {

    /**
     * 门店类型
     */
    private String storeType;

    /**
     * 可退数量
     */
    private BigDecimal returnNum;
}
