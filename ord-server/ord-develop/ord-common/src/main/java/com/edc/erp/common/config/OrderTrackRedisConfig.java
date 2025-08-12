package com.edc.erp.common.config;

import com.edc.erp.common.service.OrderTrackMessageServer;
import com.edc.plugins.redis.RedisConfigVO;
import com.edc.plugins.redis.factory.MyRedisConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-03-06 15:49
 */
@Configuration
public class OrderTrackRedisConfig {

    @Value("${redisMq.disOrderTrackTopic}")
    private String disOrderTrackTopic;

    @Value("${redisMq.dirOrderTrackTopic}")
    private String dirOrderTrackTopic;

    @Autowired
    RedisConfigVO redisConfigVO;

    @Bean
    public RedisMessageListenerContainer container(MessageListenerAdapter messageListenerAdapter) {
        LettuceConnectionFactory lettuceConnectionFactory = MyRedisConfigurator.getConnection(redisConfigVO);
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactory);
        //订阅主题（可订阅多个）
        container.addMessageListener(messageListenerAdapter, new PatternTopic(disOrderTrackTopic));
        container.addMessageListener(messageListenerAdapter, new PatternTopic(dirOrderTrackTopic));
        //这个container 可以添加多个 messageListener
        return container;
    }

    @Bean
    MessageListenerAdapter ordServerListenerAdapter(OrderTrackMessageServer receiver) {
        //这个地方 是给messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“receiveMessage”
        //也有好几个重载方法，这边默认调用处理器的方法 叫handleMessage 可以自己到源码里面看
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
}
