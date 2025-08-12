package com.edc.erp.directly.service.impl;

import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.mapper.DirOrderProcessConfigItemMapper;
import com.edc.erp.directly.service.DirOrderProcessConfigItemService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 订单类型流程选项配置(OrderProcessConfigItem)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:50:59
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DirOrderProcessConfigItemServiceImpl extends BaseServiceImpl<DirOrderProcessConfigItem> implements DirOrderProcessConfigItemService {


     private final DirOrderProcessConfigItemMapper dirOrderProcessConfigItemMapper;

     /**
      * 根据条件查询流程配置明细列表
      *
      * @param orderTypeConfigId
      * @param processCode
      * @param processCodeConfigCode
      * @param bizOrgCode
      * @return
      */
     @Override
     public List<DirOrderProcessConfigItem> findConfigItemListByParameter(Integer orderTypeConfigId, String processCode, String processCodeConfigCode, String bizOrgCode) {
          List<DirOrderProcessConfigItem> configItemList = dirOrderProcessConfigItemMapper.findTheOrderProcessConfigOut(orderTypeConfigId.longValue(),
                  processCode, processCodeConfigCode, bizOrgCode);
          return configItemList;
     }
}
