package com.edc.erp.common.service;

import com.edc.erp.common.entity.OrderProcess;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.model.out.OrderProcessOut;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 订单类型流程(OrderProcess)表服务接口
 *
 * @author fxw
 * @since 2022-10-18 16:47:16
 */
public interface OrderProcessService extends BaseService<OrderProcess> {

    /**
     * 根据订单流程配置id和业务组织编码查询订单流程
     *
     * @param orderTypeConfig
     * @param bizOrgCode
     * @return
     */
    List<OrderProcessOut> findProcessListByOrderTypeConfigId(OrderTypeConfig orderTypeConfig, String bizOrgCode);
}
