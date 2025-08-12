package com.edc.erp.ord.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-02-09 17:00
 */
@Configuration
@Slf4j
@MapperScan(basePackages = {"com.edc.erp.common.mapper.dss"},
        sqlSessionFactoryRef = "dssSqlSessionFactory", sqlSessionTemplateRef = "dssSqlSessionTemplate")
public class DssDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.dss.slave")
    public DataSource dssDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "dssSqlSessionFactory")
    public SqlSessionFactory dssSqlSessionFactory(@Qualifier("dssDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*Mapper.xml"));
        bean.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        return bean.getObject();
    }

    @Bean(name = "flowTransactionManager")
    public DataSourceTransactionManager flowTransactionManager(@Qualifier("dssDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "dssSqlSessionTemplate")
    public SqlSessionTemplate dssSqlSessionTemplate(@Qualifier("dssSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
