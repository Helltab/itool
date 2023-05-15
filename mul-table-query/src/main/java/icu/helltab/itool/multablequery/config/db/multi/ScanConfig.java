package icu.helltab.itool.multablequery.config.db.multi;

import icu.helltab.itool.multablequery.config.db.IMybatisPlusConfig;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.support.JdbcTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:40
 * @desc 这里是多数据源的配置及 mapper 扫描类
 * @see MapperScannerConfigurer
 * @link 如果对默认的 mybatisplus 配置不满意, 可以自定义, 参考测试包中: DemoCustomConfig
 */
@Configuration
@Slf4j
public class ScanConfig {

	@Bean
	public BeanFactoryPostProcessor beanFactory(Environment environment,
												ApplicationContext applicationContext,
												ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
		BindResult<MultiDatasourceProperties> jdbcProperties = Binder.get(environment)
			.bind("system.jdbc.datasource", MultiDatasourceProperties.class);
		MultiDatasourceProperties multiDatasourceProperties = jdbcProperties.get();
		DefaultListableBeanFactory defaultListableBeanFactory =
			(DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		return processor -> {
			// 该 factory 不会注册到 spring 的上下文中
			DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
			multiDatasourceProperties.getConnections().forEach((db, scan) -> {
				MapperScannerConfigurer scanner = new MapperScannerConfigurer();
				DruidDataSource forkDs = multiDatasourceProperties.fork(db);
				IMybatisPlusConfig mpConfig = new IMybatisPlusConfig() {
				};

				// 非默认的数据源,走这里
				if (!multiDatasourceProperties.isDefault(db)) {
					//sqlSessionFactoryBean
					MybatisSqlSessionFactoryBean sqlSessionFactoryBean = mpConfig.mybatisSqlSessionFactory(forkDs);
					multiDatasourceProperties.handleCustomer(db, sqlSessionFactoryBean);
					//transactionManager
					JdbcTransactionManager tm = mpConfig.transactionManager(transactionManagerCustomizers, forkDs);
					// SQL runner
					MySqlRunner sqlRunner = mpConfig.mySqlRunner(sqlSessionFactoryBean);
					defaultListableBeanFactory.registerSingleton(db + "_DS", forkDs);
					defaultListableBeanFactory.registerSingleton(db + "_FACTORY", sqlSessionFactoryBean);
					defaultListableBeanFactory.registerSingleton(db + "_TM", tm);
					defaultListableBeanFactory.registerSingleton(db + "_RUNNER", sqlRunner);
					scanner.setSqlSessionFactoryBeanName(db + "_FACTORY");
				}

				scanner.setBasePackage(String.join(",", scan.getScanPackages()));
				beanFactory.registerSingleton(db + "_MAPPER_SCAN", scanner);
				scanner.postProcessBeanDefinitionRegistry(processor instanceof BeanDefinitionRegistry ?
					(BeanDefinitionRegistry) processor : null);
			});

		};
	}


}
