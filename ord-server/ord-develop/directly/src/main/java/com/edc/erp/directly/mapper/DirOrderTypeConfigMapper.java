package com.edc.erp.directly.mapper;

import com.edc.erp.directly.entity.DirGoodsCombination;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 订单类型设置表(OrderTypeConfig)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-18 16:54:38
 */
@Repository
public interface DirOrderTypeConfigMapper extends BaseMapper<DirOrderTypeConfig> {

    List<Long> findOrderTypeIdByGoodsCombination(DirGoodsCombination goodsCombination);
}
