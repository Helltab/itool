package icu.helltab.itool.multablequery.config.db.multi;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.helltab.itool.multablequery.config.db.CusBaseService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Throwable.class)
public class DefaultBaseService<M extends BaseMapper<T>, T> extends CusBaseService<M, T> {


	@Resource
	MySqlRunner mySqlRunner;


	@Override
	protected MySqlRunner getMySqlRunner() {
		return mySqlRunner;
	}
}
