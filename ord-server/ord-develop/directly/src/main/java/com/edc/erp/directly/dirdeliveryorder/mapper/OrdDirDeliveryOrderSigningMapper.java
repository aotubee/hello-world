package com.edc.erp.directly.dirdeliveryorder.mapper;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderSigning;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


/**
 * 配货单签收(OrdDirDeliveryOrderSigning)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
@Repository
public interface OrdDirDeliveryOrderSigningMapper extends BaseMapper<OrdDirDeliveryOrderSigning> {


    OrdDirDeliveryOrderSigning getDeliveryOrderSigningServiceByDirDeliveryOrderId(@Param("dirDeliveryOrderId") Long dirDeliveryOrderId, @Param("bizOrgCode") String bizOrgCode);
}
