package com.edc.erp.directly.returnorder.model.excel;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 导入接受参数VO
 * @author yaojinpeng
 * @since 2022/10/26 17:10
 */
@Data
public class ImportOrdReturnGoodsVO extends BaseEntity {

    /**
     * 商品代码
     */
    private String goodsCode;

//    /**
//     *
//     */
//    private BigDecimal packingNumber;

    /**
     * 申请退货数量
     */
    private BigDecimal applyReturnQuantity;

    /**
     * 退货单价
     */
    private BigDecimal returnUnitPrice;

    private String expiry;
}
