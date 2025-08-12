package com.edc.erp.common.model.in.fund;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName UnFrozenAndPayIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/7/21 11:03
 **/
@Data
public class UnFrozenAndPayIn implements Serializable {
    private static final long serialVersionUID = -6330182336612135242L;

    @ApiModelProperty(name = "rechargeLiquidationIn", value = "清算对象")
    private RechargeLiquidationIn rechargeLiquidationIn;

    @ApiModelProperty(name = "unFrozenBusinessNo", value = "解冻原单号")
    private String unFrozenBusinessNo;
}
