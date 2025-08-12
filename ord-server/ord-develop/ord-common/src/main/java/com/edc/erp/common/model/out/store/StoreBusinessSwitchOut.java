package com.edc.erp.common.model.out.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 门店是否可执行业务开关出参类
 * @author lee
 */
@Data
public class StoreBusinessSwitchOut implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "门店代码")
    private String storeCode;

    @ApiModelProperty(value = "门店名称")
    private String storeName;

    @ApiModelProperty(value = "业务类型")
    private String businessType;

    @ApiModelProperty(value = "是否可执行")
    private Integer isExecute;

    @ApiModelProperty(value = "门店状态")
    private String storeStatus;

    @ApiModelProperty(value = "门店状态中文")
    private String storeStatusStr;
}
