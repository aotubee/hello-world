package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderAttachment;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;



/**
 * 配货单签收附件表(OrdDirDeliveryOrderAttachment)表服务接口
 *
 * @author weichao
 * @since 2022-11-22 10:46:50
 */
public interface OrdDirDeliveryOrderAttachmentService extends BaseService<OrdDirDeliveryOrderAttachment> {
    /**
     * 查询配货单签收附件列表
     * @param dirDeliveryOrderId
     * @param bizOrgCode
     * @return
     */
    List<OrdDirDeliveryOrderAttachment> findAllByDeliveryOrderIdAndBizOrgCode(Long dirDeliveryOrderId, String bizOrgCode);

    /**
     * 批量添加配货单签收附件信息
     *
     * @param deliveryOrderAttachmentList
     */
    void batchSaveDeliveryOrderAttachment(List<OrdDirDeliveryOrderAttachment> deliveryOrderAttachmentList);
}
