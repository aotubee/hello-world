package com.edc.erp.directly.returnnoticeorder.model.out;

import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeStore;
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
public class OrdReturnNoticeStoreOut extends OrdDirReturnNoticeStore implements Serializable {

    /**
     * 门店类型
     */
    private String storeType;

    /**
     * 可退数量
     */
    private BigDecimal returnNum;
}
