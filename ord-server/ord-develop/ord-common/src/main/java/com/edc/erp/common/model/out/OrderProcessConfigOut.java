package com.edc.erp.common.model.out;

import com.edc.erp.common.entity.OrderProcessConfig;
import lombok.Data;

import java.util.List;

/**
 * @author fxw
 * @description: 订单流程配置出参
 * @since 2022/10/18 16:49
 */
@Data
public class OrderProcessConfigOut extends OrderProcessConfig {

    private List<OrderProcessConfigItemOut> configItemList;
}
