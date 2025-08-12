package com.edc.erp.directly.service.impl;

import com.edc.erp.common.mapper.OrderProcessConfigMapper;
import com.edc.erp.directly.entity.DirOrderProcessConfig;
import com.edc.erp.directly.service.DirOrderProcessConfigService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 *  订单类型流程配置表(OrderProcessConfig)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:49:00
 */
@Service
@RequiredArgsConstructor
public class DirOrderProcessConfigServiceImpl extends BaseServiceImpl<DirOrderProcessConfig> implements DirOrderProcessConfigService {
     
     private final OrderProcessConfigMapper orderProcessConfigMapper;
}
