# mul-table-query

![](https://img.fpstore.shop/public/img.png)

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
     security:
       gateway-auth: true
       gateway-open: 
     jdbc:
       datasource:
         filters: stat,wall
         customConfig: com.comac.fpi.pure.common.web.config.CustomJdbcConfig
         connections:
           mysql:
             validationQuery: SELECT 1
             scanPackages: com.comac.fpi.pure.module.system.web.mapper
             driver-class-name: com.mysql.cj.jdbc.Driver
             url: jdbc:mysql://172.16.40.153:3306/fpi-pure?allowMultiQueries=true&createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true
             username: {pldd}
             password: {password}
   ```

3. 使用数据源和设置事务管理，默认数据源只需要继承  [DefaultBaseService.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/multi/DefaultBaseService.java) ，事务管理默认也是添加好的

   ```java
   @Service
   public class UserService extends DefaultBaseService<UserMapper, UserInfo> {
   
   }
   List<UserInfo> objects = userService.exList(sql -> {
       sql.select(UserInfo::getUsername)
         .from(UserInfo.class)
       ;
     }, UserInfo.class);
   ```

4. 自定义数据源配置修改钩子

   1. 在配置中指定钩子处理类

   ```yaml
   system:
     jdbc:
       datasource:
       # 配置钩子处理类
         customConfig: com.comac.fpi.pure.common.web.config.CustomJdbcConfig
   ```

   2. 编写钩子处理类, 下面这个例子中，我们修改了数据配置中的元数据注入策略

      1. 继承 [MultiDatasourceProperties.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/multi/MultiDatasourceProperties.java) ，这里主要调用 register 将钩子注册到数据源初始化的生命周期里面去
      2. 通过 `MybatisConfiguration configuration = factory.getConfiguration();` 获取到配置，可以做自定义配置

      ```java
      package com.comac.fpi.pure.common.web.config;
      
      import cn.hutool.core.date.LocalDateTimeUtil;
      import com.baomidou.mybatisplus.core.MybatisConfiguration;
      import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
      import com.comac.fpi.pure.common.web.config.security.HttpContextHolder;
      import com.comac.itool.multablequery.config.db.handler.MyMetaObjectHandler;
      import com.comac.itool.multablequery.config.db.multi.MultiDatasourceProperties;
      import org.apache.ibatis.reflection.MetaObject;
      import org.springframework.context.annotation.Configuration;
      import org.springframework.stereotype.Component;
      
      import javax.annotation.Resource;
      import javax.servlet.http.HttpServletRequest;
      
      /**
       * @author Helltab
       * @mail helltab@163.com
       * @date 2023/4/18 13:45
       * @desc 这是数据源自定义配置的钩子, 在这里可以更改 mybatis 的 MybatisSqlSessionFactoryBean 配置
       * @see
       */
      public class CustomJdbcConfig extends MultiDatasourceProperties {
          static  {
              // 注册数据源对应的回调函数, 可以修改 MybatisSqlSessionFactoryBean 的属性
              register("mysql", factory->{
                  MybatisConfiguration configuration = factory.getConfiguration();
                  GlobalConfigUtils.getGlobalConfig(configuration)
                          .setMetaObjectHandler(new CusMetaObjectHandler())
                          ;
              });
          }
      
          public static class CusMetaObjectHandler extends MyMetaObjectHandler {
              @Override
              public void insertFill(MetaObject metaObject) {
                  this.setFieldValByName("createBy", HttpContextHolder.getUserNo(), metaObject);
                  this.setFieldValByName("updateBy", HttpContextHolder.getUserNo(), metaObject);
                  super.insertFill(metaObject);
              }
      
      
              @Override
              public void updateFill(MetaObject metaObject) {
                  this.setFieldValByName("updateBy", HttpContextHolder.getUserNo(), metaObject);
                  super.updateFill(metaObject);
              }
          }
      }
      
      
      ```



5. 新增数据源:

   1. 在配置文件中添加数据源，参见 [ScanConfig.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/multi/ScanConfig.java)

   ```yaml
     jdbc:
       datasource:
         filters: stat,wall
         customConfig: com.comac.fpi.pure.common.web.config.CustomJdbcConfig
         connections:
           other_db: # 这里添加新数据源
             validationQuery: SELECT 1
             scanPackages: com.comac.fpi.pure.module.system.web.mapper
             driver-class-name: com.mysql.cj.jdbc.Driver
             url: jdbc:mysql://172.16.40.153:3306/fpi-pure?allowMultiQueries=true&createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true
             username: {pldd}
             password: {password}
   ```

   2. 添加数据源操作基类，参考 [DefaultBaseService.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/multi/DefaultBaseService.java) , 该数据源的操作都需要集成这个类

      ```java
      package com.comac.itool.multablequery.config.db.multi;
      
      import javax.annotation.Resource;
      
      import com.comac.itool.multablequery.config.db.CusBaseService;
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
      
      ```



## 多表联查工具的使用

### Startup

1. 配置完数据源之后，即可使用多表联查了，当然也可以直接使用 MySqlRunner 中的方法，为了统一，推荐继承相关数据源的 Service，使用提供的查询能力。

   1. 继承相关数据源 Service

      ```java
      @Service
      public class UserService extends DefaultBaseService<UserMapper, UserInfo> {
      
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
2. select(UserInfo::getUsername, q.Alias(1));
// 多个字段查询
1. select(UserInfo::getUsername, UserInfo::getPasswrod);//类型检测预警
2. select(UserInfo::getUsername)
  .select(UserInfo::getPassword);
3. select(UserInfo::getUsrname, q.Alias(1))
  .select(UserInfo::getPasswrod,q.Alias(2));
//子查询
1. sql.select(inner->{
      inner
        .selectCount(UserInfo::getId, q.Alias(1))
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
// left, 第二个参数使用 lambda 来做条件设置
sql.leftJoin(RoleInfo.class, j -> {
                        j.eq(RoleInfo::getId, UserInfo::getRoleId);
                    })
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
sql.like(UserInfo::getUsername, "尚");
sql.notLike(UserInfo::getAge, "尚");
```

#### group

```java
sql.group(UserInfo::getId).group(UserInfo::getAge).group(UserInfo::getUsername);

// having
sql.having(UserInfo::getUsername, "='张三'");
```

#### sort

```java
// true 正序, false 倒序
sql.order(UserInfo::getAge, true);
sql.order(UserInfo::getAge, 0, true);
```

#### function

```java
// select count(a.id) from user_info a;
sql.selectCount(UserInfo::getId).from(UserInfo.class);
sql.selectCount(UserInfo::getId, 0).from(UserInfo.class);

// select sum(a.id) from user_info a;
sql.selectSum(UserInfo::getId).from(UserInfo.class);
sql.selectSum(UserInfo::getId, 0).from(UserInfo.class);


// 使用 concat({}, {}) 表达式来申明函数原型
// 使用 .bind(UserInfo::getAge).bind(UserInfo::getUsername) 来绑定参数
sql.eq(false, sql.fun("concat({}, {})").bind(UserInfo::getAge)
                            .bind(UserInfo::getUsername), "23")
```

### CusBaseService 的能力

>   [CusBaseService.java](mul-table-query/src/main/java/icu/helltab/itool/multablequery/config/db/CusBaseService.java)

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



