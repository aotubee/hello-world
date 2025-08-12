package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName CheckUpperLowerGoodsOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/6/18 16:03
 **/
@Data
public class CheckUpperLowerGoodsOut implements Serializable {

    private static final long serialVersionUID = -382558334726278297L;

    @ApiModelProperty(value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(value = "配销允许上下限跑货")
    private Integer isUpLowLimitDis;

    @ApiModelProperty(value = "允许上下限跑货")
    private Integer isUpLowLimit;

    @ApiModelProperty(value = "配货规格1*10的10")
    private Integer qpc;

}
