package com.edc.erp.distribution.mapper.mapper;

import com.edc.erp.distribution.entity.OrdDisOrderDistributionRel;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.springframework.stereotype.Repository;



/**
 * 配销分货单与配销订货单关联表(DisOrderDistributionRel)表数据库访问层
 *
 * @author yaojinpeng
 * @since 2022-10-30 23:50:31
 */
@Repository
public interface DisOrderDistributionRelMapper extends BaseMapper<OrdDisOrderDistributionRel> {

}
