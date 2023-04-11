package icu.helltab.itool.multablequery;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import cn.hutool.db.sql.SqlUtil;
import icu.helltab.itool.multablequery.config.db.query.lambda.SqlLambdaBuilder;
import icu.helltab.itool.multablequery.mapper.RoleInfo;
import icu.helltab.itool.multablequery.mapper.UserInfo;
import icu.helltab.itool.multablequery.mapper.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * lambda sql 测试
 */
@RunWith(JUnit4.class)
@Slf4j
public class LambdaSqlTest {

	@Resource
	UserService baseService;
	@Before
	public void setUp() {
		System.out.println("----------------start");
	}

	@After
	public void tearDown() {
		System.out.println("----------------end");
	}

	/**
	 * 测试 select
	 * 1. select 单个
	 * 2. select 多个
	 * 3. select 复杂情况
	 */
	@Test
	public void testSelect() {
		String result = SqlLambdaBuilder.lambda(sql -> {
			sql.select(UserInfo::getRoleId)
				.from(UserInfo.class);
		});
		System.out.println("--1--");
		System.out.println(SqlUtil.formatSql(result));
		Assert.assertTrue(result.contains("SELECT"));
		Assert.assertTrue(result.contains("role_id"));

		System.out.println("--2--");
		result = SqlLambdaBuilder.lambda(sql -> {
			sql.from(UserInfo.class);
		});
		System.out.println(SqlUtil.formatSql(result));
		Assert.assertTrue(result.contains("SELECT"));
		Assert.assertTrue(result.contains("role_id"));
		Assert.assertTrue(result.contains("password"));

		System.out.println("--3-- 注意这里重复表名的处理");
		result = SqlLambdaBuilder.lambda(sql -> {
			sql.select(UserInfo::getRoleId, sql.ALIAS(1))
				.select(UserInfo::getPassword, sql.ALIAS(1))
				.select(inner -> {
					inner.selectCount(UserInfo::getId)
						.from(UserInfo.class);
				}, UserInfo::getCount)
				.from(UserInfo.class);
		});
		System.out.println(SqlUtil.formatSql(result));
		Assert.assertTrue(result.contains("SELECT"));
		Assert.assertTrue(result.contains("role_id"));
	}

	@Test
	public void testJoin() {

		baseService.getOne(q->{
			q.select(UserInfo::getUsername)
				.eq(UserInfo::getId, "");
		});

		baseService.exOne(q->{
			q.select(UserInfo::getUsername)
				.from(UserInfo.class)
				.leftJoin(RoleInfo.class, join -> {
					join.eq(RoleInfo::getId, UserInfo::getRoleId);
				})
				.eq(UserInfo::getId, "")
			;
		}, UserInfo.class);

		String result = SqlLambdaBuilder.lambda(sql -> {
			sql.select(UserInfo::getUsername)
				.select(RoleInfo::getRoleName, RoleInfo::getId)
				.from(UserInfo.class, RoleInfo.class)
				.eq(UserInfo::getRoleId, RoleInfo::getCreate_by)
			;
		});
		System.out.println(SqlUtil.formatSql(result));
		Assert.assertTrue(result.contains("SELECT"));
		Assert.assertTrue(result.contains("username"));
		Assert.assertTrue(result.contains("role_id"));
		Assert.assertTrue(result.contains("role_name"));
	}
}
