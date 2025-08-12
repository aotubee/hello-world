package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.OrderProcess;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 订单类型流程(OrderProcess)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-18 16:47:16
 */
@Repository
public interface OrderProcessMapper extends BaseMapper<OrderProcess> {

    /**
     * 根据订单流程配置id和业务组织编码查询订单流程
     *
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    List<OrderProcess> findProcessListByOrderTypeConfigId(@Param("orderTypeConfigId") Integer orderTypeConfigId, @Param("bizOrgCode") String bizOrgCode);
}
