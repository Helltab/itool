package icu.helltab.itool.multablequery.config.db.injector;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;

public class SelectCustom extends AbstractMethod {


	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, "${sql}", modelClass);
		return this.addSelectMappedStatementForTable(mapperClass, "selectCustom", sqlSource, tableInfo);
	}
}
