package com.edc.erp.distribution.service;

import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.model.in.AppQueryDisOrderIn;
import com.edc.erp.distribution.model.out.DisOrderCycleOrderOut;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 配销订货周期(OrdDisOrderCycle)表服务接口
 *
 * @author fxw
 * @since 2022-10-17 16:49:33
 */
public interface OrdDisOrderCycleService extends BaseService<OrdDisOrderCycle> {

    /**
     * 根据id和业务组织编码获取配销订货周期信息
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    OrdDisOrderCycle getOrderCycleByIdAndBizOrgCode(Integer id, String bizOrgCode);

    /**
     * 根据条件查询订货周期
     *
     * @param storeCode
     * @param orderTypeConfigId
     * @param truncationTime
     * @param bizOrgCode
     * @return
     */
    OrdDisOrderCycle getOrderCycle(String storeCode, Integer orderTypeConfigId, LocalDateTime truncationTime, String bizOrgCode);

    /**
     * 创建订货周期
     *
     * @param orderCycle
     */
    OrdDisOrderCycle createOrderCycle(OrdDisOrderCycle orderCycle);

    List<OrdDisOrderCycle> findOrderCycleListBetweenCreateTime(String beginTime, String endTime, String bizOrgCode, List<String> orderStatusCodeList);

    List<DisOrderCycleOrderOut> findOrderCycleOrderListByAppQueryOrderIn(AppQueryDisOrderIn appQueryOrderIn);

    @Transactional(rollbackFor = Exception.class)
    void updateFirstOrderTime(Integer id, LocalDateTime firstOrderTime, String loginUsername);

    List<OrdDisOrderCycle> findByOrderIds(String ids);
}
