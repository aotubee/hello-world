package com.edc.erp.wholesale.model.in.shipment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName HSWholesaleDifferenceDetail
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/11 14:45
 **/
@Data
public class HSWholesaleDifferenceDetail implements Serializable {
    private static final long serialVersionUID = 3252049162440705307L;

    @ApiModelProperty(name = "goodsCode",value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "barCode",value = "商品条码")
    private String barCode;

    @ApiModelProperty(name = "qty",value = "退货数量")
    private BigDecimal qty;

    @ApiModelProperty(name = "line",value = "行号")
    private Integer line;

    @ApiModelProperty(name = "isGift",value = " 业务类型(1 销售/2 赠 品)")
    private String isGift;






}
