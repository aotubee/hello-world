package com.edc.erp.directly.dirdeliveryorder.model.out;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName DeliveryOrderArrivalDataOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/18 16:23
 **/
@Data
public class DirDeliveryOrderArrivalDataOut implements Serializable {
    private static final long serialVersionUID = -6188614538444608690L;

    private BigDecimal totalArrivalQuantity;

    private BigDecimal totalArrivalAmount;
}
