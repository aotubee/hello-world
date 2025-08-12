package com.edc.erp.distribution.mapper;

import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.model.in.AppQueryDisOrderIn;
import com.edc.erp.distribution.model.out.DisOrderCycleOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销订货周期(OrdDisOrderCycle)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-17 16:49:33
 */
@Repository
public interface OrdDisOrderCycleMapper extends BaseMapper<OrdDisOrderCycle> {


    List<OrdDisOrderCycle> findOrderCycleListBetweenCreateTime(@Param("beginTime") String beginTime,
                                                               @Param("endTime") String endTime,
                                                               @Param("bizOrgCode") String bizOrgCode,
                                                               @Param("orderStatusCodeList") List<String> orderStatusCodeList);

    List<DisOrderCycleOrderOut> findOrderCycleOrderListByAppQueryOrderIn(AppQueryDisOrderIn appQueryOrderIn);


    List<OrdDisOrderCycle> findByOrderIds(@Param("ids") String ids);
}
