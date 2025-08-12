package com.edc.erp.disdeliveryorder.mapper;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderAttachment;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销单签收附件表(OrdDisDeliveryOrderAttachment)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Repository
public interface OrdDisDeliveryOrderAttachmentMapper extends BaseMapper<OrdDisDeliveryOrderAttachment> {

    /**
     * 批量添加配销单签收附件信息
     *
     * @param deliveryOrderAttachmentList
     */
    void batchSaveDeliveryOrderAttachment(@Param("deliveryOrderAttachmentList") List<OrdDisDeliveryOrderAttachment> deliveryOrderAttachmentList);
}
