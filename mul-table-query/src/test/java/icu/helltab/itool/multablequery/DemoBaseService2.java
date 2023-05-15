package icu.helltab.itool.multablequery;

import javax.annotation.Resource;

import icu.helltab.itool.multablequery.config.db.multi.MySqlRunner;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.helltab.itool.multablequery.config.db.CusBaseService;

@Transactional(transactionManager = "mysql-helltab_TM", rollbackFor = Throwable.class)
//@MapperScan(value = "icu.helltab.itool.multablequery.mapper", sqlSessionFactoryRef = DSConfig01.CONF.FACTORY)
public class DemoBaseService2<M extends BaseMapper<T>, T> extends CusBaseService<M, T> {


	@Resource(name = "mysql-helltab_RUNNER")
    MySqlRunner mySqlRunner;

	@Override
	protected MySqlRunner getMySqlRunner() {
		return mySqlRunner;
	}
}
