package com.edc.erp.directly.redismq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.model.out.OrdDirOrderTrackOut;
import com.edc.erp.common.service.DirOrderTrackServer;
import com.edc.erp.directly.distribution.entity.OrdDirOrderTrack;
import com.edc.erp.directly.distribution.service.OrdDirOrderTrackService;
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
 * 直营订单追踪处理类
 *
 * @author : wei
 */
@Slf4j
@Component
public class DirOrderTrackMessageReceive implements DirOrderTrackServer {

    @Autowired
    private OrdDirOrderTrackService ordDirOrderTrackService;

    @Autowired
    private RedisService redisService;

    /**
     * 直营接收消息的方法
     */
    @Override
    public void dirReceiveMessage(String message) {
        log.info("接收到订单追踪日志消息-----{}", message);
        if (StringUtils.isEmpty(message)) {
            log.info("订单追踪日志新增消息内容为空");
            return;
        }
        OrdDirOrderTrackOut orderTrackOut = JSONObject.toJavaObject(JSON.parseObject(message), OrdDirOrderTrackOut.class);
        if (Objects.isNull(orderTrackOut)) {
            return;
        }
        String orderTrackLockKey = "dirOrderTrack:" + orderTrackOut.getBizOrgCode() + ":" + orderTrackOut.getOrderNo() + orderTrackOut.getOrderStatus();
        String lockOrderNo = redisService.get(orderTrackLockKey);
        if (StringUtils.isNotEmpty(lockOrderNo)) {
            log.info("直营订货单" + orderTrackOut.getOrderNo() + orderTrackOut.getOrderStatus() + "入库时重复消费");
        } else {
            OrdDirOrderTrack orderTrack = new OrdDirOrderTrack();
            BeanUtils.copy(orderTrackOut, orderTrack);
            orderTrack.setCreateTime(DateUtils.parseTime(orderTrackOut.getCreateTimeStr()));
            ordDirOrderTrackService.saveOrderTrack(orderTrack);
            redisService.set(orderTrackLockKey, orderTrack.getOrderNo() + orderTrack.getOrderStatus(), 5, TimeUnit.MINUTES);
        }
    }

}
