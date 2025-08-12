package com.edc.erp.common.service.impl;


import com.edc.erp.common.entity.GoodsCombination;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.mapper.OrderTypeConfigMapper;
import com.edc.erp.common.service.OrderTypeConfigService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 订单类型设置表(OrderTypeConfig)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:54:38
 */
@Service
@Slf4j
public class OrderTypeConfigServiceImpl extends BaseServiceImpl<OrderTypeConfig> implements OrderTypeConfigService {

     @Autowired
     private OrderTypeConfigMapper orderTypeConfigMapper;

     /**
      * 根据id和业务组织代码查询订单类型配置信息
      *
      * @param orderTypeConfigId
      * @param bizOrgCode
      * @return
      */
     @Override
     public OrderTypeConfig getOrderTypeConfigByIdAndBizOrgCode(Integer orderTypeConfigId, String bizOrgCode) {
          OrderTypeConfig orderTypeConfig = new OrderTypeConfig();
          orderTypeConfig.setId(orderTypeConfigId);
          orderTypeConfig.setBizOrgCode(bizOrgCode);
          orderTypeConfig.setIsDelete(ModelConst.DELETE.NO);
          return orderTypeConfigMapper.selectOne(orderTypeConfig);
     }

     @Override
     public List<Long> findOrderTypeIdByParameter(String combinationTypeCode, String combinationValue, String bizOrgCode) {
          GoodsCombination goodsCombination = new GoodsCombination();
          goodsCombination.setCombinationTypeCode(combinationTypeCode);
          goodsCombination.setCombinationValue(combinationValue);
          goodsCombination.setBizOrgCode(bizOrgCode);
          goodsCombination.setIsDelete(ModelConst.DELETE.NO);
          return orderTypeConfigMapper.findOrderTypeIdByGoodsCombination(goodsCombination);
     }
}
