package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.GoodsCombination;
import com.edc.erp.common.entity.OrderTypeConfig;
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
public interface OrderTypeConfigMapper extends BaseMapper<OrderTypeConfig> {

    List<Long> findOrderTypeIdByGoodsCombination(GoodsCombination goodsCombination);
}
