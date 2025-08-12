package com.edc.erp.redismq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.service.DisOrderTrackServer;
import com.edc.erp.distribution.entity.OrdDisOrderTrack;
import com.edc.erp.distribution.model.out.OrdDisOrderTrackOut;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 订单追踪处理类
 *
 * @author : wei
 */
@Slf4j
@Component
public class DisOrderTrackMessageReceive implements DisOrderTrackServer {

    @Autowired
    private OrdDisOrderTrackService ordDisOrderTrackService;

    @Autowired
    private RedisService redisService;

    /**
     * 接收消息的方法
     */
    @Override
    public void disReceiveMessage(String message) {
        log.info("接收到订单追踪日志消息-----{}", message);
        if (StringUtils.isEmpty(message)) {
            log.info("订单追踪日志新增消息内容为空");
            return;
        }
        OrdDisOrderTrackOut orderTrackOut = JSONObject.toJavaObject(JSON.parseObject(message), OrdDisOrderTrackOut.class);
        if (Objects.isNull(orderTrackOut)) {
            return;
        }
        String orderTrackLockKey = "disOrderTrack:" + orderTrackOut.getBizOrgCode() + ":" + orderTrackOut.getOrderNo() + orderTrackOut.getOrderStatus();
        String lockOrderNo = redisService.get(orderTrackLockKey);
        if (StringUtils.isNotEmpty(lockOrderNo)) {
            log.info("加盟配销订货单" + orderTrackOut.getOrderNo() + orderTrackOut.getOrderStatus() + "入库时重复消费");
        } else {
            OrdDisOrderTrack orderTrack = new OrdDisOrderTrack();
            BeanUtils.copy(orderTrackOut, orderTrack);
            orderTrack.setCreateTime(DateUtils.parseTime(orderTrackOut.getCreateTimeStr()));
            ordDisOrderTrackService.saveOrderTrack(orderTrack);
            redisService.set(orderTrackLockKey, orderTrack.getOrderNo() + orderTrack.getOrderStatus(), 5, TimeUnit.MINUTES);
        }
    }
}
