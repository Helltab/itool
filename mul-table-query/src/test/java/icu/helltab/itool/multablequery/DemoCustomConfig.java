package icu.helltab.itool.multablequery;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import icu.helltab.itool.multablequery.config.db.handler.MyMetaObjectHandler;
import icu.helltab.itool.multablequery.config.db.multi.MultiDatasourceProperties;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:45
 * @desc 这是数据源自定义配置的钩子, 在这里可以更改 mybatis 的 MybatisSqlSessionFactoryBean 配置
 * @see
 */
public class DemoCustomConfig extends MultiDatasourceProperties {
	static  {
		// 注册数据源对应的回调函数, 可以修改 MybatisSqlSessionFactoryBean 的属性
		register("mysql", factory->{
			MybatisConfiguration configuration = factory.getConfiguration();
			configuration.setCacheEnabled(false);
			configuration.getGlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler());
		});
	}
}
