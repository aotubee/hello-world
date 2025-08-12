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
public class FirstOrderFreezeDeliveryOrderOut extends FreezeDeliveryOrderBaseInfo implements Serializable {

    private static final long serialVersionUID = -2521515850077491233L;

    @ApiModelProperty(name = "firstOrderId", value = "铺货单ID")
    private Long firstOrderId;

    @ApiModelProperty(name = "firstOrderNo", value = "铺货单单号")
    private String firstOrderNo;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;


}
