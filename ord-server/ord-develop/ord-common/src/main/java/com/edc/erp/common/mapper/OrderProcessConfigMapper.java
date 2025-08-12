package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.OrderProcessConfig;import com.edc.erp.common.model.out.OrderProcessConfigOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 *  订单类型流程配置表(OrderProcessConfig)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-18 16:49:00
 */
@Repository
public interface OrderProcessConfigMapper extends BaseMapper<OrderProcessConfig> {

    /**
     * 根据订单流程id、业务组织代码查询订单流程配置列表
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    List<OrderProcessConfigOut> findConfigListByOrderProcessId(@Param("id") Long id, @Param("bizOrgCode") String bizOrgCode);
}
