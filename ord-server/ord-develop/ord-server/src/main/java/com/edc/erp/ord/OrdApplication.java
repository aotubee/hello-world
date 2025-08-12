package com.edc.erp.ord;

import com.edc.erp.reducestock.config.ZkSyncStockWarehouseSenderConfig;
import com.edc.plugins.common.config.BaseBootApplication;
import com.edc.plugins.feignclient.config.GlobalFeignClientsConfiguration;
import com.edc.plugins.mybatis.config.TkMyBatisConfiguration;
import com.edc.plugins.redis.RedisConfig;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.sdk.dictionary.config.DictionaryPluginConfiguration;
import com.edc.sdk.log.config.LogServerMqSenderConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author fxw
 * @description: 订货启动类
 * @since 2022/12/2 17:34
 */
@BaseBootApplication
@EnableFeignClients(basePackages = {"com.edc.erp.*.rpc", "com.edc.sdk.log.rpc", "com.edc.sdk.dts.rpc"})
@MapperScan(basePackages = {"com.edc.erp.mapper", "com.edc.erp.*.mapper", "com.edc.erp.*.*.mapper"})
@Import({DictionaryPluginConfiguration.class, TkMyBatisConfiguration.class, RedisConfig.class, UniqueUtils.class,
        TaskLockUtils.class, GlobalFeignClientsConfiguration.class, LogServerMqSenderConfiguration.class, ZkSyncStockWarehouseSenderConfig.class})
@ComponentScan(basePackages = {"com.edc.plugins.*", "com.edc.erp", "com.edc.sdk.log", "com.edc.sdk.dts"})
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class OrdApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdApplication.class, args);
    }

    /**
     * URL特殊字符过滤
     *
     * @return
     */
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedQueryChars", "|{}[]\\"));
        return factory;
    }
}
