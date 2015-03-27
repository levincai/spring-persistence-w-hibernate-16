package com.apress.springpersistence.audiomanager.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import java.util.Properties;

@Configuration
@Profile("jpa")
@EnableJpaRepositories("com.apress.springpersistence.audiomanager.core")
@EnableTransactionManagement
public class JpaConfig {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSourceConfigurationPropertiesBean dataSourceProperties;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        DataSourceBuilder factory = DataSourceBuilder
                .create(this.getClass().getClassLoader())
                .driverClassName(this.dataSourceProperties.getDriverClassName())
                .url(this.dataSourceProperties.getUrl())
                .username(this.dataSourceProperties.getUsername())
                .password(this.dataSourceProperties.getPassword());
        return factory.build();
    }


    @Bean
    public EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setDataSource(dataSource());
        lef.setJpaVendorAdapter(jpaVendorAdapter());
        lef.setJpaDialect(new HibernateJpaDialect());
        String[] packages = environment.getProperty("jpa.packages.to.scan").split(",");
        lef.setPackagesToScan(packages);

        Properties props = new Properties();
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
        props.put("hibernate.connection.charSet", "UTF-8");
        props.put("hibernate.current_session_context_class", "jta");
        props.put("hibernate.archive.autodetection", "class");
        props.put("hibernate.transaction.manager_lookup_class", "com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup");
        props.put("hibernate.dialect", environment.getProperty("jpa.dialect"));
        props.put("hibernate.hbm2ddl.auto", environment.getProperty("jpa.hibernate.create.strategy"));
        lef.setJpaProperties(props);

        lef.afterPropertiesSet();

        return lef.getObject();
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(false);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabasePlatform(environment.getProperty("jpa.dialect"));

        return hibernateJpaVendorAdapter;
    }

    @Bean
    public JpaTransactionManager jpaTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory());
        return transactionManager;
    }

    //todo: add transaction managers


}