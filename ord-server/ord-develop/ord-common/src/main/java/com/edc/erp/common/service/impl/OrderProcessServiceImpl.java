package com.edc.erp.common.service.impl;

import com.edc.erp.common.entity.OrderProcess;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.mapper.OrderProcessMapper;
import com.edc.erp.common.model.out.OrderProcessOut;
import com.edc.erp.common.rpc.OrderProcessConfigItemClient;
import com.edc.erp.common.service.OrderProcessService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


/**
 * 订单类型流程(OrderProcess)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:47:16
 */
@Service
@RequiredArgsConstructor
public class OrderProcessServiceImpl extends BaseServiceImpl<OrderProcess> implements OrderProcessService {
     
     private final OrderProcessConfigItemClient orderProcessConfigItemClient;

     /**
      * 根据订单流程配置id和业务组织编码查询订单流程
      *
      * @param orderTypeConfig
      * @param bizOrgCode
      * @return
      */
     @Override
     public List<OrderProcessOut> findProcessListByOrderTypeConfigId(OrderTypeConfig orderTypeConfig, String bizOrgCode) {
          Response<List<OrderProcessOut>> orderProcessListResponse = orderProcessConfigItemClient.findProcessOutListByTypeConfigIdAndOrg(orderTypeConfig.getId().longValue(), bizOrgCode);
          if (!orderProcessListResponse.isSuccess() && CollectionUtils.isEmpty(orderProcessListResponse.getData())) {
               throw new BusinessException("订单类型" + orderTypeConfig.getOrderTypeName() + "未设置流程");
          }
          return orderProcessListResponse.getData();
     }
}
