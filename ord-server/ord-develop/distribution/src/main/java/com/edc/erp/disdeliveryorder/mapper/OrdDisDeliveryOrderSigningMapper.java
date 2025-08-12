package com.edc.erp.disdeliveryorder.mapper;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderSigning;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.springframework.stereotype.Repository;



/**
 * 配销单签收(OrdDisDeliveryOrderSigning)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Repository
public interface OrdDisDeliveryOrderSigningMapper extends BaseMapper<OrdDisDeliveryOrderSigning> {
    
}
