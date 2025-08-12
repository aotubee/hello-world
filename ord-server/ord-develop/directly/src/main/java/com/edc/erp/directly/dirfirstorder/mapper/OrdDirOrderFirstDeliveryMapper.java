package com.edc.erp.directly.dirfirstorder.mapper;

import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDelivery;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配货单与铺货单关联表(OrdDirOrderFirstDelivery)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-10 14:09:05
 */
@Repository
public interface OrdDirOrderFirstDeliveryMapper extends BaseMapper<OrdDirOrderFirstDelivery> {
    /**
     * 批量保存配货单与铺货单关联表
     * @param deliveryOrderIds
     * @param firstOrderId
     * @param userName
     */
    void batchSave(@Param("deliveryOrderIds") List<Long> deliveryOrderIds,@Param("firstOrderId") Long firstOrderId,@Param("userName")  String userName);
}
