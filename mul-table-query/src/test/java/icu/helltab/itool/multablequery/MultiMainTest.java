package icu.helltab.itool.multablequery;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import icu.helltab.itool.multablequery.config.db.multi.MySqlRunner;
import icu.helltab.itool.multablequery.mapper.UserInfo;
import icu.helltab.itool.multablequery.mapper2.UserService2;
import icu.helltab.itool.multablequery.mapper.RoleInfo;
import icu.helltab.itool.multablequery.mapper.RoleUser;
import icu.helltab.itool.multablequery.mapper.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import icu.helltab.itool.multablequery.config.db.query.lambda.SqlLambdaBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MultiMain.class)
public class MultiMainTest {

    @Resource(name = "mysql-helltab_RUNNER")
    MySqlRunner mySqlRunner;

    @Autowired
    UserService userService;
    @Autowired
    UserService2 userService2;

    private <P> void type (Func1<P, ?> func1) {
        System.out.println(LambdaUtil.getRealClass(func1));
        try {
            Method method = func1.getClass().getMethod("call", Object.class);
            Type[] parameterTypes = method.getGenericParameterTypes();
            Type[] typeArguments = ((ParameterizedType) parameterTypes[0]).getActualTypeArguments();
            Class<?> parameterType = (Class<?>) typeArguments[0];
            System.out.println(parameterType.getName());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void des() {
//        type(UserInfo::getId);
        System.out.println(SqlLambdaBuilder.lambda(q -> {
            q.selectRaw(q.fun("sum({})").bind(UserInfo::getAge).as(UserInfo::getAge))
                    .from(UserInfo.class)
                    .join(RoleInfo.class, j -> {
                        j.eq(RoleInfo::getId, UserInfo::getRoleId);
                    })
                    .leftJoin(RoleInfo.class, j -> {
                        j.eq(RoleInfo::getId, UserInfo::getRoleId);
                    })
                    .group(UserInfo::getUsername)
                    .order(UserInfo::getId, true)
            ;
        }));
    }

    @Test
    public void testFun() {


        System.out.println(SqlLambdaBuilder.lambda(q -> {
            q.selectRaw(q.fun("sum({})").bind(UserInfo::getAge).as(UserInfo::getAge))
                    .from(UserInfo.class)
                    .join(RoleInfo.class, j -> {
                        j.eq(RoleInfo::getId, UserInfo::getRoleId);
                    })
                    .leftJoin(RoleInfo.class, j -> {
                        j.eq(RoleInfo::getId, UserInfo::getRoleId);
                    })
                    .group(UserInfo::getUsername)
                    .order(UserInfo::getId, true)
            ;
        }));

        System.out.println(SqlLambdaBuilder.lambda(q -> {
            q.selectRaw(q.fun("sum({})").bind(UserInfo::getAge).as(UserInfo::getAge))
                    .from(UserInfo.class);
        }));
        System.out.println(SqlLambdaBuilder.lambda(q -> {
            q.selectFun(q.fun("sum({})").bind(UserInfo::getAge), UserInfo::getAge)
                    .from(UserInfo.class);
        }));
        System.out.println(SqlLambdaBuilder.lambda(q -> {
            q.selectFun(q.fun("sum({})").bind(UserInfo::getAge), UserInfo::getAge)
                    .from(UserInfo.class)
                    .eq(false, q.fun("concat({}, {})").bind(UserInfo::getAge)
                            .bind(UserInfo::getUsername), "23")
                    .eq(false, q.fun("concat({}, {})").bind(UserInfo::getAge)
                            .bind(UserInfo::getUsername), "")
                    .eq(q.fun("concat({}, {})").bind(UserInfo::getAge)
                            .bind(UserInfo::getUsername), "")
                    .like(q.fun("concat({}, {})").bind(UserInfo::getAge)
                            .bind(UserInfo::getUsername), "23")
                    .like(true, false, q.fun("concat({}, {})").bind(UserInfo::getAge)
                            .bind(UserInfo::getUsername), "23")
                    .eqNull(q.fun("concat({}, {})").bind(UserInfo::getAge)
                            .bind(UserInfo::getUsername))
            ;
        }));
    }

    @Test
    public void test1() {

//		System.out.println(JSONUtil.toJsonStr(userService.pageQuery(q -> {
//		})));
//		System.out.println(userService.exOne(q -> {
//			q.from(UserInfo.class)
//			;
//		}, UserInfo.class));
//		System.out.println(userService.getOne(q -> {
//			q.last("limit 1");
//		}));
    }

    @Test
    public void test() {
        userService.getOne(q -> {
            q.last("limit 1");
        });
        List<UserInfo> objects = userService.exList(sql -> {
            sql.select(UserInfo::getUsername, sql.ALIAS(0))
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
    public void testIn() {
        List<Map<String, Object>> maps = mySqlRunner.selectLambda(sql -> {
            sql.from(UserInfo.class)
                    .in(UserInfo::getUsername, "fp", "hjc")
            ;
        });
        System.out.println(maps);
//		Object
    }

    @Test
    public void testName() {
        userService.list();
        List<Map<String, Object>> maps = mySqlRunner.selectLambda(sql -> {
            sql.from(UserInfo.class)
                    .in(UserInfo::getUsername, "fp", "hjc")
            ;
        });
        System.out.println(maps);
    }

    @Test
    public void testMulTableQuery() {
        List<UserInfo> objects = userService.exList(sql -> {

            // 单个字段查询
            sql.select(UserInfo::getUsername);
            sql.select(UserInfo::getUsername, sql.ALIAS(1));
            // 多个字段查询
            sql.select(UserInfo::getUsername, UserInfo::getPassword);

            sql.select(UserInfo::getUsername, sql.ALIAS(1))
                    .select(UserInfo::getPassword, sql.ALIAS(2));
            // 子查询
            sql.select(inner -> {
                inner
                        .selectCount(UserInfo::getId, sql.ALIAS(1))
                        .from(UserInfo.class)
                        .eq(UserInfo::getUsername, "张三");
            }, UserInfo::getCount);

            // 原始查询
            sql.selectRaw("Ada username", "1 password");
            sql.from(UserInfo.class, RoleInfo.class, RoleUser.class);
//			sql.from(inner->{
//				sql.select(UserInfo::getUsername, 0)
//					.from(UserInfo.class);
//			});

            sql.leftJoin(UserInfo.class, join -> {
                        join.eq(UserInfo::getId, RoleUser::getUserId);
                    })
                    .leftJoin(RoleInfo.class, join -> {
                        join.eq(RoleInfo::getId, RoleUser::getRoleId);
                    })
            ;

            sql.eq(UserInfo::getUsername, "张三");
            sql.neq(UserInfo::getUsername, "张三");
            sql.le(UserInfo::getAge, 20);
            sql.lt(UserInfo::getAge, 20);
            sql.ge(UserInfo::getAge, 20);
            sql.gt(UserInfo::getAge, 20);
//			sql.in(UserInfo::getAge,0, 20);
//			sql.notIn(UserInfo::getAge, 20, 18);
//			sql.notIn(UserInfo::getAge, inner->inner.selectRaw("1"));
            sql.exists(UserInfo::getAge, inner -> inner.selectRaw("1"));
            sql.like(UserInfo::getUsername, "*尚*");
            sql.notLike(UserInfo::getAge, "*尚*");

            sql.group(UserInfo::getId).group(UserInfo::getAge).group(UserInfo::getUsername);

            sql.having(UserInfo::getUsername, "='张三'");

            sql.order(UserInfo::getAge, true);
            sql.order(UserInfo::getAge, sql.ALIAS(0), true);

        }, UserInfo.class);
    }

    @Test
    public void testFrom() {
        System.out.println(SqlLambdaBuilder.lambda(sql -> {
            sql.select(UserInfo::getUsername)
                    .from(UserInfo.class)
                    .from(inner -> {
                        inner.selectRaw("1")
                                .from(UserInfo.class);
                    }, sql.SUB_ALIAS(0))
                    .eq(UserInfo::getUsername, UserInfo::getUsername, sql.ALIAS(0), sql.SUB_ALIAS(0));
            ;
        }));
    }

    @Test
    public void testJoin() {
        System.out.println(SqlLambdaBuilder.lambda(sql -> {
            sql.select(UserInfo::getUsername)
                    .from(UserInfo.class)
                    .leftJoin(RoleInfo.class, join -> {
                        join.eq(RoleInfo::getId, UserInfo::getRoleId);
                    })
                    .eq(UserInfo::getUsername, UserInfo::getUsername, sql.ALIAS(0), sql.SUB_ALIAS(0))
                    .and(con -> {
                        con.eq(UserInfo::getUsername, UserInfo::getUsername, sql.ALIAS(0), sql.SUB_ALIAS(0))
                                .or(con2 -> {
                                    con2.eq(UserInfo::getUsername, UserInfo::getUsername, sql.ALIAS(0), sql.SUB_ALIAS(0));
                                });
                    })
            ;
            ;
        }));
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
