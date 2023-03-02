package icu.helltab.itool.multablequery.config.db.handler;

import java.sql.JDBCType;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

@Component
@ConditionalOnMissingBean(MetaObjectHandler.class)
public class MyMetaObjectHandler implements MetaObjectHandler {


	@Override
	public void insertFill(MetaObject metaObject) {
		this.setFieldValByName("createTime", LocalDateTimeUtil.now(), metaObject);
		this.setFieldValByName("updateTime", LocalDateTimeUtil.now(), metaObject);
		this.setFieldValByName("createDate", LocalDateTimeUtil.now(), metaObject);
		this.setFieldValByName("updateDate", LocalDateTimeUtil.now(), metaObject);
		this.setFieldValByName("updateDate", LocalDateTimeUtil.now(), metaObject);
		this.setFieldValByName("isDelete", 0, metaObject);
	}


	@Override
	public void updateFill(MetaObject metaObject) {
		this.setFieldValByName("updateTime", LocalDateTimeUtil.now(), metaObject);
		this.setFieldValByName("updateDate", LocalDateTimeUtil.now(), metaObject);
	}


}
