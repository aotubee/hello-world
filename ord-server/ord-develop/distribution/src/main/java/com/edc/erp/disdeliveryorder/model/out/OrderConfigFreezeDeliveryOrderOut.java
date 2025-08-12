package com.edc.erp.disdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName OrderConfigDeliveryOrderOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/7/19 15:52
 **/
@Data
public class OrderConfigFreezeDeliveryOrderOut extends FreezeDeliveryOrderBaseInfo implements Serializable {

    private static final long serialVersionUID = -2819144034821846100L;

    @ApiModelProperty(name = "orderCycleId", value = "订货周期ID")
    private Long orderCycleId;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private String truncationDateTime;


}
