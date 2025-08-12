package com.edc.erp.mapper;

import com.edc.erp.entity.DisActivity;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.springframework.stereotype.Repository;



/**
 * 配销活动表(DisActivity)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-19 18:43:02
 */
@Repository
public interface DisActivityMapper extends BaseMapper<DisActivity> {
    
}
