package com.edc.erp.distribution.service.impl;

import com.alibaba.fastjson.JSON;
import com.edc.erp.distribution.entity.OrdDisOrderTrack;
import com.edc.erp.distribution.model.in.DisOrderTrackIn;
import com.edc.erp.distribution.model.out.DisOrderTrackElementOut;
import com.edc.erp.distribution.model.out.DisOrderTrackOut;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.erp.distribution.mapper.OrdDisOrderTrackMapper;
import com.edc.erp.enumeration.OrderTrackBusinessTypeEnum;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


/**
 * 配销订单追踪表(OrdDisOrderTrack)表服务实现类
 *
 * @author fxw
 * @since 2022-10-18 14:17:38
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrdDisOrderTrackServiceImpl extends BaseServiceImpl<OrdDisOrderTrack> implements OrdDisOrderTrackService {

     private final OrdDisOrderTrackMapper ordDisOrderTrackMapper;

     private final RedisService redisService;

     @Value("${redisMq.disOrderTrackTopic}")
     private String disOrderTrackTopic;

     /**
      * 根据单号查询业务日志
      *
      * @param disOrderTrackIn
      * @return
      */
     @Override
     public List<DisOrderTrackElementOut> findOrderTrackOutListByParameters(DisOrderTrackIn disOrderTrackIn) {
          List<DisOrderTrackOut> orderTrackOutList = ordDisOrderTrackMapper.findOrderTrackOutListByParameters(disOrderTrackIn);
          Map<String, List<DisOrderTrackOut>> map = new LinkedHashMap<>();
          for (DisOrderTrackOut disOrderTrackOut : orderTrackOutList) {
               List<DisOrderTrackOut> list = map.get(disOrderTrackOut.getOrderStatus());
               if (CollectionUtils.isEmpty(list)){
                    list = new ArrayList<>();
               }
               list.add(disOrderTrackOut);
               map.put(disOrderTrackOut.getOrderStatus(),list);
          }
          List<DisOrderTrackElementOut> orderTrackElementOuts = new ArrayList<>();
          for (Map.Entry<String, List<DisOrderTrackOut>> entry : map.entrySet()) {
               DisOrderTrackElementOut orderTrackElementOut = new DisOrderTrackElementOut();
               orderTrackElementOut.setOrderStatus(entry.getKey());
               orderTrackElementOut.setOrderTrackOutList(entry.getValue());
               orderTrackElementOuts.add(orderTrackElementOut);
          }
          return orderTrackElementOuts;
     }

     /**
      * 保存订单追踪记录
      *
      * @param orderTrack
      */
     @Override
     @Transactional(rollbackFor = Exception.class)
     public void saveOrderTrack(OrdDisOrderTrack orderTrack) {
          ordDisOrderTrackMapper.insert(orderTrack);
     }

     /**
      * 缓存订单追踪消息
      *
      * @param orderNo
      * @param storeCode
      * @param orderStatus
      * @param trackLog
      * @param bizOrgCode
      * @param creator
      * @param createTime
      */
     @Override
     public void pushRedisOrderTrackMessage(String orderNo, String storeCode, String orderStatus, String trackLog, String bizOrgCode, String creator, LocalDateTime createTime) {
          DisOrderTrackOut orderTrackOut = new DisOrderTrackOut();
          orderTrackOut.setOrderNo(orderNo);
          orderTrackOut.setStoreCode(storeCode);
          orderTrackOut.setOrderStatus(orderStatus);
          orderTrackOut.setTrackLog(trackLog);
          orderTrackOut.setBusinessType(OrderTrackBusinessTypeEnum.ORDER.getName());
          orderTrackOut.setBizOrgCode(bizOrgCode);
          orderTrackOut.setCreator(creator);
          orderTrackOut.setCreateTimeStr(DateUtils.format(createTime));
          String orderTrackLockKey = "disOrderTrack:" + bizOrgCode + ":" + orderNo + orderStatus;
          String lockOrderNo = redisService.get(orderTrackLockKey);
          if (StringUtils.isEmpty(lockOrderNo)) {
               redisService.getRedisTemplate().convertAndSend(disOrderTrackTopic, JSON.toJSONString(orderTrackOut));
          } else {
               log.info(orderNo + orderStatus + "重复放入队列中");
          }
     }
}
