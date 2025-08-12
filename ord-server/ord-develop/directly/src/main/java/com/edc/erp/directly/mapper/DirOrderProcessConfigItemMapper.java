package com.edc.erp.directly.mapper;

import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
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
public interface DirOrderProcessConfigItemMapper extends BaseMapper<DirOrderProcessConfigItem> {

    /**
     * 根据订单类型配置id、业务组织代码查询订单流程配置明细列表
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    List<DirOrderProcessConfigItem> findItemListByOrderProcessConfigId(@Param("id") Long id, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据条件查询流程配置明细列表
     *
     * @param orderTypeConfigId
     * @param processCode
     * @param processCodeConfigCode
     * @param bizOrgCode
     * @return
     */
    List<DirOrderProcessConfigItem> findConfigItemListByParameter(@Param("orderTypeConfigId") Integer orderTypeConfigId,
                                                               @Param("processCode") String processCode,
                                                               @Param("processCodeConfigCode") String processCodeConfigCode,
                                                               @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询配置项
     * @param orderTypeConfigId
     * @param processCode
     * @param processConfigCode
     * @param bizOrgCode
     * @return
     */
    List<DirOrderProcessConfigItem> findTheOrderProcessConfigOut(@Param("orderTypeConfigId") Long orderTypeConfigId, @Param("processCode") String processCode,
                                                                 @Param("processConfigCode") String processConfigCode, @Param("bizOrgCode") String bizOrgCode);
}
