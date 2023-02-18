package icu.helltab.itool.multablequery.config.db.injector;

import java.util.List;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.AbstractSqlInjector;
import com.baomidou.mybatisplus.core.injector.methods.Delete;
import com.baomidou.mybatisplus.core.injector.methods.DeleteByMap;
import com.baomidou.mybatisplus.core.injector.methods.Insert;
import com.baomidou.mybatisplus.core.injector.methods.SelectByMap;
import com.baomidou.mybatisplus.core.injector.methods.SelectCount;
import com.baomidou.mybatisplus.core.injector.methods.SelectList;
import com.baomidou.mybatisplus.core.injector.methods.SelectMaps;
import com.baomidou.mybatisplus.core.injector.methods.SelectMapsPage;
import com.baomidou.mybatisplus.core.injector.methods.SelectObjs;
import com.baomidou.mybatisplus.core.injector.methods.SelectPage;
import com.baomidou.mybatisplus.core.injector.methods.Update;

import static java.util.stream.Collectors.toList;

public class SqlInjector extends AbstractSqlInjector {
	@Override
	public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
		Stream.Builder<AbstractMethod> builder = Stream.<AbstractMethod>builder()
			.add(new Insert())
			.add(new Delete())
			.add(new DeleteByMap())
			.add(new Update())
			.add(new SelectByMap())
			.add(new SelectCount())
			.add(new SelectMaps())
			.add(new SelectMapsPage())
			.add(new SelectObjs())
			.add(new SelectList())
			.add(new SelectPage())
			.add(new SelectCustom())
			;
		return builder.build().collect(toList());
	}


}
