package icu.helltab.itool.multablequery;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.helltab.itool.multablequery.config.db.CusBaseService;
import icu.helltab.itool.multablequery.config.db.MySqlRunner;
import icu.helltab.itool.multablequery.config.db.multi.ds01.DSConfig01;

@Transactional(transactionManager = DSConfig01.CONF.TRANS, rollbackFor = Throwable.class)
@MapperScan(value = "icu.helltab.itool.multablequery.mapper", sqlSessionFactoryRef = DSConfig01.CONF.FACTORY)
public class DemoBaseService<M extends BaseMapper<T>, T> extends CusBaseService<M, T> {


	@Resource(name = DSConfig01.CONF.SQL_RUNNER)
	MySqlRunner mySqlRunner;

	@Override
	protected MySqlRunner getMySqlRunner() {
		return mySqlRunner;
	}
}
