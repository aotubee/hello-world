package com.edc.erp.directly.dirdeliveryorder.service.impl;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderAttachment;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryOrderAttachmentMapper;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderAttachmentService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;





/**
 * 配货单签收附件表(OrdDirDeliveryOrderAttachment)表服务实现类
 *
 * @author weichao
 * @since 2022-11-22 10:46:50
 */
@Service
@RequiredArgsConstructor
public class OrdDirDeliveryOrderAttachmentServiceImpl extends BaseServiceImpl<OrdDirDeliveryOrderAttachment> implements OrdDirDeliveryOrderAttachmentService {
     
     private final OrdDirDeliveryOrderAttachmentMapper ordDirDeliveryOrderAttachmentMapper;

     @Override
     public List<OrdDirDeliveryOrderAttachment> findAllByDeliveryOrderIdAndBizOrgCode(Long dirDeliveryOrderId, String bizOrgCode) {
          OrdDirDeliveryOrderAttachment ordDirDeliveryOrderAttachment = new OrdDirDeliveryOrderAttachment();
          ordDirDeliveryOrderAttachment.setBizOrgCode(bizOrgCode);
          ordDirDeliveryOrderAttachment.setDirDeliveryOrderId(dirDeliveryOrderId);
          return ordDirDeliveryOrderAttachmentMapper.select(ordDirDeliveryOrderAttachment);
     }

     /**
      * 批量添加配货单签收附件信息
      *
      * @param deliveryOrderAttachmentList
      */
     @Override
     @Transactional(rollbackFor = Exception.class)
     public void batchSaveDeliveryOrderAttachment(List<OrdDirDeliveryOrderAttachment> deliveryOrderAttachmentList) {
          ordDirDeliveryOrderAttachmentMapper.batchSaveDeliveryOrderAttachment(deliveryOrderAttachmentList);
     }
}
