package com.edc.erp.common.model.out;

import com.edc.erp.common.entity.OrderProcess;
import lombok.Data;

import java.util.List;

/**
 * @author fxw
 * @description: 订单流程出参
 * @since 2022/10/18 16:46
 */
@Data
public class OrderProcessOut extends OrderProcess {

    private List<OrderProcessConfigOut> configList;
}
