package com.edc.erp.directly.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.directly.entity.DirGoodsCombination;
import com.edc.erp.directly.mapper.DirGoodsCombinationMapper;
import com.edc.erp.directly.service.DirGoodsCombinationService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 商品组合表(GoodsCombination)表服务实现类
 *
 * @author fxw
 * @since 2022-10-19 15:08:50
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DirGoodsCombinationServiceImpl extends BaseServiceImpl<DirGoodsCombination> implements DirGoodsCombinationService {
     
     private final DirGoodsCombinationMapper dirGoodsCombinationMapper;

     private final StoreCenterService storeCenterService;

     /**
      * 根据商品组合值查询订单类型配置信息
      *
      * @param storeCode
      * @param bizOrgCode
      * @param combinationTypeValueList
      * @return
      */
     @Override
     public Integer getOrderTypeConfigIdByCombinationTypeValues(String storeCode, String bizOrgCode, List<String> combinationTypeValueList) {
          List<Integer> orderTypeConfigIdList = storeCenterService.findByStoreCode(storeCode);
          if (CollectionUtils.isEmpty(orderTypeConfigIdList)) {
               return null;
          }
          // 条件去重
          combinationTypeValueList = combinationTypeValueList.stream().collect(
                  Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(combinationTypeValue -> combinationTypeValue))), ArrayList::new));
          // 根据组织和订单类型id查找商品组合集合
          List<DirGoodsCombination> goodsCombinationList = dirGoodsCombinationMapper.findByOrgCodeAndOrderTypeConfigIdList(bizOrgCode, orderTypeConfigIdList);
          if (CollectionUtils.isEmpty(goodsCombinationList)) {
               throw new BusinessException(storeCode + "未匹配到订单流商品组合配置");
          }
          Map<String, List<DirGoodsCombination>> combinationTypeCodeMap = goodsCombinationList.stream().collect(Collectors.groupingBy(DirGoodsCombination::getCombinationValue));
//          log.info("门店{}combinationType配置转MAP-----{}",storeCode, JSONObject.toJSONString(combinationTypeCodeMap));
          // 符合当前查询条件的商品组合集合
          List<DirGoodsCombination> eligibleGoodsCombinationList = Lists.newArrayList();
          StringJoiner combinationTypeValueJoiner = new StringJoiner(",", "[", "]");
          combinationTypeValueList.forEach(combinationTypeValue -> {
               combinationTypeValueJoiner.add(combinationTypeValue);
               combinationTypeCodeMap.forEach((key, value) -> {
                    Map<String, String> keyMap = Arrays.asList(key.split(",")).stream().collect(Collectors.toMap(String::intern, Function.identity()));
                    if (Objects.nonNull(keyMap.get(combinationTypeValue))) {
                         eligibleGoodsCombinationList.addAll(value);
                    }
               });
          });
          // 计算每个订单类型的商品组合条件数，如果条件数与查询条件数一样，认为该订单类型是目标值
          Map<Integer, Long> countOrderTypeConfigIdMap = eligibleGoodsCombinationList.stream().collect(Collectors.groupingBy(DirGoodsCombination::getOrderTypeConfigId, Collectors.counting()));
//          log.info("门店{}combinationType配置转countOrderTypeConfigIdMap-----{}",storeCode, JSONObject.toJSONString(countOrderTypeConfigIdMap));
          List<Integer> orderTypeConfigIds = Lists.newArrayList();
          int checkCombinationTypeValueSize = combinationTypeValueList.size();
//          log.info("门店{}combinationType配置checkCombinationTypeValueSize长度-----{}",storeCode, checkCombinationTypeValueSize);
          countOrderTypeConfigIdMap.forEach((key, value) -> {
               if (value == checkCombinationTypeValueSize) {
                    orderTypeConfigIds.add(key);
               }
          });
          if (CollectionUtils.isEmpty(orderTypeConfigIds)) {
               throw new BusinessException(storeCode + "该" + combinationTypeValueJoiner.toString() + "条件下查找没有找到匹配的订单类型配置。");
          }
          if (orderTypeConfigIds.size() > 1) {
               throw new BusinessException("该" + combinationTypeValueJoiner.toString() + "该条件下查找出多条订单类型配置。");
          }
          return orderTypeConfigIds.get(0);
     }
}
