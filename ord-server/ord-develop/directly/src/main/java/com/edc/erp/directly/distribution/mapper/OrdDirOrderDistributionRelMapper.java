package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionRel;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分货单与订货单关联表(OrdDirOrderDistributionRel)表数据库访问层
 *
 * @author fxw
 * @since 2022-11-18 18:13:57
 */
@Repository
public interface OrdDirOrderDistributionRelMapper extends BaseMapper<OrdDirOrderDistributionRel> {

    /**
     * 批量保存分货单与订货单关联表
     * @param dirtributionJoinOrderList
     */
    void batchSaveDistributionJoinOrder(@Param("disJoinOrder") List<OrdDirOrderDistributionRel> dirtributionJoinOrderList);
}
