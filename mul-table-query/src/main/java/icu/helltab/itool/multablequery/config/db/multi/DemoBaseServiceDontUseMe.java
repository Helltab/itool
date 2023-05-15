package icu.helltab.itool.multablequery.config.db.multi;

import javax.annotation.Resource;

import icu.helltab.itool.multablequery.config.db.CusBaseService;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 多数据源示例
 * todo
 * 所有的该数据源的 service 都需要继承本 Service
 */
@Transactional(transactionManager = "这里需要替换: ${数据源名}_TM", rollbackFor = Throwable.class)
public class DemoBaseServiceDontUseMe<M extends BaseMapper<T>, T> extends CusBaseService<M, T> {

	/**
	 * todo
	 */
	@Resource(name = "这里需要替换:${数据源名}_RUNNER")
	MySqlRunner mySqlRunner;

	protected MySqlRunner getMySqlRunner() {
		return mySqlRunner;
	}
}
