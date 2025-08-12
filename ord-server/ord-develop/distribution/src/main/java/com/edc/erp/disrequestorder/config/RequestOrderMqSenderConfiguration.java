package com.edc.erp.disrequestorder.config;

import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.mq.MqProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fxw
 * @description: 集货单消息生产者配置
 * @since 2022/10/24 15:11
 */
@Configuration
public class RequestOrderMqSenderConfiguration {

//    /**
//     * 向调度模块发送集货单创建完成消息
//     *
//     * @param mqProducer
//     * @return
//     */
//    @Bean("afterCreateRequestOrderSender")
//    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
//    @ConfigurationProperties(prefix = "mq.producer.after-create-request-order-sender")
//    public MessageSender requestOrderSender(MqProducer mqProducer) {
//        return new MessageSender(mqProducer);
//    }
}
