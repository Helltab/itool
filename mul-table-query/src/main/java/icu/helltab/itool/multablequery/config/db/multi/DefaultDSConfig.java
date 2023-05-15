package icu.helltab.itool.multablequery.config.db.multi;


import javax.annotation.Resource;

import icu.helltab.itool.multablequery.config.db.IMybatisPlusConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.support.JdbcTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;


/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:52
 * @desc 默认数据源, 符合大多数只有一个数据源的场景使用
 * 如果配置了多个数据源, 其他数据源会在
 * @see ScanConfig 中进行初始化
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
		MybatisSqlSessionFactoryBean factoryBean = factoryBean();
		multiDatasource.handleCustomer(
				multiDatasource.defaultDsName(), factoryBean
		);
		return mySqlRunner(factoryBean);
	}

}
