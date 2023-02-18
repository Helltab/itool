package icu.helltab.itool.multablequery.config.db.multi.ds01;


import javax.annotation.Resource;

import org.mybatis.spring.annotation.MapperScan;
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
import icu.helltab.itool.multablequery.config.db.MySqlRunner;
import icu.helltab.itool.multablequery.config.db.handler.MyMetaObjectHandler;
import icu.helltab.itool.multablequery.config.db.multi.MultiDatasourceProperties;

import static icu.helltab.itool.multablequery.config.db.multi.ds01.DSConfig01.CONF.DS;
import static icu.helltab.itool.multablequery.config.db.multi.ds01.DSConfig01.CONF.FACTORY;
import static icu.helltab.itool.multablequery.config.db.multi.ds01.DSConfig01.CONF.IDX;
import static icu.helltab.itool.multablequery.config.db.multi.ds01.DSConfig01.CONF.SQL_RUNNER;
import static icu.helltab.itool.multablequery.config.db.multi.ds01.DSConfig01.CONF.TRANS;

/**
 * 多数据源配置
 */
@Configuration
@Primary
public class DSConfig01 implements IMybatisPlusConfig {

	/**
	 * !!!!注意, 复制本类后, 需要修改 import
	 */
	public interface CONF {
		int IDX = 1;
		String DS = "MY_DS0" + IDX;
		String FACTORY = DS + "_FACTORY";
		String TRANS = DS + "_TRANS";
		String SQL_RUNNER = DS + "_SQL_RUNNER";
	}

	@Resource
	MultiDatasourceProperties multiDatasource;

	@Resource
	ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers;

	@Bean(DS)
	public DruidDataSource datasource() {
		// note fix there when copy
		return multiDatasource.copy(IDX - 1);
	}

	@Bean(FACTORY)
	public MybatisSqlSessionFactoryBean factoryBean() {
		return mybatisSqlSessionFactory(multiDatasource.copy(IDX - 1));
	}

	@Bean(TRANS)
	public JdbcTransactionManager tm() {
		return transactionManager(transactionManagerCustomizers, multiDatasource.copy(IDX - 1));
	}

	@Bean(SQL_RUNNER)
	public MySqlRunner mySqlRunner() throws Exception {
		MybatisSqlSessionFactoryBean factoryBean = factoryBean();
		return new MySqlRunner(factoryBean);
	}

	@Resource
	MyMetaObjectHandler myMetaObjectHandler;
	@Override
	public MetaObjectHandler metaObjectHandler() {
		return myMetaObjectHandler;
	}
}
