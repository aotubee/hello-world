//package com.edc.erp.orderscheduing.mq.consumer;
//
//import com.edc.erp.handle.DisRequestOrderHandle;
//import com.edc.erp.orderscheduing.handle.OrderProcessSchedulingHandle;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import com.edc.plugins.redis.RedisService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
///**
// * 消费订单处于 "已付款" 和 "已提交" 状态下，要货单创建之前的消息
// *
// * @author : zhangyao@tseveryday.com
// * @date 2021-07-06 15:37
// */
//@Service("dirBeforeCreateRequestOrderConsumerService")
//@Slf4j
//public class DirBeforeCreateRequestOrderConsumerService implements MessageProcessor {
//
//    @Value("${mq.consumer.handle-dir-before-create-request-order.tags}")
//    private String tags;
//
//    @Value("${mq.consumer.handle-dir-before-create-request-order.topic}")
//    private String topic;
//
//    @Autowired
//    private RedisService redisService;
//
//    @Autowired
//    private OrderProcessSchedulingHandle orderProcessSchedulingHandle;
//
//    @Autowired
//    private DisRequestOrderHandle requestOrderHandle;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        return false;
//    }
//
//}
