package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName DSSOrderInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/6/18 10:39
 **/
@Data
public class DssOrderInfoOut implements Serializable {

    private static final long serialVersionUID = -8082553551475309090L;

    @ApiModelProperty(name = "sDh", value = "订单号")
    private String sDh;

    @ApiModelProperty(name = "sFdbh", value = "门店代码")
    private String sFdbh;

    @ApiModelProperty(name = "dAddtime", value = "订单生成时间")
    private LocalDateTime dAddtime;

    @ApiModelProperty(name = "sSpbh", value = "商品代码")
    private String sSpbh;

    @ApiModelProperty(name = "nsl", value = "数量")
    private BigDecimal nsl;
}
