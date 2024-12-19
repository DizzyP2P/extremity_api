package com.breech.extremity.config;


import com.github.pagehelper.PageInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import javax.sql.DataSource;
import java.util.Properties;

import static com.breech.extremity.core.constant.ExtremityConstant.*;
@Slf4j
@Configuration
public class MybatisConfigurer {

    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTypeAliasesPackage(MODEL_PACKAGE);

        Properties mybatisProperties = new Properties();
        mybatisProperties.setProperty("mapUnderscoreToCamelCase", "true");
        mybatisProperties.setProperty("cacheEnabled", "false");
        mybatisProperties.setProperty("lazyLoadingEnabled", "true");
        mybatisProperties.setProperty("defaultExecutorType", "REUSE");
        factory.setConfigurationProperties(mybatisProperties);

        PageInterceptor pageHelper = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("pageSizeZero", "true");
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        pageHelper.setProperties(properties);
        factory.setPlugins(pageHelper);

        // 设置 XML Mapper 文件路径
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factory.setMapperLocations(resolver.getResources("classpath:mapper/**/*.xml"));
        return factory.getObject();
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactoryBean");
        mapperScannerConfigurer.setBasePackage(MAPPER_PACKAGE);

        Properties properties = new Properties();
        properties.setProperty("mappers", MAPPER_INTERFACE_REFERENCE);
        properties.setProperty("notEmpty", "false");
        properties.setProperty("IDENTITY", "JDBC");
        mapperScannerConfigurer.setProperties(properties);

        return mapperScannerConfigurer;
    }
}