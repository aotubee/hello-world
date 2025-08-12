package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * description 用户被授权的所有代码合集
 *
 * @author gusiyuan
 * @since 2024/11/13 18:54
1 */
@Data
public class EmpowerGroupAuthCodeOut implements Serializable {

    private static final long serialVersionUID = -6513889780489577351L;

    @ApiModelProperty(value = "授权可用的订单方代码合集")
    private List<String> authorizeVendorCodes;

    @ApiModelProperty(value = "授权可用的仓位代码合集")
    private List<String> authorizeStockCodes;
}
