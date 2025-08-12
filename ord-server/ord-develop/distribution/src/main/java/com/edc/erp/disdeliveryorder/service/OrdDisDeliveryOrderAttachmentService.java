package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderAttachment;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 配销单签收附件表(OrdDisDeliveryOrderAttachment)表服务接口
 *
 * @author fxw
 * @since 2022-10-26 18:06:46
 */
public interface OrdDisDeliveryOrderAttachmentService extends BaseService<OrdDisDeliveryOrderAttachment> {

    /**
     * 查询配销单签收附件列表
     *
     * @param disDeliveryOrderId
     * @param bizOrgCode
     * @return
     */
    List<OrdDisDeliveryOrderAttachment> findAllByDeliveryOrderIdAndBizOrgCode(Long disDeliveryOrderId, String bizOrgCode);

    /**
     * 批量添加配销单签收附件信息
     *
     * @param deliveryOrderAttachmentList
     */
    void batchSaveDeliveryOrderAttachment(List<OrdDisDeliveryOrderAttachment> deliveryOrderAttachmentList);
}
