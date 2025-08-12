package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 作废分货单关联的订货单列表使用的出参类
 * @author lee
 */
@Data
public class DisJoinOrderOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "id", value = "订货单主键")
    private Integer id;

    @ApiModelProperty(name = "orderNo", value = "订货单号")
    private String orderNo;

    @ApiModelProperty(name = "orderStatusCode", value = "订货单状态")
    private String orderStatusCode;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "orderStatusCodeStr", value = "订货单状态中文")
    private String orderStatusCodeStr;

}
