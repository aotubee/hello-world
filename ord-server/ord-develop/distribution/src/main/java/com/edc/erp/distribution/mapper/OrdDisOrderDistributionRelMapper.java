package com.edc.erp.distribution.mapper;

import com.edc.erp.distribution.entity.OrdDisOrderDistributionRel;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yaojinpeng
 * @since 2022/10/30 23:42
 */
@Repository
public interface OrdDisOrderDistributionRelMapper extends BaseMapper<OrdDisOrderDistributionRel> {
    /**
     * 批量保存配销分货单与配销订货单关联表
     * @param distributionJoinOrderList
     */
    void batchSaveDistributionJoinOrder(@Param("disJoinOrder") List<OrdDisOrderDistributionRel> distributionJoinOrderList);
}
