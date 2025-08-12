package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class CheckPresaleAdjustOrderDetailIn implements Serializable {
    private static final long serialVersionUID = 7424660849269221472L;
    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 调整类型
     */
    @ApiModelProperty(name = "adjustType", value = "调整类型 add增加 reduce扣减")
    private String adjustType;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 调整数量
     */
    @ApiModelProperty(name = "adjustQty", value = "调整数量")
    private BigDecimal adjustQty;

    /**
     * 预售活动号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
    private String presaleActivityNo;
}
