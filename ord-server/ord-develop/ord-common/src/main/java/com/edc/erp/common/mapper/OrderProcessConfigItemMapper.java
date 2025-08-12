package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.OrderProcessConfigItem;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 订单类型流程选项配置(OrderProcessConfigItem)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-18 16:50:59
 */
@Repository
public interface OrderProcessConfigItemMapper extends BaseMapper<OrderProcessConfigItem> {

    /**
     * 根据订单类型配置id、业务组织代码查询订单流程配置明细列表
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    List<OrderProcessConfigItem> findItemListByOrderProcessConfigId(@Param("id") Long id, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据条件查询流程配置明细列表
     *
     * @param orderTypeConfigId
     * @param processCode
     * @param processCodeConfigCode
     * @param bizOrgCode
     * @return
     */
    List<OrderProcessConfigItem> findConfigItemListByParameter(@Param("orderTypeConfigId") Integer orderTypeConfigId,
                                                               @Param("processCode") String processCode,
                                                               @Param("processCodeConfigCode") String processCodeConfigCode,
                                                               @Param("bizOrgCode") String bizOrgCode);
}
