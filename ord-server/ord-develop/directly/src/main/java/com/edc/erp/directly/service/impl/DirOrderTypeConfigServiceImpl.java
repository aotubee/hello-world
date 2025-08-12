package com.edc.erp.directly.service.impl;


import com.edc.erp.directly.entity.DirGoodsCombination;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.mapper.DirOrderTypeConfigMapper;
import com.edc.erp.directly.service.DirOrderTypeConfigService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


/**
 * 订单类型设置表(OrderTypeConfig)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:54:38
 */
@Service
@Slf4j
public class DirOrderTypeConfigServiceImpl extends BaseServiceImpl<DirOrderTypeConfig> implements DirOrderTypeConfigService {

     @Autowired
     private DirOrderTypeConfigMapper dirOrderTypeConfigMapper;

     /**
      * 根据id和业务组织代码查询订单类型配置信息
      *
      * @param orderTypeConfigId
      * @param bizOrgCode
      * @return
      */
     @Override
     public DirOrderTypeConfig getOrderTypeConfigByIdAndBizOrgCode(Integer orderTypeConfigId, String bizOrgCode) {
          DirOrderTypeConfig dirOrderTypeConfig = dirOrderTypeConfigMapper.selectByPrimaryKey(orderTypeConfigId);
          if(Objects.isNull(dirOrderTypeConfig)){
             log.error("订单类型设置id{}查询为空",orderTypeConfigId);
              return null;
          }
          return dirOrderTypeConfig;
     }

     @Override
     public List<Long> findOrderTypeIdByParameter(String distributionType, String type, String bizOrgCode) {
          DirGoodsCombination goodsCombination = new DirGoodsCombination();
          goodsCombination.setCombinationTypeCode(distributionType);
          goodsCombination.setCombinationValue(type);
          goodsCombination.setBizOrgCode(bizOrgCode);
          goodsCombination.setIsDelete(ModelConst.DELETE.NO);
          return dirOrderTypeConfigMapper.findOrderTypeIdByGoodsCombination(goodsCombination);
     }
}
