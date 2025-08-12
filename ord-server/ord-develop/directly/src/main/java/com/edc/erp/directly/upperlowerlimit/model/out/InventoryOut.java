package com.edc.erp.directly.upperlowerlimit.model.out;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存出参
 * @author weichao
 */
@Data
public class InventoryOut {

    /**
     * 商品ID
     */
    private Integer gid;

    /**
     * 门店代码
     */
    private String storeCode;

    /**
     * 库存数
     */
    private BigDecimal inventory;

    /**
     * 仓位ID
     */
    private Integer wrh;

    /**
     * 商品代码
     */
    private String goodsCode;
}
