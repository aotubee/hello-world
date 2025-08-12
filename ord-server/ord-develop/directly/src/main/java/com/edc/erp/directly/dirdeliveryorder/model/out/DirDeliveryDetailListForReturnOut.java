package com.edc.erp.directly.dirdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName QueryDirDeliveryDetailListForRerurnOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/7/24 17:21
 **/
@Data
public class DirDeliveryDetailListForReturnOut implements Serializable {
    private static final long serialVersionUID = -5076060011740551085L;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "expiry", value = "效期码")
    private String expiry;

    @ApiModelProperty(name = "arrivalQuantity", value = "实收数量")
    private BigDecimal arrivalQuantity;
}
