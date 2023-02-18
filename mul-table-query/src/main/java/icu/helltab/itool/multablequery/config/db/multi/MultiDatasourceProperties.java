package icu.helltab.itool.multablequery.config.db.multi;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.ZipUtil;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ConfigurationProperties("system.jdbc.datasource")
@Component
public class MultiDatasourceProperties {

	private static Map<Integer, DruidDataSource> CACHE = new HashMap<>();
	private List<MyDataSourceProperties> connections;

	/**
	 * 详细配置
	 * @see com.alibaba.druid.filter.FilterManager
	 */
	private String filters;
	/**
	 * 详细配置
	 * @see  com.alibaba.druid.pool.DruidAbstractDataSource validationQuery
	 */
	private String validationQuery;


	/**
	 * 从基本数据源复制出新的数据源, 满足多数据源需求
	 * @param idx 数据源序号
	 * @return
	 */
	public DruidDataSource copy(int idx) {
		if (CACHE.containsKey(idx)) {
			return CACHE.get(idx);
		}
		DruidDataSource multiDatasource = new DruidDataSource();
		MyDataSourceProperties conn = connections.get(idx);
		multiDatasource.setUrl(conn.getUrl());
		multiDatasource.setUsername(conn.getUsername());
		multiDatasource.setPassword(conn.getPassword());
		multiDatasource.setDriverClassName(conn.getDriverClassName());
		try {
			multiDatasource.setFilters(getFilters());
			multiDatasource.setValidationQuery(getValidationQuery());
		} catch (SQLException ignore) {
		}

		CACHE.put(idx, multiDatasource);
		return multiDatasource;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class MyDataSourceProperties extends DataSourceProperties {
		private List<String> scanPackages;
	}


}
