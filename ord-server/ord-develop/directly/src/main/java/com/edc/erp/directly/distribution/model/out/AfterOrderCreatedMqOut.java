package com.edc.erp.directly.distribution.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author fxw
 * @description: 创建订货单后发送消息对象
 * @since 2022/10/17 17:06
 */
@Data
public class AfterOrderCreatedMqOut extends BaseEntity {

    @ApiModelProperty(name = "orderIdMessageOutList", value = "订货单创建成功后向单据调度发送消息集合")
    private List<OrderIdMessageOut> orderIdMessageOutList;

    @ApiModelProperty(name = "isContainsNeedPayOrder", value = "本次是否含有需要支付的订货单")
    private Long isContainsNeedPayOrder;
}
