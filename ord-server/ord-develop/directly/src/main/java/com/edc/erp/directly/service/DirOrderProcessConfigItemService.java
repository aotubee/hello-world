package com.edc.erp.directly.service;

import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 订单类型流程选项配置(OrderProcessConfigItem)表服务接口
 *
 * @author fxw
 * @since 2022-10-18 16:50:59
 */
public interface DirOrderProcessConfigItemService extends BaseService<DirOrderProcessConfigItem> {

//    /**
//     * 根据订单类型配置id、业务组织代码查询订单流程配置明细列表
//     *
//     * @param id
//     * @param bizOrgCode
//     * @return
//     */
//    List<DirOrderProcessConfigItemOut> findItemListByOrderProcessConfigId(Long id, String bizOrgCode);

    /**
     * 根据条件查询流程配置明细列表
     *
     * @param orderTypeConfigId
     * @param processCode
     * @param processCodeConfigCode
     * @param bizOrgCode
     * @return
     */
    List<DirOrderProcessConfigItem> findConfigItemListByParameter(Integer orderTypeConfigId, String processCode, String processCodeConfigCode, String bizOrgCode);
}
