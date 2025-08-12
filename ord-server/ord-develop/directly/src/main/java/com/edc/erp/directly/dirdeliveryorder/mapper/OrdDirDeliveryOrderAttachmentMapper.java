package com.edc.erp.directly.dirdeliveryorder.mapper;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderAttachment;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配货单签收附件表(OrdDirDeliveryOrderAttachment)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-22 10:46:50
 */
@Repository
public interface OrdDirDeliveryOrderAttachmentMapper extends BaseMapper<OrdDirDeliveryOrderAttachment> {

    /**
     * 批量添加配货单签收附件信息
     *
     * @param deliveryOrderAttachmentList
     */
    void batchSaveDeliveryOrderAttachment(@Param("deliveryOrderAttachmentList") List<OrdDirDeliveryOrderAttachment> deliveryOrderAttachmentList);
}
