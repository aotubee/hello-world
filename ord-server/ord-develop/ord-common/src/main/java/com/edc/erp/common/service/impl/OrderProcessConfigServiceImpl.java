package com.edc.erp.common.service.impl;

import com.edc.erp.common.entity.OrderProcessConfig;import com.edc.erp.common.mapper.OrderProcessConfigMapper;
import com.edc.erp.common.model.out.OrderProcessConfigOut;
import com.edc.erp.common.service.OrderProcessConfigService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 *  订单类型流程配置表(OrderProcessConfig)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:49:00
 */
@Service
@RequiredArgsConstructor
public class OrderProcessConfigServiceImpl extends BaseServiceImpl<OrderProcessConfig> implements OrderProcessConfigService {
     
     private final OrderProcessConfigMapper orderProcessConfigMapper;
}
