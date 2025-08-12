package com.edc.erp.common.model.out.fund;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 资管校验出参
 * @author weichao
 *
 */
@Data
public class ForeignAccountFundOut implements Serializable {
    /**
     * 可用金额
     */
    private BigDecimal availableAmount;
    /**
     * 可用授信金额
     */
    private BigDecimal credit;


}
