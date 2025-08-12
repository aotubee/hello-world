package com.edc.erp.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderProcessConfig;
import com.edc.erp.common.entity.OrderProcessCopy;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.mapper.OrderProcessCopyMapper;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.model.out.OrderProcessConfigOut;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.service.DisOrderProcessCopyService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
public class DisOrderProcessCopyServiceImpl extends BaseServiceImpl<OrderProcessCopy> implements DisOrderProcessCopyService {

     @Autowired
     private OrderProcessCopyMapper orderProcessCopyMapper;

     @Autowired
     private DisOrderCycleHandle disOrderCycleHandle;

     /**
      * 按条件查找全流程副本
      *
      * @param orderingCycleId 订货周期id
      * @param storeCode       门店代码
      * @param bizOrgCode         组织代码
      * @return
      */
     @Override
     public List<OrderProcessCopy> findOrderProcessCopyListByParameter(Integer orderingCycleId, String storeCode, String bizOrgCode) {
          OrderProcessCopy orderProcessCopy = new OrderProcessCopy();
          orderProcessCopy.setOrderCycleId(orderingCycleId);
          orderProcessCopy.setStoreCode(storeCode);
          orderProcessCopy.setBizOrgCode(bizOrgCode);
          return orderProcessCopyMapper.select(orderProcessCopy);
     }

//     /**
//      * 保存流程副本
//      *
//      * @param orderProcessCopy
//      */
//     @Override
//     public void save(OrderProcessCopy orderProcessCopy) {
//          orderProcessCopyMapper.insertOrderProcessCopy(orderProcessCopy);
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
     public OrderProcessCopy getNextOrderProcessCopyByParameter(Integer orderingCycleId, String storeCode, String progressCode, String bizOrgCode) {
          log.info("组织{}下门店{}订货周期主键{}当前流程代码{}", bizOrgCode, storeCode, orderingCycleId, progressCode);
          OrderProcessCopy orderProcessCopy = orderProcessCopyMapper.getOneByParameter(orderingCycleId, storeCode, progressCode, bizOrgCode);
          if (Objects.isNull(orderProcessCopy)) {
               return null;
          }
          return orderProcessCopyMapper.getNextOneByParameter(orderingCycleId, storeCode, orderProcessCopy.getId(), bizOrgCode);
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
     public OrderProcessConfigItemOut getOrderProcessConfigItemOut(Integer orderCycleId, String bizOrgCode, String processCode, String orderProcessConfigCode) {
          OrderProcessCopy orderProcessCopy = new OrderProcessCopy();
          orderProcessCopy.setProgressCode(processCode);
          orderProcessCopy.setBizOrgCode(bizOrgCode);
          orderProcessCopy.setOrderCycleId(orderCycleId);
          orderProcessCopy = this.getOrderProcessCopy(orderProcessCopy);
          if (Objects.isNull(orderProcessCopy)) {
               OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, bizOrgCode);
               log.info("门店{}订货周期{}截单时间{}的副本不存在", orderCycle.getStoreCode(), orderCycle.getShortOrderType(), orderCycle.getTruncationDateTime());
               return null;
          }
          String processConfigItem = orderProcessCopy.getProcessConfigItem();
          List<OrderProcessConfigOut> configList = JSONArray.parseArray(processConfigItem, OrderProcessConfigOut.class);
          Map<String, OrderProcessConfigOut> configOutMap = configList.stream().collect(Collectors.toMap(OrderProcessConfig::getProcessConfigCode, Function.identity()));
          OrderProcessConfigOut orderProcessConfigOut = configOutMap.get(orderProcessConfigCode);
          if (Objects.isNull(orderProcessConfigOut)) {
               return null;
          }
          if (CollectionUtils.isEmpty(orderProcessConfigOut.getConfigItemList())) {
               return null;
          }
          return orderProcessConfigOut.getConfigItemList().get(0);
     }

     /**
      * 按条件查找一个流程副本
      * @param orderProcessCopy
      * @return
      */
     @Override
     public OrderProcessCopy getOrderProcessCopy(OrderProcessCopy orderProcessCopy) {
          return orderProcessCopyMapper.selectOne(orderProcessCopy);
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
     public List<OrderProcessConfigItemOut> findOrderProcessConfigItemOut(Integer id, String bizOrgCode, String processCode, String orderProcessConfigCode) {
          OrderProcessCopy orderProcessCopy = new OrderProcessCopy();
          orderProcessCopy.setProgressCode(processCode);
          orderProcessCopy.setBizOrgCode(bizOrgCode);
          orderProcessCopy.setOrderCycleId(id);
          orderProcessCopy = this.getOrderProcessCopy(orderProcessCopy);
          if (Objects.isNull(orderProcessCopy)) {
               OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(id, bizOrgCode);
               log.info("门店{}订货周期{}截单时间{}的副本不存在", orderCycle.getStoreCode(), orderCycle.getShortOrderType(), orderCycle.getTruncationDateTime());
               return null;
          }
          String processConfigItem = orderProcessCopy.getProcessConfigItem();
          List<OrderProcessConfigOut> configList = JSONArray.parseArray(processConfigItem, OrderProcessConfigOut.class);
          Map<String, OrderProcessConfigOut> configOutMap = configList.stream().collect(Collectors.toMap(OrderProcessConfig::getProcessConfigCode, Function.identity()));
          OrderProcessConfigOut orderProcessConfigOut = configOutMap.get(orderProcessConfigCode);
          return orderProcessConfigOut.getConfigItemList();
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
     public OrderProcessCopy getOrderProcessCopyByParameter(Integer orderCycleId, String storeCode, String processCode, String bizOrgCode) {
          return orderProcessCopyMapper.getOneByParameter(orderCycleId, storeCode, processCode,bizOrgCode);
     }

     @Override
     public Map<String, List<OrderProcessConfigItemOut>> findConfigItemMapByParam(Integer orderCycleId, String storeCode, String bizOrgCode, String processCodes, String orderProcessConfigCodes) {
          Example example = new Example(OrderProcessCopy.class);
          Example.Criteria criteria = example.createCriteria();
          criteria.andEqualTo("orderCycleId", orderCycleId);
          criteria.andEqualTo("bizOrgCode", bizOrgCode);
          List<String> processCodeList = Lists.newArrayList(processCodes.split(SystemConstant.COMMA));
          criteria.andIn("progressCode", processCodeList);
          List<OrderProcessCopy> orderProcessCopyList = orderProcessCopyMapper.selectByExample(example);
          if (CollectionUtils.isEmpty(orderProcessCopyList)) {
               return Collections.emptyMap();
          }
          Map<String, List<OrderProcessConfigItemOut>> map = new HashMap<>();
          for (OrderProcessCopy orderProcessCopy : orderProcessCopyList) {
               String processConfigItem = orderProcessCopy.getProcessConfigItem();
               List<OrderProcessConfigOut> configOutList = JSONArray.parseArray(processConfigItem, OrderProcessConfigOut.class);
               for (OrderProcessConfigOut orderProcessConfigOut : configOutList) {
                    map.put(orderProcessConfigOut.getProcessConfigCode(), orderProcessConfigOut.getConfigItemList());
               }
          }
          if (StringUtils.isBlank(orderProcessConfigCodes)) {
               return map;
          }
          String[] configCodeArray = orderProcessConfigCodes.split(SystemConstant.COMMA);
          Map<String, List<OrderProcessConfigItemOut>> map1 = new HashMap<>(configCodeArray.length);
          for (String orderProcessConfigCode : configCodeArray) {
               if (map.containsKey(orderProcessConfigCode)) {
                    map1.put(orderProcessConfigCode, map.get(orderProcessConfigCode));
               }
          }
          return map1;
     }


}
