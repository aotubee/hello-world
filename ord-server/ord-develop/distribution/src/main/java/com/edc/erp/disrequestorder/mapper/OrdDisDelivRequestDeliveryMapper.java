package com.edc.erp.disrequestorder.mapper;

import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDelivery;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;


/**
 * 集货单与配销单关联表(OrdDisDelivRequestDelivery)表数据库访问层
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Repository
public interface OrdDisDelivRequestDeliveryMapper extends BaseMapper<OrdDisDelivRequestDelivery> {

}
