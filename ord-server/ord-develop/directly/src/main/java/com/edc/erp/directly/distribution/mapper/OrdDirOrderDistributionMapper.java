package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.common.model.out.ucmanager.UserNameOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.model.in.DirJoinOrderIn;
import com.edc.erp.directly.distribution.model.in.OrdDirOrderDistributionIn;
import com.edc.erp.directly.distribution.model.out.DirJoinOrderOut;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderDistributionOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 直营分货单(OrdDisOrderDistribution)表数据库访问层
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:22
 */
@Repository
public interface OrdDirOrderDistributionMapper extends BaseMapper<OrdDirOrderDistribution> {
    /**
     * 查看直营分货单
     * @param ordDirOrderDistributionIn
     * @return
     */
    List<OrdDirOrderDistributionOrderOut> findOrdDirOrderDistributionByPage(OrdDirOrderDistributionIn ordDirOrderDistributionIn);

    /**
     * 查询分货订单列表
     * @param dirJoinOrderIn 作废分货单关联的订货单列表使用的入参类
     * @return
     */
    List<DirJoinOrderOut> findDirJoinOrderListByPage(DirJoinOrderIn dirJoinOrderIn);

    /**
     * 根据时间和状态查询分货单
     * @param status 审核状态
     * @param nowTime   当前时间
     * @return
     */
    List<OrdDirOrderDistributionOrderOut> findOrderByNewTimeAndStatus(@Param("status") String status, @Param("nowTime") String nowTime);

    /**
     * 查找直营分货单操作人集合
     * @param bizOrgCode
     * @return
     */
    List<UserNameOut> findDirOrderCreatorList(String bizOrgCode);

    String getDistributionOrderNoByOrderId(Long orderId, String bizOrgCode);

    List<Long> findNeedExecuteList(@Param("status") String status, @Param("nowTime") String nowTime);


}
