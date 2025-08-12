package com.edc.erp.directly.service;


import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.entity.DirOrderProcessCopy;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;
import java.util.Map;


/**
 * 订单类型流程副本(OrderProcessCopy)表服务接口
 *
 * @author fxw
 * @since 2022-10-18 16:35:14
 */
public interface DirOrderProcessCopyService extends BaseService<DirOrderProcessCopy> {

    /**
     * 按条件查找全流程副本
     *
     * @param orderingCycleId 订货周期id
     * @param storeCode       门店代码
     * @param bizOrgCode      业务组织代码
     * @return
     */
    List<DirOrderProcessCopy> findOrderProcessCopyListByParameter(Integer orderingCycleId, String storeCode, String bizOrgCode);

//    /**
//     * 保存流程副本
//     *
//     * @param orderProcessCopy
//     */
//    void save(DirOrderProcessCopy orderProcessCopy);

    /**
     * 查找指定订货周期内当前流程代码的下一个流程副本
     *
     * @param orderingCycleId 订货周期id
     * @param storeCode       门店代码
     * @param progressCode    当前流程代码
     * @param bizOrgCode      组织代码
     * @return
     */
    DirOrderProcessCopy getNextOrderProcessCopyByParameter(Integer orderingCycleId, String storeCode, String progressCode, String bizOrgCode);

    /**
     * 查找指定订货周期、订单流程、流程配置下的选项
     *
     * @param orderCycleId           订货周期id
     * @param bizOrgCode             业务组织代码
     * @param processCode            订单流程代码
     * @param orderProcessConfigCode 订单流程配置代码
     * @return
     */
    DirOrderProcessConfigItem getOrderProcessConfigItemOut(Integer orderCycleId, String bizOrgCode, String processCode, String orderProcessConfigCode);

    /**
     * 按条件查找一个流程副本
     *
     * @param dirOrderProcessCopy
     * @return
     */
    DirOrderProcessCopy getOrderProcessCopy(DirOrderProcessCopy dirOrderProcessCopy);

    /**
     * 查找指定订货周期、订单流程、流程配置下的选项集合
     *
     * @param id                     订货周期id
     * @param bizOrgCode             业务组织代码
     * @param processCode            订单流程代码
     * @param orderProcessConfigCode 订单流程配置代码
     * @return
     */
    List<DirOrderProcessConfigItem> findOrderProcessConfigItemOut(Integer id, String storeCode, String bizOrgCode, String processCode, String orderProcessConfigCode);

    /**
     * 判断是否有支付订单
     *
     * @param orderCycleId
     * @param storeCode
     * @param processCode
     * @param bizOrgCode
     * @return
     */
    DirOrderProcessCopy getOrderProcessCopyByParameter(Integer orderCycleId, String storeCode, String processCode, String bizOrgCode);

    Map<String, List<OrderProcessConfigItemOut>> findConfigItemMapByParam(Integer orderCycleId, String storeCode, String bizOrgCode, String processCodes, String processConfigCodes);
}
