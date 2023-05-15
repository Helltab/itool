package icu.helltab.itool.multablequery.config.db.handler;

import org.apache.ibatis.reflection.MetaObject;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

/**
 * @author Helltab
 * @desc mybatis plus 自动填充响应的字段
 * 可以在
 * @see
 * @date 2023/4/18-12:44
 */

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
