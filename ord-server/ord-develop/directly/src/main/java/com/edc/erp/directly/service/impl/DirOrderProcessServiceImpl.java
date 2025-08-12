package com.edc.erp.directly.service.impl;

import com.edc.erp.directly.entity.DirOrderProcess;
import com.edc.erp.directly.entity.DirOrderProcessConfig;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.mapper.DirOrderProcessConfigItemMapper;
import com.edc.erp.directly.mapper.DirOrderProcessConfigMapper;
import com.edc.erp.directly.mapper.DirOrderProcessMapper;
import com.edc.erp.directly.model.out.DirOrderProcessConfigOut;
import com.edc.erp.directly.model.out.DirOrderProcessOut;
import com.edc.erp.directly.service.DirOrderProcessService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 订单类型流程(OrderProcess)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:47:16
 */
@Service
@RequiredArgsConstructor
public class DirOrderProcessServiceImpl extends BaseServiceImpl<DirOrderProcess> implements DirOrderProcessService {

     private final DirOrderProcessMapper dirOrderProcessMapper;

     private final DirOrderProcessConfigMapper dirOrderProcessConfigMapper;

     private final DirOrderProcessConfigItemMapper dirOrderProcessConfigItemMapper;

     /**
      * 根据订单流程配置id和业务组织编码查询订单流程
      *
      * @param dirOrderTypeConfig
      * @param bizOrgCode
      * @return
      */
     @Override
     public List<DirOrderProcessOut> findProcessListByOrderTypeConfigId(DirOrderTypeConfig dirOrderTypeConfig, String bizOrgCode) {
          List<DirOrderProcessOut> orderProcessList = this.findProcessOutListByTypeConfigIdAndOrg(dirOrderTypeConfig.getId().longValue(), bizOrgCode);
          if (CollectionUtils.isEmpty(orderProcessList)) {
               throw new BusinessException("订单类型" + dirOrderTypeConfig.getOrderTypeName() + "未设置流程");
          }
          return orderProcessList;
     }


     /**
      * 根据订单id查询订单类型流程所有信息
      * @param orderTypeConfigId  订单类型主键
      * @param bizOrgCode    业务组织
      * @return
      */
     @Override
     public List<DirOrderProcessOut> findProcessOutListByTypeConfigIdAndOrg(Long orderTypeConfigId, String bizOrgCode) {
          DirOrderProcess query = new DirOrderProcess();
          query.setOrderTypeConfigId(orderTypeConfigId);
          query.setBizOrgCode(bizOrgCode);
          query.setIsDelete(ModelConst.DELETE.NO);
          List<DirOrderProcess> processList = dirOrderProcessMapper.select(query);
          if (CollectionUtils.isEmpty(processList)) {
               return null;
          }
          return processList.stream().map(process -> {
               DirOrderProcessOut processOut = new DirOrderProcessOut();
               BeanUtils.copy(process, processOut);
               DirOrderProcessConfig optOrderProcessConfig = new DirOrderProcessConfig();
               optOrderProcessConfig.setOrderProcessId(process.getId());
               optOrderProcessConfig.setIsDelete(ModelConst.DELETE.NO);
               List<DirOrderProcessConfig> processConfigList = dirOrderProcessConfigMapper.select(optOrderProcessConfig);
               if (CollectionUtils.isEmpty(processConfigList)) {
                    throw new BusinessException("流程" + process.getProcessName() + "未设置配置");
               }
               List<DirOrderProcessConfigOut> configOutList = processConfigList.stream().map(config -> {
                    DirOrderProcessConfigOut configOut = new DirOrderProcessConfigOut();
                    BeanUtils.copy(config, configOut);
                    DirOrderProcessConfigItem optDirOrderProcessConfigItem = new DirOrderProcessConfigItem();
                    optDirOrderProcessConfigItem.setOrderProcessConfigId(config.getId());
                    optDirOrderProcessConfigItem.setIsDelete(ModelConst.DELETE.NO);
                    List<DirOrderProcessConfigItem> configItemList = dirOrderProcessConfigItemMapper.select(optDirOrderProcessConfigItem);
                    if (CollectionUtils.isEmpty(configItemList)) {
                         throw new BusinessException("配置" + config.getProcessConfigName() + "未设置选项");
                    }
                    configOut.setConfigItemList(configItemList);
                    return configOut;
               }).collect(Collectors.toList());
               processOut.setConfigList(configOutList);
               return processOut;
          }).collect(Collectors.toList());
     }

}
