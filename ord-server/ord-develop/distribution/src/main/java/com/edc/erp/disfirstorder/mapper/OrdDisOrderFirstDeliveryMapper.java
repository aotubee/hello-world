package com.edc.erp.disfirstorder.mapper;

import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDelivery;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销配货单与配销铺货单关联表(OrdDisOrderFirstDelivery)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-10 16:17:57
 */
@Repository
public interface OrdDisOrderFirstDeliveryMapper extends BaseMapper<OrdDisOrderFirstDelivery> {
    /**
     * 批量保存配销配货单与配销铺货单关联表
     * @param deliveryOrderIds
     * @param firstOrderId
     * @param userName
     */
    void batchSave(@Param("deliveryOrderIds") List<Long> deliveryOrderIds, @Param("firstOrderId") Long firstOrderId,@Param("userName") String userName);

    Long getFirstOrderIdByDeliveryOrderId(@Param("deliveryOrderId") Long deliveryOrderId);
}
