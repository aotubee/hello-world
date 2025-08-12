package com.edc.erp.directly.service;

import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 订单类型设置表(OrderTypeConfig)表服务接口
 *
 * @author fxw
 * @since 2022-10-18 16:54:38
 */
public interface DirOrderTypeConfigService extends BaseService<DirOrderTypeConfig> {

    /**
     * 根据id和业务组织代码查询订单类型配置信息
     *
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    DirOrderTypeConfig getOrderTypeConfigByIdAndBizOrgCode(Integer orderTypeConfigId, String bizOrgCode);

    /**
     * 查询中转订单流配置
     * @param distributionType
     * @param type
     * @param bizOrgCode
     * @return
     */
    List<Long> findOrderTypeIdByParameter(String distributionType, String type, String bizOrgCode);
}
