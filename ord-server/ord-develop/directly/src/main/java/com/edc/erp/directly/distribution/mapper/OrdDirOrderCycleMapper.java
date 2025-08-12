package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.model.in.AppQueryDirOrderIn;
import com.edc.erp.directly.distribution.model.out.DirOrderCycleOrderOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import io.swagger.annotations.ApiModelProperty;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订货周期(OrdDirOrderCycle)表数据库访问层
 *
 * @author wanglidong
 * @since 2022-11-16 14:27:55
 */
@Repository
public interface OrdDirOrderCycleMapper extends BaseMapper<OrdDirOrderCycle> {

    List<OrdDirOrderCycle> findOrderCycleListBetweenCreateTime(@Param("beginTime") String beginTime,
                                                               @Param("endTime") String endTime,
                                                               @Param("bizOrgCode") String bizOrgCode,
                                                               @Param("orderStatusCodeList") List<String> orderStatusCodeList);

    List<DirOrderCycleOrderOut> findOrderCycleOrderListByAppQueryOrderIn(AppQueryDirOrderIn appQueryOrderIn);

    List<OrdDirOrderCycle> findByOrderIds(@Param("ids") String ids);
}
