package icu.helltab.itool.multablequery;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.helltab.itool.multablequery.config.db.multi.MySqlRunner;
import icu.helltab.itool.multablequery.config.db.CusBaseService;

public class DemoBaseService<M extends BaseMapper<T>, T> extends CusBaseService<M, T> {


	@Resource
    MySqlRunner mySqlRunner;

	@Override
	protected MySqlRunner getMySqlRunner() {
		return mySqlRunner;
	}
}
