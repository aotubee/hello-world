package com.edc.erp.directly.distribution.model.in;

import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.directly.distribution.model.out.OrderCycleHeaderOut;
import lombok.Data;

import java.util.List;

/**
 * @author fxw
 * @description: 创建订单信息入参
 * @since 2022/10/17 17:50
 */
@Data
public class CreateOrderInfoIn {

    private List<OrderCycleHeaderOut> orderCycleHeaderOutList;

    private String sourceCode;

    private String loginUsername;

    private Long distributionOrderId;

    private String storeCode;

    private String bizOrgCode;

    private String orgCode;
}
