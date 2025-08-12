package com.edc.erp.mq;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.util.MqUtil;
import com.edc.erp.presale.model.in.UpdatePresaleOrderPaidForMqIn;
import com.edc.erp.presale.service.OrdDisPresaleOrderService;
import com.edc.plugins.jms.MessageProcessor;
import com.edc.plugins.jms.entity.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 预售订单支付后更新状态消费者
 */
@Service("presaleOrderConsumerService")
@Slf4j
@RequiredArgsConstructor
public class PresaleOrderConsumerServiceImpl implements MessageProcessor {
    @Value("${mq.consumer.update-presale-order-paid-consumer.tags}")
    private String tags;
    @Value("${mq.consumer.update-presale-order-paid-consumer.topic}")
    private String topic;
    @Autowired
    private OrdDisPresaleOrderService ordDisPresaleOrderService;

    @Override
    public boolean process(MqMessage mqMessage) {
        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
        if (null == jsonObject) {
            return true;
        }
        log.info("预售订单支付后更新状态消息：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
        UpdatePresaleOrderPaidForMqIn updatePresaleOrderPaidForMqIn = JSONObject.toJavaObject(jsonObject, UpdatePresaleOrderPaidForMqIn.class);
        try {
            ordDisPresaleOrderService.updatePresaleOrderAfterPaidSuccess(updatePresaleOrderPaidForMqIn.getPresaleOrderId(), updatePresaleOrderPaidForMqIn.getLoginUsername());
        } catch (Exception e) {
            log.error("预售订单支付后更新状态异常：{},{}", mqMessage.getMessageId(), updatePresaleOrderPaidForMqIn.getPresaleOrderId(), e);
            return false;
        }
        return true;
    }
}


