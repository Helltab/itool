package icu.helltab.itool.multablequery;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import icu.helltab.itool.multablequery.config.db.multi.MultiDatasourceProperties;

public class DemoCustomConfig extends MultiDatasourceProperties {
	static  {
		register("mysql-fp", factory->{
			MybatisConfiguration configuration = factory.getConfiguration();
			configuration.setCacheEnabled(false);
		});
	}
}
