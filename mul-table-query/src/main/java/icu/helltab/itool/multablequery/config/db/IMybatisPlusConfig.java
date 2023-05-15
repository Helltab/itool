package icu.helltab.itool.multablequery.config.db;

import javax.sql.DataSource;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import icu.helltab.itool.multablequery.config.db.handler.MyMetaObjectHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.LocalCacheScope;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.jdbc.support.JdbcTransactionManager;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import icu.helltab.itool.multablequery.config.db.multi.MySqlRunner;
import icu.helltab.itool.multablequery.config.db.plugins.PrintSqlPlugin;

/**
 * Topic mybatis-plus 的公用配置
 * 使用: 实现这个接口, 传入 datasource 即可
 *
 * @author helltab
 * @version 1.0
 * @date 2021/10/14 9:20
 */
public interface IMybatisPlusConfig {

	/*
	 * 配置入口: sessionFactory
	 *          configLocation: mybatis 配置文件路径
	 *          mapperLocations: xml 对应位置
	 *          executorType
	 *          configurationProperties
	 *          --以下为包扫描--
	 *          typeAliasesPackage: 别名包
	 *          typeAliasesSuperType
	 *          typeHandlersPackage
	 *          configuration: 如下
	 *          globalConfig: 如下
	 *     1.configuration: mybatis 原生配置
	 *          mapUnderscoreToCamelCase: 自动驼峰
	 *          defaultEnumTypeHandler: 枚举处理器
	 *          autoMappingUnknownColumnBehavior: 映射失败时的处理方案
	 *          localCacheScope: 本地缓存
	 *          cacheEnabled: 二级缓存
	 *          callSettersOnNulls: 结果为 null 时是否存到对象中
	 *          configurationFactory:todo 暂时不清楚具体表现
	 *     2.global-config mybatis-plus 专有配置
	 *          banner
	 *          enableSqlRunner: 是否初始化
	 *              @see SqlRunner
	 *          sqlInjector: 注入器
	 *          superMapperClass
	 *          metaObjectHandler: 元对象字段填充控制器, 用于自动填充
	 *          identifierGenerator: Id 生成器
	 *          dbConfig: 如下
	 *     2.1.db-config
	 *          idType: 默认主键类型
	 *          tablePrefix: 表前缀
	 *          schema: 库
	 *          columnFormat: todo 暂时不清楚具体表现
	 *          propertyFormat
	 *          tableUnderline: 表名转下划线
	 *          capitalMode: 大写命名
	 *          keyGenerator: 主键生成器
	 *          logicDeleteField: 逻辑删除
	 *          logicDeleteValue
	 *          logicNotDeleteValue
	 *          insertStrategy: 插入策略 todo 暂时不清楚具体表现
	 *          updateStrategy
	 *          whereStrategy
	 *
	 */
	default MybatisSqlSessionFactoryBean mybatisSqlSessionFactory(DataSource dataSource) {
		MybatisSqlSessionFactoryBean sessionFactoryBean = new MybatisSqlSessionFactoryBean();
		sessionFactoryBean.setConfiguration(mybatisConfiguration());
		sessionFactoryBean.setGlobalConfig(globalConfig());
		sessionFactoryBean.setPlugins(plugins());
		sessionFactoryBean.setDataSource(dataSource);
		SpringManagedTransactionFactory transactionFactory = new SpringManagedTransactionFactory();
		sessionFactoryBean.setTransactionFactory(transactionFactory);
		return sessionFactoryBean;
	}

	default Interceptor[] plugins() {
		MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
		PaginationInnerInterceptor innerInterceptor = new PaginationInnerInterceptor();
		innerInterceptor.setOverflow(false);
		mybatisPlusInterceptor.addInnerInterceptor(innerInterceptor);
		return new Interceptor[]{
			new PrintSqlPlugin(),
				mybatisPlusInterceptor
		};
	}


	default MetaObjectHandler metaObjectHandler() {
		return new MyMetaObjectHandler();
	}


	default MybatisSqlSessionFactoryBean factoryBean() {return null;}

	/**
	 * mybatis 原生属性
	 *
	 * @return MybatisConfiguration
	 */
	default MybatisConfiguration mybatisConfiguration() {
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.setDefaultEnumTypeHandler(org.apache.ibatis.type.EnumTypeHandler.class);
		configuration.setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
		configuration.setLocalCacheScope(LocalCacheScope.SESSION);
		configuration.setCacheEnabled(false);
		configuration.setCallSettersOnNulls(false);

		return configuration;
	}

	/**
	 * plus 配置
	 *
	 * @return GlobalConfig
	 */
	default GlobalConfig globalConfig() {
		GlobalConfig global = new GlobalConfig();
		global.setBanner(false);
		global.setDbConfig(dbConfig());
		global.setEnableSqlRunner(true);
//		global.setSqlInjector(new SqlInjector());
		global.setMetaObjectHandler(metaObjectHandler());
		return global;
	}

	/**
	 * 数据库配置
	 *
	 * @return GlobalConfig.DbConfig
	 */
	default GlobalConfig.DbConfig dbConfig() {
		GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
		dbConfig.setIdType(IdType.ASSIGN_ID);
		dbConfig.setLogicDeleteField("IS_DELETE");
		dbConfig.setLogicDeleteValue("1");
		dbConfig.setLogicNotDeleteValue("0");
		dbConfig.setTableUnderline(true);
		dbConfig.setCapitalMode(false);
		return dbConfig;
	}

	default JdbcTransactionManager transactionManager(ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers, DataSource dataSource) {
		JdbcTransactionManager transactionManager = new JdbcTransactionManager(dataSource);
		transactionManagerCustomizers.ifAvailable((customizers) -> customizers.customize(transactionManager));
		return transactionManager;
	}

	default MySqlRunner mySqlRunner(MybatisSqlSessionFactoryBean sqlSessionFactoryBean) {
		return new MySqlRunner(sqlSessionFactoryBean);
	}


}
