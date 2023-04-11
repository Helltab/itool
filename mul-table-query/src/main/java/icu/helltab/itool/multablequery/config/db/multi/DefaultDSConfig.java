package icu.helltab.itool.multablequery.config.db.multi;


import javax.annotation.Resource;

import icu.helltab.itool.multablequery.config.db.plugins.PrintSqlPlugin;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.support.JdbcTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import icu.helltab.itool.multablequery.config.db.IMybatisPlusConfig;
import icu.helltab.itool.multablequery.config.db.handler.MyMetaObjectHandler;


/**
 * 多数据源配置
 */
@Configuration
@Primary

public class DefaultDSConfig implements IMybatisPlusConfig {
	@Resource
	MultiDatasourceProperties multiDatasource;

	@Resource
	ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers;

	@Bean
	@Primary
	public DruidDataSource datasource() {
		// note fix there when copy
		return multiDatasource.defaultDs();
	}

	@Bean
	@Primary
	public MybatisSqlSessionFactoryBean factoryBean() {
		MybatisSqlSessionFactoryBean factoryBean = mybatisSqlSessionFactory(multiDatasource.defaultDs());
		multiDatasource.handleCustomer(
			multiDatasource.defaultDsName(), factoryBean
		);
		return factoryBean;
	}

	@Bean
	@Primary
	public JdbcTransactionManager tm() {
		return transactionManager(transactionManagerCustomizers, multiDatasource.defaultDs());
	}

	@Bean
	@Primary
	public MySqlRunner mySqlRunner() {
		return mySqlRunner(factoryBean());
	}

}
