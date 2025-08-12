package com.edc.erp.directly.dirfirstorder.service.impl;

import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDelivery;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDeliveryService;
import com.edc.erp.directly.dirfirstorder.mapper.OrdDirOrderFirstDeliveryMapper;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;





/**
 * 配货单与铺货单关联表(OrdDirOrderFirstDelivery)表服务实现类
 *
 * @author weichao
 * @since 2022-11-10 14:08:53
 */
@Service
@RequiredArgsConstructor
public class OrdDirOrderFirstDeliveryServiceImpl extends BaseServiceImpl<OrdDirOrderFirstDelivery> implements OrdDirOrderFirstDeliveryService {
     
     private final OrdDirOrderFirstDeliveryMapper ordDirOrderFirstDeliveryMapper;

     /**
      * 批量保存配货单与铺货单关联表
      *
      * @param deliveryOrders
      * @param firstOrderId
      * @param userName
      */
     @Override
     @Transactional(rollbackFor = Exception.class)
     public void batchSave(List<Long> deliveryOrders, Long firstOrderId, String userName) {
          if (CollectionUtils.isEmpty(deliveryOrders)) {
               return;
          };
          ordDirOrderFirstDeliveryMapper.batchSave(deliveryOrders, firstOrderId, userName);
     }
}
