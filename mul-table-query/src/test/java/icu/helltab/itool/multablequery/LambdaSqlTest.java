package icu.helltab.itool.multablequery;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.hutool.db.sql.SqlUtil;
import icu.helltab.itool.multablequery.config.db.query.lambda.SqlLambdaBuilder;
import icu.helltab.itool.multablequery.mapper.RoleInfo;
import icu.helltab.itool.multablequery.mapper.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * lambda sql 测试
 */
@RunWith(JUnit4.class)
@Slf4j
public class LambdaSqlTest {
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
			sql.select(UserInfo::getRoleId, UserInfo::getPassword)
				.from(UserInfo.class);
		});
		System.out.println(SqlUtil.formatSql(result));
		Assert.assertTrue(result.contains("SELECT"));
		Assert.assertTrue(result.contains("role_id"));
		Assert.assertTrue(result.contains("password"));

		System.out.println("--3-- 注意这里重复表名的处理");
		result = SqlLambdaBuilder.lambda(sql -> {
			sql.select(UserInfo::getRoleId, 1)
				.select(UserInfo::getPassword, 1)
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
