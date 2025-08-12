package com.edc.erp.disdeliveryorder.service.impl;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderAttachment;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryOrderAttachmentMapper;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderAttachmentService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;





/**
 * 配销单签收附件表(OrdDisDeliveryOrderAttachment)表服务实现类
 *
 * @author fxw
 * @since 2022-10-26 18:06:46
 */
@Service
@RequiredArgsConstructor
public class OrdDisDeliveryOrderAttachmentServiceImpl extends BaseServiceImpl<OrdDisDeliveryOrderAttachment> implements OrdDisDeliveryOrderAttachmentService {
     
     private final OrdDisDeliveryOrderAttachmentMapper ordDisDeliveryOrderAttachmentMapper;

     /**
      * 查询配销单签收附件列表
      *
      * @param disDeliveryOrderId
      * @param bizOrgCode
      * @return
      */
     @Override
     public List<OrdDisDeliveryOrderAttachment> findAllByDeliveryOrderIdAndBizOrgCode(Long disDeliveryOrderId, String bizOrgCode) {
          OrdDisDeliveryOrderAttachment ordDisDeliveryOrderAttachment = new OrdDisDeliveryOrderAttachment();
          ordDisDeliveryOrderAttachment.setBizOrgCode(bizOrgCode);
          ordDisDeliveryOrderAttachment.setDisDeliveryOrderId(disDeliveryOrderId);
          return ordDisDeliveryOrderAttachmentMapper.select(ordDisDeliveryOrderAttachment);
     }

     /**
      * 批量添加配销单签收附件信息
      *
      * @param deliveryOrderAttachmentList
      */
     @Override
     @Transactional(rollbackFor = Exception.class)
     public void batchSaveDeliveryOrderAttachment(List<OrdDisDeliveryOrderAttachment> deliveryOrderAttachmentList) {
          ordDisDeliveryOrderAttachmentMapper.batchSaveDeliveryOrderAttachment(deliveryOrderAttachmentList);
     }
}
