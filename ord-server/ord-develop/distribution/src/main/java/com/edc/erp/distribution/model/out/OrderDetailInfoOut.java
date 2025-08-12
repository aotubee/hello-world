package com.edc.erp.distribution.model.out;

import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 订货单明细出参
 * @since 2022/11/14 15:51
 */
@Data
@ApiModel(description = "订货单明细信息出参")
public class OrderDetailInfoOut extends OrderHeaderOut implements Serializable {
    private static final long serialVersionUID = 1641089711112631258L;

    @ApiModelProperty(name = "orderDetailList",value = "订货明细")
    private List<OrdDisOrderDetail> orderDetailList;
}
