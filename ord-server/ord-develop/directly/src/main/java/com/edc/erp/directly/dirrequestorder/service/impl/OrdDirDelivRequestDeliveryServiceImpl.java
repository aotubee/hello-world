package com.edc.erp.directly.dirrequestorder.service.impl;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDelivery;
import com.edc.erp.directly.dirrequestorder.mapper.OrdDirDelivRequestDeliveryMapper;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestDeliveryService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;





/**
 * 要货单与配货单关联表(OrdDirDelivRequestDelivery)表服务实现类
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Service
@RequiredArgsConstructor
public class OrdDirDelivRequestDeliveryServiceImpl extends BaseServiceImpl<OrdDirDelivRequestDelivery> implements OrdDirDelivRequestDeliveryService {
     
     private final OrdDirDelivRequestDeliveryMapper ordDirDelivRequestDeliveryMapper;

     /**
      * 批量新增
      *
      * @param deliveryOrderList
      * @param requestOrderId
      */
     @Override
     @Transactional(rollbackFor = Exception.class)
     public void batchSave(List<OrdDirDelivery> deliveryOrderList, Long requestOrderId) {
          List<OrdDirDelivRequestDelivery> requestOrderDeliveryOrderList = Lists.newArrayList();
          deliveryOrderList.stream().forEach(deliveryOrder -> {
               OrdDirDelivRequestDelivery requestOrderDeliveryOrder = new OrdDirDelivRequestDelivery();
               requestOrderDeliveryOrder.setDeliveryOrderId(deliveryOrder.getId());
               requestOrderDeliveryOrder.setRequestOrderId(requestOrderId);
               requestOrderDeliveryOrder.setCreator(deliveryOrder.getCreator());
               requestOrderDeliveryOrder.setCreateTime(LocalDateTime.now());
               requestOrderDeliveryOrderList.add(requestOrderDeliveryOrder);
          });
          ordDirDelivRequestDeliveryMapper.batchSaveDirDeliveryRequestOrderList(requestOrderDeliveryOrderList);
     }
}
