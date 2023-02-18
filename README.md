# mul-table-query

![image-20230218203546451](img/img.png)

基于 mybatis-plus的多数据源配置，多表联查工具

效果：不改写任何的 mapper 和 mapper.xml，轻松实现 crud

## 多数据源配置

1. 多数据源使用了 mybatis、mybatis-plus 以及 druid，mybatis相关的配置在   [IMybatisPlusConfig.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/IMybatisPlusConfig.java)  中，只有数据源和 druid 的配置需要单独配置。

2. 为了支持多数据源，使用了单独的数据源配置，前缀为`system.jdbc.datasource`, 见 [MultiDatasourceProperties.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/multi/MultiDatasourceProperties.java) 

   ```yaml
   spring:
     datasource:
       # 使用alibaba的druid作为数据库连接池
       type: com.alibaba.druid.pool.DruidDataSource
       druid:
         # 连接池初始化连接数量
         initial-size: 5
         # 最小空闲连接数量
         min-idle: 5
         # 最大连接数量
         max-active: 20
         # 最大等待时间
         max-wait: 60000
         time-between-eviction-runs-millis: 60000
         # 单个连接在池中最小生存的时间，单位是毫秒
         min-evictable-idle-time-millis: 300000
         # 单个连接在池中最大生存的时间，单位是毫秒
         max-evictable-idle-time-millis: 900000
         # 以系统资源换稳定，连接空闲时检查有效性
         test-while-idle: true
         # 以系统资源换稳定，申请连接时检查有效性
         test-on-borrow: false
         # 以系统资源换稳定，回收连接时检查有效性
         test-on-return: false
         # 缓存statement，用本机内存换效率，但是通常可以关闭
         pool-prepared-statements: false
           # max-pool-prepared-statement-per-connection-size: 20
         # 监控过滤器
         web-stat-filter:
           enabled: true
           exclusions:
             - "*.js"
             - "*.gif"
             - "*.jpg"
             - "*.png"
             - "*.css"
             - "*.ico"
             - "/druid/*"
         # druid 监控页面
         stat-view-servlet:
           enabled: true
           url-pattern: /druid/*
           reset-enable: false
           login-username: root
           login-password: root
   system:
     jdbc:
       datasource:
         filters: stat,wall
         validationQuery: SELECT 1
         connections:
           - driver-class-name: com.mysql.cj.jdbc.Driver
             url: jdbc:mysql://xx:xx/xx?allowMultiQueries=true&createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
             username: xx
             password: xx
   ```

3. 使用数据源和设置事务管理，继承  [CusBaseService.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/CusBaseService.java) ，实现 getMySqlRunner() 方法，将对应的数据源 MySqlRunner 和 TransactionManager 设置好即可：

   1. @Transactional：transactionManager、rollbackFor
   2. @MapperScan：value、sqlSessionFactoryRef
   3. getMySqlRunner：需要指定名称

   ```java
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
   ```

4. 新增数据源: 仿造 [DSConfig01.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/multi/ds01/DSConfig01.java) ，改变其中的索引值

   ```java
   public interface CONF {
   		int IDX = 1;// <--改变这个，注意本页面引用的常量必须来自这里，而不是其他数据源的
   		String DS = "MY_DS0" + IDX;
   		String FACTORY = DS + "_FACTORY";
   		String TRANS = DS + "_TRANS";
   		String SQL_RUNNER = DS + "_SQL_RUNNER";
   	}
   ```

   

## 多表联查工具的使用

### Startup

1. 配置完数据源之后，即可使用多表联查了，当然也可以直接使用 MySqlRunner 中的方法，为了统一，推荐继承相关数据源的 Service，使用提供的查询能力。

   1. 继承相关数据源 Service

      ```java
      @Service
      public class UserService extends DemoBaseService<UserMapper, UserInfo> {
      
      }
      List<UserInfo> objects = userService.exList(sql -> {
          sql.select(UserInfo::getUsername)
            .from(UserInfo.class)
          ;
        }, UserInfo.class);
      ```

      

   2. 直接使用 MysqlRunner

      ```java
      @Resource(name = DSConfig01.CONF.SQL_RUNNER)
      MySqlRunner mySqlRunner;
      
      List<Map<String, Object>> maps = mySqlRunner.selectLambda(sql -> {
          sql.select(UserInfo::getUsername)
            .from(UserInfo.class);
        });
      ```

      

### API 说明

> 特别说明：因为 lambda 表达式的原理是推断表名，因此无法优雅的自定义某个表的别名：这里采取一个策略：别名自动生成，但是如果一个表在环境中出现两次以上，则需要指定其编号，如第一次出现的表是 0，第二次出现是 1，默认为 0；

#### select

```java
// 单个字段查询
1. select(UserInfo::getUsername);
2. select(UserInfo::getUsername, 1);
// 多个字段查询
1. select(UserInfo::getUsername, UserInfo::getPasswrod);//类型检测预警
2. select(UserInfo::getUsername)
  .select(UserInfo::getPassword);
3. select(UserInfo::getUsrname, 1)
  .select(UserInfo::getPasswrod,2);
//子查询
1. sql.select(inner->{
      inner
        .selectCount(UserInfo::getId, 1)
        .from(UserInfo.class)
        .eq(UserInfo::getUsername, "张三");
    }, UserInfo::getCount);
// 原始查询
1. sql.selectRaw("Ada username", "1 password");

```

#### from

```java
// 多表 inner join
sql.from(UserInfo.class, UserInfo.class, UserInfo.class, UserInfo.class);
// 子查询
sql.from(inner->{
  sql.select(UserInfo::getUsername, 0)
    .from(UserInfo.class);
});
```

#### join

```java
// left
sql.leftJoin(UserInfo::getId, RoleUser::getUserId)
  .leftJoin(RoleInfo::getId, RoleUser::getRoleId)
;
sql.leftJoin(UserInfo::getId, RoleUser::getUserId, 0, 0)
  .leftJoin(RoleInfo::getId, RoleUser::getRoleId, 0, 0)
;
// right full 类似
```

#### where

> 条件判断第一个参数前面可以添加一个 nullJudge 的布尔值，默认为 true
>
> true: 如果值为空，则不添加判断条件;
>
> false: 如果值不为空，则添加判断条件，如 eq 会变为 is null, neq 会变为 is not null

```java
// eq
sql.eq(UserInfo::getUsername, "张三");
sql.neq(UserInfo::getUsername, "张三");
// 小于等于
sql.le(UserInfo::getAge, 20);
// 小于
sql.lt(UserInfo::getAge, 20);
// 大于等于
sql.ge(UserInfo::getAge, 20);
// 大于
sql.gt(UserInfo::getAge, 20);
sql.in(UserInfo::getAge, 20, 18);
sql.notIn(UserInfo::getAge, 20, 18);
sql.notIn(UserInfo::getAge, inner->inner.selectRaw("1"));
sql.exists(UserInfo::getAge, inner->inner.selectRaw("1"));
sql.like(UserInfo::getUsername, "*尚*");
sql.notLike(UserInfo::getAge, "*尚*");
```

#### group

```java
sql.group(UserInfo::getId, UserInfo::getAge, UserInfo::getUsername);

// 先传表的索引，再依次传递表字段
sql.group(
  ListUtil.of(0, 0, 0),
  UserInfo::getId, UserInfo::getAge, UserInfo::getUsername
);
// having
sql.having(UserInfo::getUsername, "='张三'");
```

#### sort

```java
// true 正序, false 倒序
sql.order(UserInfo::getAge, true);
sql.order(UserInfo::getAge, 0, true);
```

#### function 待持续完善

```java
// select count(a.id) from user_info a;
sql.selectCount(UserInfo::getId).from(UserInfo.class);
sql.selectCount(UserInfo::getId, 0).from(UserInfo.class);

// select sum(a.id) from user_info a;
sql.selectSum(UserInfo::getId).from(UserInfo.class);
sql.selectSum(UserInfo::getId, 0).from(UserInfo.class);
```

### CusBaseService 的能力

>  [CusBaseService.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/CusBaseService.java) 

#### 新增

```sh
save;
saveBatch;
```

#### 修改

```java
updateById;
updateByIdBatch;
```



#### 新增或修改

```java
saveOrUpdate;
saveOrUpdateBatch;
```

#### 删除

```java
remove;
removeById;
removeByIds;
```



#### 查询

> 特别说明，分页返回结果为  [HttpPagedInfo.java](common/src/main/java/icu/helltab/itool/common/http/HttpPagedInfo.java) 
>
> 包含 count 和 list 两个值

```java
// 查询单个
getOne(sql->sql.select(UserInfo::getUsername));
// 查询数量
getCount(sql->sql.eq(UserInfo::getUsername, "张三"));
// 查询是否存在
has(sql->sql.eq(UserInfo::getUsername, "张三"));
// 查询列表
list(sql->sql.eq(UserInfo::getUsername, "张三"));
// 查询分页, 默认接口传参 params: pageNum: 当前页数， pageSize: 分页大小（最大为 100）
page(sql->sql.eq(UserInfo::getUsername, "张三"));


// 多表联查能力, 需要指定 from 和返回值接收对象
exOne(sql->sql.select(UserInfo::getUsername)
      .from(UserInfo.class)
 , UserInfoVo.class);
// 列表
exList(sql->sql.select(UserInfo::getUsername)
      .from(UserInfo.class)
 , UserInfoVo.class);
// 分页
exPage(sql->sql.select(UserInfo::getUsername)
      .from(UserInfo.class)
 , UserInfoVo.class);

```



