package com.edc.erp.directly.distribution.service.impl;

import com.alibaba.fastjson.JSON;
import com.edc.erp.directly.distribution.entity.OrdDirOrderTrack;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderTrackMapper;
import com.edc.erp.directly.distribution.model.in.DirOrderTrackIn;
import com.edc.erp.directly.distribution.model.out.DirOrderTrackElementOut;
import com.edc.erp.directly.distribution.model.out.DirOrderTrackOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderTrackService;
import com.edc.erp.directly.enumeration.OrderTrackBusinessTypeEnum;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * 直营订单追踪表(OrdDirOrderTrack)表服务实现类
 *
 * @author fxw
 * @since 2022-11-18 18:46:57
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDirOrderTrackServiceImpl extends BaseServiceImpl<OrdDirOrderTrack> implements OrdDirOrderTrackService {
     
     private final OrdDirOrderTrackMapper ordDirOrderTrackMapper;


     private final RedisService redisService;

     @Value("${redisMq.dirOrderTrackTopic}")
     private String dirOrderTrackTopic;

     /**
      * 根据单号查询业务日志
      *
      * @param disOrderTrackIn
      * @return
      */
     @Override
     public List<DirOrderTrackElementOut> findOrderTrackOutListByParameters(DirOrderTrackIn disOrderTrackIn) {
          List<DirOrderTrackOut> orderTrackOutList = ordDirOrderTrackMapper.findOrderTrackOutListByParameters(disOrderTrackIn);
          Map<String, List<DirOrderTrackOut>> map = new LinkedHashMap<>();
          for (DirOrderTrackOut disOrderTrackOut : orderTrackOutList) {
               List<DirOrderTrackOut> list = map.get(disOrderTrackOut.getOrderStatus());
               if (CollectionUtils.isEmpty(list)){
                    list = new ArrayList<>();
               }
               list.add(disOrderTrackOut);
               map.put(disOrderTrackOut.getOrderStatus(),list);
          }
          List<DirOrderTrackElementOut> orderTrackElementOuts = new ArrayList<>();
          for (Map.Entry<String, List<DirOrderTrackOut>> entry : map.entrySet()) {
               DirOrderTrackElementOut orderTrackElementOut = new DirOrderTrackElementOut();
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
     public void saveOrderTrack(OrdDirOrderTrack orderTrack) {
          ordDirOrderTrackMapper.insert(orderTrack);
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
          DirOrderTrackOut orderTrackOut = new DirOrderTrackOut();
          orderTrackOut.setOrderNo(orderNo);
          orderTrackOut.setStoreCode(storeCode);
          orderTrackOut.setOrderStatus(orderStatus);
          orderTrackOut.setTrackLog(trackLog);
          orderTrackOut.setBusinessType(OrderTrackBusinessTypeEnum.ORDER.getName());
          orderTrackOut.setBizOrgCode(bizOrgCode);
          orderTrackOut.setCreator(creator);
          orderTrackOut.setCreateTimeStr(DateUtils.format(createTime));
          String orderTrackLockKey = "dirOrderTrack:" + bizOrgCode + ":" + orderNo + orderStatus;
          String lockOrderNo = redisService.get(orderTrackLockKey);
          if (StringUtils.isEmpty(lockOrderNo)) {
               redisService.getRedisTemplate().convertAndSend(dirOrderTrackTopic, JSON.toJSONString(orderTrackOut));
          } else {
               log.info(orderNo + orderStatus + "重复放入队列中");
          }
     }
}
