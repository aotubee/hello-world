package com.edc.erp.common.service.impl;

import com.edc.erp.common.entity.OrderProcessConfigItem;
import com.edc.erp.common.mapper.OrderProcessConfigItemMapper;
import com.edc.erp.common.rpc.OrderProcessConfigItemClient;
import com.edc.erp.common.service.OrderProcessConfigItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 订单类型流程选项配置(OrderProcessConfigItem)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:50:59
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessConfigItemServiceImpl implements OrderProcessConfigItemService {
     
     private final OrderProcessConfigItemMapper orderProcessConfigItemMapper;

     private final OrderProcessConfigItemClient orderProcessConfigItemClient;

     /**
      * 根据订单类型配置id、业务组织代码查询订单流程配置明细列表
      *
      * @param id
      * @param bizOrgCode
      * @return
      */
//     @Override
//     public List<OrderProcessConfigItemOut> findItemListByOrderProcessConfigId(Long id, String bizOrgCode) {
//          List<OrderProcessConfigItem> list = orderProcessConfigItemMapper.findItemListByOrderProcessConfigId(id, bizOrgCode);
//          if (CollectionUtils.isEmpty(list)) {
//               return null;
//          }
//          List<OrderProcessConfigItemOut> itemOutList = Lists.newArrayList();
//          list.stream().forEach(orderProcessConfigItem -> {
//               OrderProcessConfigItemOut orderProcessConfigItemOut = new OrderProcessConfigItemOut();
//               BeanUtils.copy(orderProcessConfigItem, orderProcessConfigItemOut);
//               itemOutList.add(orderProcessConfigItemOut);
//          });
//          return itemOutList;
//     }

     /**
      * 根据条件查询流程配置明细列表
      *
      * @param orderTypeConfigId
      * @param processCode
      * @param processCodeConfigCode
      * @param bizOrgCode
      * @return
      */
     @Override
     public List<OrderProcessConfigItem> findConfigItemListByParameter(Integer orderTypeConfigId, String processCode, String processCodeConfigCode, String bizOrgCode) {
          return orderProcessConfigItemMapper.findConfigItemListByParameter(orderTypeConfigId, processCode, processCodeConfigCode, bizOrgCode);
     }
}
