package com.edc.erp.directly.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.model.out.OrderProcessConfigOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.entity.DirOrderProcessConfig;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.entity.DirOrderProcessCopy;
import com.edc.erp.directly.mapper.DirOrderProcessCopyMapper;
import com.edc.erp.directly.model.out.DirOrderProcessConfigOut;
import com.edc.erp.directly.service.DirOrderProcessCopyService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 订单类型流程副本(OrderProcessCopy)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 16:35:14
 */
@Service
@Slf4j
public class DirOrderProcessCopyServiceImpl extends BaseServiceImpl<DirOrderProcessCopy> implements DirOrderProcessCopyService {

     @Autowired
     private DirOrderProcessCopyMapper dirOrderProcessCopyMapper;

     @Autowired
     private DirOrderCycleHandle dirOrderCycleHandle;

     /**
      * 按条件查找全流程副本
      *
      * @param orderingCycleId 订货周期id
      * @param storeCode       门店代码
      * @param bizOrgCode         组织代码
      * @return
      */
     @Override
     public List<DirOrderProcessCopy> findOrderProcessCopyListByParameter(Integer orderingCycleId, String storeCode, String bizOrgCode) {
          DirOrderProcessCopy dirOrderProcessCopy = new DirOrderProcessCopy();
          dirOrderProcessCopy.setOrderCycleId(orderingCycleId);
          dirOrderProcessCopy.setStoreCode(storeCode);
          dirOrderProcessCopy.setBizOrgCode(bizOrgCode);
          return dirOrderProcessCopyMapper.select(dirOrderProcessCopy);
     }

//     /**
//      * 保存流程副本
//      *
//      * @param orderProcessCopy
//      */
//     @Override
//     @Transactional(rollbackFor = Exception.class)
//     public void save(DirOrderProcessCopy orderProcessCopy) {
//          dirOrderProcessCopyMapper.insertOrderProcessCopy(orderProcessCopy);
//     }

     /**
      * 查找指定订货周期内当前流程代码的下一个流程副本
      *
      * @param orderingCycleId 订货周期id
      * @param storeCode       门店代码
      * @param progressCode    当前流程代码
      * @param bizOrgCode         组织代码
      * @return
      */
     @Override
     public DirOrderProcessCopy getNextOrderProcessCopyByParameter(Integer orderingCycleId, String storeCode, String progressCode, String bizOrgCode) {
          log.info("组织{}下门店{}订货周期主键{}当前流程代码{}", bizOrgCode, storeCode, orderingCycleId, progressCode);
          DirOrderProcessCopy dirOrderProcessCopy = dirOrderProcessCopyMapper.getOneByParameter(orderingCycleId, storeCode, progressCode, bizOrgCode);
          if (Objects.isNull(dirOrderProcessCopy)) {
               return null;
          }
          return dirOrderProcessCopyMapper.getNextOneByParameter(orderingCycleId, storeCode, dirOrderProcessCopy.getId(), bizOrgCode);
     }

     /**
      * 查找指定订货周期、订单流程、流程配置下的选项
      *
      * @param orderCycleId           订货周期id
      * @param bizOrgCode              业务组织代码
      * @param processCode            订单流程代码
      * @param orderProcessConfigCode 订单流程配置代码
      * @return
      */
     @Override
     public DirOrderProcessConfigItem getOrderProcessConfigItemOut(Integer orderCycleId, String bizOrgCode, String processCode, String orderProcessConfigCode) {
          DirOrderProcessCopy dirOrderProcessCopy = new DirOrderProcessCopy();
          dirOrderProcessCopy.setProgressCode(processCode);
          dirOrderProcessCopy.setBizOrgCode(bizOrgCode);
          dirOrderProcessCopy.setOrderCycleId(orderCycleId);
          dirOrderProcessCopy = this.getOrderProcessCopy(dirOrderProcessCopy);
          if (Objects.isNull(dirOrderProcessCopy)) {
               OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, bizOrgCode);
               log.info("门店{}订货周期{}截单时间{}的副本不存在", orderCycle.getStoreCode(), orderCycle.getShortOrderType(), orderCycle.getTruncationDateTime());
               return null;
          }
          String processConfigItem = dirOrderProcessCopy.getProcessConfigItem();
          List<DirOrderProcessConfigOut> configList = JSONArray.parseArray(processConfigItem, DirOrderProcessConfigOut.class);
          Map<String, DirOrderProcessConfigOut> configOutMap = configList.stream().collect(Collectors.toMap(DirOrderProcessConfig::getProcessConfigCode, Function.identity()));
          DirOrderProcessConfigOut dirOrderProcessConfigOut = configOutMap.get(orderProcessConfigCode);
          if (Objects.isNull(dirOrderProcessConfigOut)) {
               return null;
          }
          if (CollectionUtils.isEmpty(dirOrderProcessConfigOut.getConfigItemList())) {
               return null;
          }
          return dirOrderProcessConfigOut.getConfigItemList().get(0);
     }

     /**
      * 按条件查找一个流程副本
      * @param dirOrderProcessCopy
      * @return
      */
     @Override
     public DirOrderProcessCopy getOrderProcessCopy(DirOrderProcessCopy dirOrderProcessCopy) {
          return dirOrderProcessCopyMapper.selectOne(dirOrderProcessCopy);
     }

     /**
      * 查找指定订货周期、订单流程、流程配置下的选项集合
      *
      * @param id 订货周期id
      * @param bizOrgCode 业务组织代码
      * @param processCode   订单流程代码
      * @param orderProcessConfigCode 订单流程配置代码
      * @return
      */
     @Override
     public List<DirOrderProcessConfigItem> findOrderProcessConfigItemOut(Integer id, String storeCode, String bizOrgCode, String processCode, String orderProcessConfigCode) {
          DirOrderProcessCopy dirOrderProcessCopy = new DirOrderProcessCopy();
          dirOrderProcessCopy.setProgressCode(processCode);
          dirOrderProcessCopy.setBizOrgCode(bizOrgCode);
          dirOrderProcessCopy.setOrderCycleId(id);
          dirOrderProcessCopy.setStoreCode(storeCode);
          dirOrderProcessCopy = this.getOrderProcessCopy(dirOrderProcessCopy);
          if (Objects.isNull(dirOrderProcessCopy)) {
               OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(id, bizOrgCode);
               log.info("门店{}订货周期{}截单时间{}的副本不存在", orderCycle.getStoreCode(), orderCycle.getShortOrderType(), orderCycle.getTruncationDateTime());
               return null;
          }
          String processConfigItem = dirOrderProcessCopy.getProcessConfigItem();
          List<DirOrderProcessConfigOut> configList = JSONArray.parseArray(processConfigItem, DirOrderProcessConfigOut.class);
          Map<String, DirOrderProcessConfigOut> configOutMap = configList.stream().collect(Collectors.toMap(DirOrderProcessConfig::getProcessConfigCode, Function.identity()));
          DirOrderProcessConfigOut dirOrderProcessConfigOut = configOutMap.get(orderProcessConfigCode);
          return dirOrderProcessConfigOut.getConfigItemList();
     }

     /**
      * 判断是否有支付订单
      *
      * @param orderCycleId
      * @param storeCode
      * @param processCode
      * @param bizOrgCode
      * @return
      */
     @Override
     public DirOrderProcessCopy getOrderProcessCopyByParameter(Integer orderCycleId, String storeCode, String processCode, String bizOrgCode) {
          return dirOrderProcessCopyMapper.getOneByParameter(orderCycleId, storeCode, processCode,bizOrgCode);
     }

     @Override
     public Map<String, List<OrderProcessConfigItemOut>> findConfigItemMapByParam(Integer orderCycleId, String storeCode, String bizOrgCode, String processCodes, String processConfigCodes) {
          Example example = new Example(DirOrderProcessCopy.class);
          Example.Criteria criteria = example.createCriteria();
          criteria.andEqualTo("orderCycleId", orderCycleId);
          criteria.andEqualTo("bizOrgCode", bizOrgCode);
          List<String> processCodeList = Lists.newArrayList(processCodes.split(SystemConstant.COMMA));
          criteria.andIn("progressCode", processCodeList);
          List<DirOrderProcessCopy> orderProcessCopyList = dirOrderProcessCopyMapper.selectByExample(example);
          if (CollectionUtils.isEmpty(orderProcessCopyList)) {
               return Collections.emptyMap();
          }
          Map<String, List<OrderProcessConfigItemOut>> map = new HashMap<>();
          for (DirOrderProcessCopy orderProcessCopy : orderProcessCopyList) {
               String processConfigItem = orderProcessCopy.getProcessConfigItem();
               List<OrderProcessConfigOut> configOutList = JSONArray.parseArray(processConfigItem, OrderProcessConfigOut.class);
               for (OrderProcessConfigOut orderProcessConfigOut : configOutList) {
                    map.put(orderProcessConfigOut.getProcessConfigCode(), orderProcessConfigOut.getConfigItemList());
               }
          }
          if (StringUtils.isBlank(processConfigCodes)) {
               return map;
          }
          String[] configCodeArray = processConfigCodes.split(SystemConstant.COMMA);
          Map<String, List<OrderProcessConfigItemOut>> map1 = new HashMap<>(configCodeArray.length);
          for (String orderProcessConfigCode : configCodeArray) {
               if (map.containsKey(orderProcessConfigCode)) {
                    map1.put(orderProcessConfigCode, map.get(orderProcessConfigCode));
               }
          }
          return map1;
     }

}
