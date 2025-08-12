package com.edc.erp.distribution.mapper;

import com.edc.erp.common.model.out.ucmanager.UserNameOut;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.model.in.DisJoinOrderIn;
import com.edc.erp.distribution.model.in.OrdDisOrderDistributionIn;
import com.edc.erp.distribution.model.out.DisJoinOrderOut;
import com.edc.erp.distribution.model.out.OrdDisOrderDistributionOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销分货单(OrdDisOrderDistribution)表数据库访问层
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:22
 */
@Repository
public interface OrdDisOrderDistributionMapper extends BaseMapper<OrdDisOrderDistribution> {
    /**
     * 查看配销分货单
     * @param ordDisOrderDistributionIn
     * @return
     */
    List<OrdDisOrderDistributionOrderOut> findOrdDisOrderDistributionByPage(OrdDisOrderDistributionIn ordDisOrderDistributionIn);

    /**
     * 查询分货订单列表
     * @param disJoinOrderIn 作废分货单关联的订货单列表使用的入参类
     * @return
     */
    List<DisJoinOrderOut> findDisJoinOrderListByPage(DisJoinOrderIn disJoinOrderIn);

    /**
     * 根据时间和状态查询分货单
     * @param status 审核状态
     * @param nowTime   当前时间
     * @return
     */
    List<OrdDisOrderDistributionOrderOut> findOrderByNewTimeAndStatus(@Param("status") String status, @Param("nowTime") String nowTime);

    /**
     * 查找配销分货单操作人集合
     * @param bizOrgCode 业务组织
     * @return
     */
    List<UserNameOut> findDistributionOrderCreatorList(String bizOrgCode);

    List<Long> findNeedExecuteList(@Param("status") String status, @Param("nowTime") String nowTime);

    String getDistributionOrderNoByOrderId(Long orderId, String bizOrgCode);
}
