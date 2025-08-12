package com.edc.erp.directly.dirdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 配货单详情实体
 * @since 2022/11/17 15:18
 */
@Data
public class ImportDirDeliveryDetailsOrder implements Serializable {
    private static final long serialVersionUID = -608503324008071350L;

    /**
     * 商品代码
     */
    private String goodsCode;

    /**
     * 要货数量
     */
    private BigDecimal orderQuantity;

    /** 采购单号 */
    private String purchaseNo;
}
