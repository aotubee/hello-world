package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName SubmitPresaleOrderDetailIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/29 15:11
 **/
@Data
@ApiModel(value = "SubmitPresaleOrderGoodsIn", description = "提交预售订单商品入参")
public class SubmitPresaleOrderGoodsIn implements Serializable {
    private static final long serialVersionUID = -4772062137293089159L;

    @ApiModelProperty(name = "id", value = "明细id")
    private Long id;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

}
