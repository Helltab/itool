package icu.helltab.itool.multablequery.config.db.multi;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ConfigurationProperties("system.jdbc.datasource")
@Component
public class MultiDatasourceProperties {


	private static Map<String, DruidDataSource> CACHE = new HashMap<>();
	private LinkedHashMap<String, MyDataSourceProperties> connections;

	private Class<MultiDatasourceProperties>  customConfig;

	/**
	 * 详细配置
	 *
	 * @see com.alibaba.druid.filter.FilterManager
	 */
	private String filters;

	/**
	 * 排除第一项
	 *
	 * @return
	 */
	public boolean isDefault(@NonNull String db) {
		return db.equals(connections.keySet().stream().findFirst().orElse(""));
	}

	/**
	 * 从基本数据源复制出新的数据源, 满足多数据源需求
	 *
	 * @param idx 数据源序号
	 * @return
	 */
	public DruidDataSource fork(String idx) {
		if (CACHE.containsKey(idx)) {
			return CACHE.get(idx);
		}
		DruidDataSource multiDatasource = new DruidDataSource();
		MyDataSourceProperties conn = connections.get(idx);
		multiDatasource.setUrl(conn.getUrl());
		multiDatasource.setUsername(conn.getUsername());
		multiDatasource.setPassword(conn.getPassword());
		multiDatasource.setDriverClassName(conn.getDriverClassName());
		multiDatasource.setValidationQuery(conn.getValidationQuery());
		try {
			multiDatasource.setFilters(getFilters());
		} catch (SQLException ignore) {
		}
		CACHE.put(idx, multiDatasource);
		return multiDatasource;
	}

	public DruidDataSource defaultDs() {
		String defaultDs = connections.keySet().stream()
			.findFirst().orElse("");
		return fork(defaultDs);
	}

	public String defaultDsName() {
		return connections.keySet().stream()
			.findFirst().orElse("");
	}


	private static Map<String, Consumer<MybatisSqlSessionFactoryBean>> consumerMap;

	/**
	 * 注册自定义配置方法
	 * 继承之后, 在 static {} 中注册即可
	 */
	protected static void register(String dsName, Consumer<MybatisSqlSessionFactoryBean> consumer) {
		if (consumerMap == null) {
			consumerMap = new HashMap<>();
		}
		consumerMap.put(dsName, consumer);
	}

	public void handleCustomer(String dsName, MybatisSqlSessionFactoryBean sqlSessionFactoryBean) {
		if (customConfig != null ) {
			try {
				// 这里触发自定义配置类的加载, 执行 static 初始化,注册处理器
				customConfig.newInstance();
				if (consumerMap != null && consumerMap.containsKey(dsName)) {
					consumerMap.get(dsName).accept(sqlSessionFactoryBean);
				}
			}catch (Exception e) {
			}
		}
	}
	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class MyDataSourceProperties extends DataSourceProperties {
		private List<String> scanPackages;
		private String validationQuery;

	}


}
