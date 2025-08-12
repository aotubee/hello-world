package com.edc.erp.returnnoticeorder.model.out;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yaojinpeng
 * @since 2022/10/21 17:30
 */
@Data
public class OrdReturnNoticeOrderSkuReturnQuantityOut extends BaseEntity {

    private String goodsName;

    private BigDecimal totalReturnQuantity;
}
