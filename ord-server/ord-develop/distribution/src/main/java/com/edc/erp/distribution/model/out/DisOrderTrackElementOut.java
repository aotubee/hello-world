package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 订单追踪详细出参
 * @since 2022/10/18 14:28
 */
@Data
public class DisOrderTrackElementOut implements Serializable {

    @ApiModelProperty(name = "orderStatus",value = "状态")
    private String orderStatus;

    @ApiModelProperty(name = "orderTrackOutList",value = "追踪集合")
    private List<DisOrderTrackOut> orderTrackOutList;
}
