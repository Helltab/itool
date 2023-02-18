package icu.helltab.itool.multablequery;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.hutool.core.collection.ListUtil;
import icu.helltab.itool.multablequery.config.db.MySqlRunner;
import icu.helltab.itool.multablequery.config.db.multi.ds01.DSConfig01;
import icu.helltab.itool.multablequery.mapper.RoleInfo;
import icu.helltab.itool.multablequery.mapper.RoleUser;
import icu.helltab.itool.multablequery.mapper.UserInfo;
import icu.helltab.itool.multablequery.mapper.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MultiMain.class)
public class MultiMainTest {

	@Resource(name = DSConfig01.CONF.SQL_RUNNER)
	MySqlRunner mySqlRunner;

	@Autowired
	UserService userService;
	@Test
	public void test() {
		List<UserInfo> objects = userService.exList(sql -> {
			sql.select(UserInfo::getUsername, 0)
				.from(UserInfo.class)
			;
		}, UserInfo.class);
		System.out.println(objects);
		List<Map<String, Object>> maps = mySqlRunner.selectLambda(sql -> {
			sql.select(UserInfo::getUsername)
				.from(UserInfo.class);
		});
		List<Map<String, Object>> maps1 = mySqlRunner.selectLambda(sql -> {
			sql.selectRaw("'Ada' username", "1 password")
				.from(UserInfo.class);
		});
		System.out.println(maps);
	}
	@Test
	public void testMulTableQuery() {
		List<UserInfo> objects = userService.exList(sql -> {

			// 单个字段查询
			sql.select(UserInfo::getUsername);
			sql.select(UserInfo::getUsername, 1);
			// 多个字段查询
			sql.select(UserInfo::getUsername, UserInfo::getPassword);

			sql.select(UserInfo::getUsername, 1)
				.select(UserInfo::getPassword, 2);
			// 子查询
			sql.select(inner->{
				inner
					.selectCount(UserInfo::getId, 1)
					.from(UserInfo.class)
					.eq(UserInfo::getUsername, "张三");
			}, UserInfo::getCount);

			// 原始查询
			sql.selectRaw("Ada username", "1 password");
			sql.from(UserInfo.class, RoleInfo.class, RoleUser.class);
			sql.from(inner->{
				sql.select(UserInfo::getUsername, 0)
					.from(UserInfo.class);
			});

			sql.leftJoin(UserInfo::getId, RoleUser::getUserId)
				.leftJoin(RoleInfo::getId, RoleUser::getRoleId)
			;
			sql.leftJoin(UserInfo::getId, RoleUser::getUserId, 0, 0)
				.leftJoin(RoleInfo::getId, RoleUser::getRoleId, 0, 0)
			;

			sql.eq(UserInfo::getUsername, "张三");
			sql.neq(UserInfo::getUsername, "张三");
			sql.le(UserInfo::getAge, 20);
			sql.lt(UserInfo::getAge, 20);
			sql.ge(UserInfo::getAge, 20);
			sql.gt(UserInfo::getAge, 20);
			sql.in(UserInfo::getAge, 20, 18);
			sql.notIn(UserInfo::getAge, 20, 18);
			sql.notIn(UserInfo::getAge, inner->inner.selectRaw("1"));
			sql.exists(UserInfo::getAge, inner->inner.selectRaw("1"));
			sql.like(UserInfo::getUsername, "*尚*");
			sql.notLike(UserInfo::getAge, "*尚*");

			sql.group(UserInfo::getId, UserInfo::getAge, UserInfo::getUsername);

			sql.group(
				ListUtil.of(0, 0, 0),
				UserInfo::getId, UserInfo::getAge, UserInfo::getUsername
			);
			sql.having(UserInfo::getUsername, "='张三'");

			sql.order(UserInfo::getAge, true);
			sql.order(UserInfo::getAge, 0, true);

		}, UserInfo.class);
	}

	@Before
	public void setUp() {
		System.out.println("start");
	}

	@After
	public void tearDown() {
		System.out.println("end");
	}

}
