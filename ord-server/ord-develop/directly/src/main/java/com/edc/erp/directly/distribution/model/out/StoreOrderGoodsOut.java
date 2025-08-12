package com.edc.erp.directly.distribution.model.out;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName StoreOrderGoodsOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/7 10:41
 **/
@Data
public class StoreOrderGoodsOut implements Serializable {
    private static final long serialVersionUID = -430798652362581971L;

    private String goodsCode;

    private BigDecimal totalQuantity;
}
