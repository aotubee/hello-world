package com.edc.erp.directly.distribution.service;

import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.model.in.AppQueryDirOrderIn;
import com.edc.erp.directly.distribution.model.out.DirOrderCycleOrderOut;
import com.edc.plugins.mybatis.service.BaseService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订货周期(OrdDirOrderCycle)表服务接口
 *
 * @author wanglidong
 * @since 2022-11-16 14:27:55
 */
public interface OrdDirOrderCycleService extends BaseService<OrdDirOrderCycle> {

    /**
     * 根据id和业务组织编码获取直营订货周期信息
     *
     * @param orderCycleId
     * @param bizOrgCode
     * @return
     */
    OrdDirOrderCycle getOrderCycleByIdAndBizOrgCode(Integer orderCycleId, String bizOrgCode);

    /**
     * 根据条件查询订货周期
     *
     * @param storeCode
     * @param orderTypeConfigId
     * @param truncationTime
     * @param bizOrgCode
     * @return
     */
    OrdDirOrderCycle getOrderCycle(String storeCode, Integer orderTypeConfigId, LocalDateTime truncationTime, String bizOrgCode);

    /**
     * 创建订货周期
     *
     * @param orderCycle
     */
    OrdDirOrderCycle createOrderCycle(OrdDirOrderCycle orderCycle);

    List<OrdDirOrderCycle> findOrderCycleListBetweenCreateTime(String beginTime, String endTime, String bizOrgCode, List<String> orderStatusCodeList);

    List<DirOrderCycleOrderOut> findOrderCycleOrderListByAppQueryOrderIn(AppQueryDirOrderIn appQueryOrderIn);

    /**
     * 根据订单ID找订货周期
     *
     * @param ids
     * @return
     */
    List<OrdDirOrderCycle> findByOrderIds(String ids);
}
