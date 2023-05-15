package icu.helltab.itool.multablequery.config.db.query.lambda;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.*;
import cn.hutool.extra.template.engine.freemarker.FreemarkerEngine;
import com.baomidou.mybatisplus.annotation.TableLogic;
import icu.helltab.itool.multablequery.config.db.query.SqlBuilderUtil;
import icu.helltab.itool.multablequery.config.db.query.lambda.alias.SubQueryAlias;
import icu.helltab.itool.multablequery.config.db.query.SqlBuilder;
import icu.helltab.itool.multablequery.config.db.query.SqlKeywords;
import icu.helltab.itool.multablequery.config.db.query.lambda.alias.BaseAlias;
import icu.helltab.itool.multablequery.config.db.query.lambda.alias.SerialAlias;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:58
 * @desc lambda 的方式进行多表联查
 * todo 添加语法兼容
 * 关键点:
 * 1.select 字段需要懒加载, 可以在 build 阶段生成
 * 2.同一个表多次出现, 需要能自动标识
 * 		2.1 考虑大多数情况表不会出现第二次, 因此默认的条件是在表只出现一次的情况下
 * 		2.2 一旦表重复出现, 就需要使用别名来进行标识, 用户手动标识使用哪一张表
 * 		2.4 使用数据结构 {talbeName: [alias0, alias1, alias2...]} 保存别名信息
 * 3.子查询
 * 		3.1 子查询需要考虑别名的计数, 为了保证别名全局唯一, 别名是全局统一计数的.
 * 		3.2 子查询不能和主查询规则相同, 不然在生成 select * 的时候会有问题
 * 		3.3 主查询: A B C 子查询: D_D E_E 子查询结果 INNER_F
 * 3.4 主查询默认添加 SELECT INNER_F.*
 * 4.解析 lambda 表达式的类和字段, 包括缓存问题
 * 4.1 现有方案: hutool 工具包中的 lambdaUtil 可以解决这个问题
 * @see
 */
@Data
@Slf4j
@SuppressWarnings({"cast", "rawtypes", "unused", "unchecked"})
public class SqlLambdaBuilder extends SqlBuilder {



    /**
     * 函数专用
     * 如: count(A.name)
     */
    private final static String FUN_FIELD = "{}(${{}@{}}.`{}`)";

    /**
     * 查询中添加函数:专用
     * 如 count(A.name) total
     */
    private final static String FUN_FIELD_AS = FUN_FIELD + " {}";

    /**
     * 如 count(select 1) num
     * 子查询是懒属性
     */
    private final static String FUN_FIELD_AS_RAW = "{}(${{}}){}";


    // 表名和字段的占位符
    private final static String TABLE_FIELD = "${{}@{}}.`{}` ";

    private static String TABLE_FIELD(String tableName, BaseAlias alias, String filedName) {
        if (alias instanceof SubQueryAlias) {
            return StrUtil.format("{}.`{}` ", alias, filedName);
        }
        return StrUtil.format(TABLE_FIELD, tableName, alias, filedName);
    }

    // 字段别名专用 a.name a_name
    private final static String SELECT_AS = TABLE_FIELD + " {}";

    private String WHERE_CONDITION(
            String tableName, BaseAlias alias, String filedName,
            Object opt,
            String tableName2, BaseAlias alias2, String filedName2

    ) {
        return TABLE_FIELD(tableName, alias, filedName) + opt + TABLE_FIELD(tableName2, alias2, filedName2);
    }

    private String WHERE_CONDITION_RAW(String tableName, BaseAlias alias, String filedName) {

        return TABLE_FIELD(tableName, alias, filedName) + " {} '{}'";
    }

    private String WHERE_CONDITION_RAW2(String tableName, BaseAlias alias, String filedName) {
        return TABLE_FIELD(tableName, alias, filedName) + " {} {}";
    }

    private String WHERE_CONDITION_RAW3(String tableName, BaseAlias alias, String filedName) {
        return TABLE_FIELD(tableName, alias, filedName) + " {} ({})";
    }


    private Map<String, List<String>> aliasMap = new HashMap<>();
    private Map<String, SqlLambdaBuilder> innerMap = new HashMap<>();

    private List<String> selectFieldList;
    private List<String> orderList = new ArrayList<>();

    private String prefix = "";

    private AtomicInteger tableIdx;

    /**
     * 构建 lambda sql 构造器
     *
     * @param consumer
     * @return return
     */
    public static String lambda(Consumer<SqlLambdaBuilder> consumer) {

        SqlLambdaBuilder sqlLambdaBuilder = new SqlLambdaBuilder();
        consumer.accept(sqlLambdaBuilder);

        return sqlLambdaBuilder.build();
    }

    private SqlLambdaBuilder() {
        selectFieldList = new ArrayList<>();
        tableIdx = new AtomicInteger(0);
    }

    private SqlLambdaBuilder(Map<String, List<String>> aliasMap, AtomicInteger tableIdx) {
        this.aliasMap = aliasMap;
        this.tableIdx = new AtomicInteger(0);
        this.prefix = getTableAlias(tableIdx.addAndGet(1), false) + "_";
        selectFieldList = new ArrayList<>();
    }

    /**
     * 复制一个 builder
     * 用于子查询继承缓存和别名
     *
     * @param sqlLambdaBuilder
     * @return return
     */
    public static SqlLambdaBuilder copy(SqlLambdaBuilder sqlLambdaBuilder) {
        return new SqlLambdaBuilder(
                sqlLambdaBuilder.aliasMap,
                sqlLambdaBuilder.tableIdx
        );
    }

    /**
     * 第一次出现的表
     * 如果一个表会出现两次以上, 需要使用重载方法, 指定第几个表
     * 如 select a.reg_name ua, b.reg_name ub from region a, region b
     * on a.parent_id = b.id
     *
     * @param funcs
     * @param <P>   generic P
     * @return return
     */
    @SafeVarargs
    public final <P> SqlLambdaBuilder select(Func1<P, ?>... funcs) {
        for (Func1<P, ?> func : funcs) {
            select(func, ALIAS(0));
        }
        return this;
    }

    /**
     * 纯文本, 适用于 select 1 idx 这样的情况
     *
     * @param selects
     * @param <P>     generic P
     * @return return
     */
    public <P> SqlLambdaBuilder selectRaw(String... selects) {
        selectFieldList.addAll(Arrays.asList(selects));
        return this;
    }

    public <P> SqlLambdaBuilder selectFun(SqlLambdaFun fun, Func1<P, ?> as) {
        selectFieldList.add(fun.as(as));
        return this;
    }

    public <P> SqlLambdaBuilder select(Func1<P, ?> func) {
        return select(func, ALIAS(0));
    }

    public <P> SqlLambdaBuilder select(Class<P> clazz) {
        return select(clazz, ALIAS(0));
    }

    public <P> SqlLambdaBuilder select(Class<P> clazz, BaseAlias idx) {
        selectFieldList.add(
                StrUtil.format(TABLE_FIELD(clazz.getSimpleName(), idx, "*"))
        );
        return this;
    }

    public <P> SqlLambdaBuilder select(Func1<P, ?> func, BaseAlias idx) {
        return select(func, idx, "");
    }

    /**
     * select 表名采用后生成方式, 先用占位符代替, 等到 build 的时候,
     * 根据表名和表在本次查询中的重复顺序 (从 0 开始) 用别名来替换占位符
     * 如 select ${region@0.reg_name}, ${region@1.reg_name}  ub from region a, region b
     * on a.parent_id = b.id
     * => 缓存: {region: ['a', 'b']}
     * <p>
     * => 组合: ${region@0.reg_name} > region@0:a region@1:b
     * => select a.reg_name, b.reg_name  from region a, region b
     * on a.parent_id = b.id
     *
     * @param func 需查询的字段
     * @param idx  表的重复顺序
     * @param <P>  generic P
     * @return return
     */
    public <P> SqlLambdaBuilder select(Func1<P, ?> func, BaseAlias idx, String as) {
        Class<P> realClass = LambdaUtil.getRealClass(func);
        selectFieldList.add(
                StrUtil.format(SELECT_AS, realClass.getSimpleName(), idx, SqlBuilderUtil.resolveFieldName(func), as)
        );
        return this;
    }

    public <P, Q> SqlLambdaBuilder selectCount(Func1<P, Q> func, BaseAlias idx) {
        Class<P> realClass = LambdaUtil.getRealClass(func);
        selectFieldList.add(
                StrUtil.format(FUN_FIELD, SqlKeywords.FUN_COUNT, realClass.getSimpleName(), idx, SqlBuilderUtil.resolveFieldName(func))
        );
        return this;
    }

    public <P, Q> SqlLambdaBuilder selectCount(Func1<P, Q> func) {

        return selectCount(func, ALIAS(0));
    }

    public <P, Q, A, B> SqlLambdaBuilder selectSum(Func1<P, Q> func, BaseAlias idx) {
        selectFieldList.add(
                StrUtil.format(FUN_FIELD, SqlKeywords.FUN_SUM, LambdaUtil.getRealClass(func).getSimpleName(), idx, SqlBuilderUtil.resolveFieldName(func))
        );
        return this;
    }

    public <P, Q> SqlLambdaBuilder selectSum(Func1<P, Q> func) {

        return selectSum(func, ALIAS(0));
    }

    public <A> SqlLambdaBuilder select(Consumer<SqlLambdaBuilder> innerConsumer, Func1<A, ?> as) {
        SqlLambdaBuilder copy = SqlLambdaBuilder.copy(this);
        innerConsumer.accept(copy);
        String inner_key = "ik_" + IdUtil.fastSimpleUUID();
        innerMap.put(inner_key, copy);
        selectFieldList.add(
                StrUtil.format("(${{}}) {}", inner_key, SqlBuilderUtil.resolveFieldName(as))
        );
        return this;
    }

    public <A> SqlLambdaBuilder selectAll(Class<A> clazz) {
        selectFieldList.add(
                StrUtil.format(TABLE_FIELD(clazz.getSimpleName(), ALIAS(0), "*"))
        );
        return this;
    }


    public SqlLambdaBuilder from(Class<?>... tables) {
        if (!isRawCondition) {
            from(getTableList(tables));
        }
        setLogic(tables);
        return this;
    }

    /**
     * 添加逻辑删除
     */
    private void setLogic(Class<?>... classes) {
        for (Class<?> table : classes) {
            for (Field field : ReflectUtil.getFields(table)) {
                if (field.isAnnotationPresent(TableLogic.class)) {
                    Optional.ofNullable(this.logic.put(new LogicObj(table, field), new AtomicInteger(0)))
                            .ifPresent(x->x.addAndGet(1));
                    break;
                }
            }
        }
    }

    public SubQueryAlias SUB_ALIAS(int idx) {
        return new SubQueryAlias(idx);
    }

    public SerialAlias ALIAS(int idx) {
        return new SerialAlias(idx);
    }

    public SqlLambdaBuilder from(Consumer<SqlLambdaBuilder> consumer, BaseAlias idx) {
        SqlLambdaBuilder lambdaBuilder = copy(this);
        consumer.accept(lambdaBuilder);
        String innerAlias = idx.get();
        from(", ( " + lambdaBuilder.build() + " ) " + innerAlias);
        selectFieldList.add(innerAlias + ".* ");
        return this;
    }

    private String[] getTableList(Class... tables) {
        return getTableList(Arrays.asList(tables));
    }

    private String[] getTableList(List<Class> tables) {
        List<String> list = new ArrayList<>();
        for (Class<?> table : tables) {
            String tableName = SqlBuilderUtil.resolveTableName(table);
            String tableAlias = getTableAlias(tableIdx.getAndAdd(1), false);
            String s = tableName + " " + tableAlias;
            list.add(s);
            List<String> aliasList = aliasMap.computeIfAbsent(table.getSimpleName(), (k) -> new ArrayList<>());
            aliasList.add(tableAlias);
        }

        return ArrayUtil.toArray(list, String.class);
    }

    /**
     * 根据表的出场顺序定制别名
     * A-Z 使用完后使用 AA-ZZ
     * 子查询中的别名会加上一层如 B_A
     * 子查询结果别名会加上 INNER_: 如 INNER_B_A
     *
     * @param idx
     * @return return
     */
    private String getTableAlias(Integer idx, boolean inner) {
        char c = (char) ('A' + idx);
        String inn = "";
        if (inner) inn = "INNER_";
        if (c > 'Z') {
            char s = (char) (c - 25);
            return inn + prefix + s + s;
        }
        return inn + prefix + c;
    }

    /**
     * join
     *
     * @param join         需连接的目标
     * @param joinKeywords 连接方式
     * @param consumer     连接条件
     * @param <P>          generic P
     * @return return
     */
    public <P, A, B, C> SqlLambdaBuilder join(Class<P> join, SqlKeywords joinKeywords, Consumer<SqlLambdaBuilder> consumer) {
        setLogic(join);
        SqlLambdaBuilder copy = copy(this);
        copy.setRawCondition(true);
        consumer.accept(copy);
        join(getTableList(join)[0], joinKeywords)
                .on(copy.build());
        return this;
    }

    public <P, A, B, C> SqlLambdaBuilder join(Class<P> join, Consumer<SqlLambdaBuilder> consumer) {
        return join(join, SqlKeywords.JOIN, consumer);
    }

    public <P, A, B, C> SqlLambdaBuilder leftJoin(Class<P> join,
                                                  Consumer<SqlLambdaBuilder> consumer
    ) {
        return join(join, SqlKeywords.LEFT_JOIN, consumer);
    }

    public <P, A, B, C> SqlLambdaBuilder rightJoin(Class<P> join, Consumer<SqlLambdaBuilder> consumer) {
        return join(join, SqlKeywords.RIGHT_JOIN, consumer);
    }

    public <P, A, B, C> SqlLambdaBuilder fullJoin(Class<P> join, Consumer<SqlLambdaBuilder> consumer) {
        return join(join, SqlKeywords.FULL_JOIN, consumer);
    }


    /**
     * 子查询
     *
     * @return return
     */
    public <P, A> SqlLambdaBuilder subQuery(Func1<P, A> func, BaseAlias sIdx, SqlKeywords opt, Consumer<SqlLambdaBuilder> consumer) {
        Class realClass = LambdaUtil.getRealClass(func);

        SqlLambdaBuilder lambdaBuilder = copy(this);
        consumer.accept(lambdaBuilder);
        String condition = StrUtil.format(
                WHERE_CONDITION_RAW3(realClass.getSimpleName(), sIdx, SqlBuilderUtil.resolveFieldName(func)),
                opt,
                lambdaBuilder.build()
        );
        this.where(condition);
        return this;
    }

    public <P, A> SqlLambdaBuilder condition(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx,
                                             Object value,
                                             SqlKeywords opt,
                                             boolean notFlag
    ) {
        if (ObjectUtil.isEmpty(value)) {
            if (nullJudge) {
                return this;
            } else if (null == value) {
                Class<P> realClass = LambdaUtil.getRealClass(source);
                this.where(StrUtil.format(
                        WHERE_CONDITION_RAW2(realClass.getSimpleName(), sIdx, SqlBuilderUtil.resolveFieldName(source)),
                        notFlag ? SqlKeywords.IS_NOT : SqlKeywords.IS,
                        SqlKeywords.NVL
                ));
                return this;
            }
        }
        String fieldName = SqlBuilderUtil.resolveFieldName(source);
        Class<P> realClass = LambdaUtil.getRealClass(source);
        if (SqlKeywords.IN == opt || SqlKeywords.NOT_IN == opt) {
            String inCondition = Arrays.stream((Object[]) value).map(x -> "'" + x + "'")
                    .collect(Collectors.joining(","));

            this.where(StrUtil.format(
                    WHERE_CONDITION_RAW2(realClass.getSimpleName(), sIdx, fieldName),
                    opt,
                    "(" + inCondition + ")"
            ));
            return this;
        }
        String condition = StrUtil.format(
                WHERE_CONDITION_RAW(realClass.getSimpleName(), sIdx, fieldName),
                opt,
                value
        );
        this.where(condition);
        return this;
    }

    public <P, A> SqlLambdaBuilder condition(boolean nullJudge, SqlLambdaFun fun,
                                             Object value,
                                             SqlKeywords opt,
                                             boolean notFlag
    ) {

        if (ObjectUtil.isEmpty(value)) {
            if (nullJudge) {
                return this;
            } else if (null == value) {
                this.where(StrUtil.format(
                        fun.condition(),
                        notFlag ? SqlKeywords.IS_NOT : SqlKeywords.IS,
                        SqlKeywords.NVL
                ));
                return this;
            }
        }
        if (SqlKeywords.IN == opt) {
            String inCondition = Arrays.stream((Object[]) value).map(x -> "'" + x + "'")
                    .collect(Collectors.joining(","));

            this.where(StrUtil.format(
                    fun.condition(),
                    SqlKeywords.IN,
                    "(" + inCondition + ")"
            ));
            return this;
        }
        String condition = StrUtil.format(
                fun.condition(),
                opt,
                value
        );
        this.where(condition);
        return this;
    }

    public <P, A, B, C> SqlLambdaBuilder condition2(Func1<P, A> a, Func1<B, C> b,
                                                    BaseAlias aIdx, BaseAlias bIdx,
                                                    SqlKeywords opt
    ) {
        Class<P> aClass = LambdaUtil.getRealClass(a);
        Class<B> bClass = LambdaUtil.getRealClass(b);
        String optResult = StrUtil.format(WHERE_CONDITION(
                        aClass.getSimpleName(), aIdx, SqlBuilderUtil.resolveFieldName(a),
                        opt,
                        bClass.getSimpleName(), bIdx, SqlBuilderUtil.resolveFieldName(b)
                )
        );
        this.where(optResult);
        return this;
    }

    public <P, A> SqlLambdaBuilder eqNull(Func1<P, A> source) {
        return eq(false, source, ALIAS(0), null);
    }

    public <P, A> SqlLambdaBuilder eqNull(SqlLambdaFun fun) {
        return eq(false, fun, null);
    }

    public <P, A> SqlLambdaBuilder like(SqlLambdaFun fun, Object value) {
        return like(false, true, fun, value);
    }

    public <P, A> SqlLambdaBuilder like(boolean left, boolean nullJudge, SqlLambdaFun fun, Object value) {
        String str;
        if (null != value && value instanceof SqlLambdaFun) {
            str = ((SqlLambdaFun) value).end();
        } else {
            str = ObjectUtil.isEmpty(value) ? null : (left ? "" : "%") + String.valueOf(value).replace("*", "%") + "%";
        }
        return condition(nullJudge, fun, str, SqlKeywords.LIKE, false);
    }

    public <P, A> SqlLambdaBuilder in(SqlLambdaFun fun, Object value) {
        return in(true, fun, value);
    }

    public <P, A> SqlLambdaBuilder in(boolean nullJudge, SqlLambdaFun fun, Object value) {
        return condition(nullJudge, fun, value, SqlKeywords.IN, false);
    }

    public <P, A> SqlLambdaBuilder gt(SqlLambdaFun fun, Object value) {
        return gt(true, fun, value);
    }

    public <P, A> SqlLambdaBuilder gt(boolean nullJudge, SqlLambdaFun fun, Object value) {
        return condition(nullJudge, fun, value, SqlKeywords.GE, false);
    }

    public <P, A> SqlLambdaBuilder lt(SqlLambdaFun fun, Object value) {
        return lt(true, fun, value);
    }

    public <P, A> SqlLambdaBuilder lt(boolean nullJudge, SqlLambdaFun fun, Object value) {
        return condition(nullJudge, fun, value, SqlKeywords.GE, false);
    }

    public <P, A> SqlLambdaBuilder ge(SqlLambdaFun fun, Object value) {
        return ge(true, fun, value);
    }

    public <P, A> SqlLambdaBuilder ge(boolean nullJudge, SqlLambdaFun fun, Object value) {
        return condition(nullJudge, fun, value, SqlKeywords.GE, false);
    }

    public <P, A> SqlLambdaBuilder le(SqlLambdaFun fun, Object value) {
        return le(true, fun, value);
    }

    public <P, A> SqlLambdaBuilder le(boolean nullJudge, SqlLambdaFun fun, Object value) {
        return condition(nullJudge, fun, value, SqlKeywords.LE, false);
    }

    public <P, A> SqlLambdaBuilder eq(SqlLambdaFun fun, Object value) {
        return eq(true, fun, value);
    }

    public <P, A> SqlLambdaBuilder eq(boolean nullJudge, SqlLambdaFun fun, Object value) {
        return condition(nullJudge, fun, value, SqlKeywords.EQ, false);
    }

    public <P, A> SqlLambdaBuilder neq(SqlLambdaFun fun, Object value) {
        return neq(true, fun, null);
    }

    public <P, A> SqlLambdaBuilder neq(boolean nullJudge, SqlLambdaFun fun, Object value) {
        return condition(nullJudge, fun, value, SqlKeywords.NE, true);
    }

    public <P, A> SqlLambdaBuilder eq(Func1<P, A> source, Object value) {
        return eq(source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder eq(Func1<P, A> source, BaseAlias sIdx, Object value) {
        return eq(true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder eq(boolean judge, Func1<P, A> source, Object value) {
        return eq(judge, source, ALIAS(0), value);
    }

    /**
     * @param nullJudge 是否需要判断空值, 默认为 true, 即遇到值为空不添加这个判断条件 是否检查空值
     *                  true: 如果值为空, 则不添加条件
     *                  false: 不论值是否为空, 都不添加条件
     * @param source    源字段
     * @param sIdx      表索引, 从 0 开始
     * @param value
     * @param <P>       generic P
     * @param <A>       generic A
     * @return return
     */
    public <P, A> SqlLambdaBuilder eq(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object value) {

        return condition(
                nullJudge,
                source,
                sIdx,
                value,
                SqlKeywords.EQ,
                false
        );
    }


    public <P, A, B, C> SqlLambdaBuilder eq(Func1<P, A> a, Func1<B, C> b) {
        return eq(a, b, new SerialAlias(0), new SerialAlias(0));
    }

    public <P, A, B, C> SqlLambdaBuilder eq(Func1<P, A> a, Func1<B, C> b, BaseAlias aIdx, BaseAlias bIdx) {
        return condition2(a, b, aIdx, bIdx, SqlKeywords.EQ);
    }

    public <P, A> SqlLambdaBuilder eq(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return eq(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder eq(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.EQ, consumer);
    }

    public <P, A> SqlLambdaBuilder neq(Func1<P, A> source, Object value) {
        return neq(true, source, value);
    }

    public <P, A> SqlLambdaBuilder neq(Func1<P, A> source, BaseAlias sIdx, Object value) {
        return neq(true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder neq(boolean nullJudge, Func1<P, A> source, Object value) {
        return neq(nullJudge, source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder neq(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                value,
                SqlKeywords.NE,
                true
        );
    }

    public <P, A, B, C> SqlLambdaBuilder neq(Func1<P, A> a, Func1<B, C> b) {
        return neq(a, b, ALIAS(0), ALIAS(0));
    }

    public <P, A, B, C> SqlLambdaBuilder neq(Func1<P, A> a, Func1<B, C> b, BaseAlias aIdx, BaseAlias bIdx) {
        return condition2(a, b, aIdx, bIdx, SqlKeywords.NE);
    }

    public <P, A> SqlLambdaBuilder neq(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return neq(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder neq(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.NE, consumer);
    }

    public <P, A> SqlLambdaBuilder ge(Func1<P, A> source, Object value) {
        return ge(true, source, value);
    }

    public <P, A> SqlLambdaBuilder ge(Func1<P, A> source, BaseAlias sIdx, Object value) {
        return ge(true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder ge(boolean nullJudge, Func1<P, A> source, Object value) {
        return ge(nullJudge, source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder ge(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                value,
                SqlKeywords.GE,
                true
        );
    }


    public <P, A, B, C> SqlLambdaBuilder ge(Func1<P, A> a, Func1<B, C> b) {
        return ge(a, b, ALIAS(0), ALIAS(0));
    }

    public <P, A, B, C> SqlLambdaBuilder ge(Func1<P, A> a, Func1<B, C> b, BaseAlias aIdx, BaseAlias bIdx) {
        return condition2(a, b, aIdx, bIdx, SqlKeywords.GE);
    }

    public <P, A> SqlLambdaBuilder ge(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return ge(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder ge(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.GE, consumer);
    }

    public <P, A> SqlLambdaBuilder gt(Func1<P, A> source, Object value) {
        return gt(true, source, value);
    }

    public <P, A> SqlLambdaBuilder gt(Func1<P, A> source, BaseAlias sIdx, Object value) {
        return gt(true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder gt(boolean nullJudge, Func1<P, A> source, Object value) {
        return gt(nullJudge, source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder gt(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                value,
                SqlKeywords.GT,
                false
        );
    }


    public <P, A, B, C> SqlLambdaBuilder gt(Func1<P, A> a, Func1<B, C> b) {
        return gt(a, b, ALIAS(0), ALIAS(0));
    }

    public <P, A, B, C> SqlLambdaBuilder gt(Func1<P, A> a, Func1<B, C> b, BaseAlias aIdx, BaseAlias bIdx) {
        return condition2(a, b, aIdx, bIdx, SqlKeywords.GT);
    }

    public <P, A> SqlLambdaBuilder gt(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return gt(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder gt(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.GT, consumer);
    }

    public <P, A> SqlLambdaBuilder le(Func1<P, A> source, Object value) {
        return le(true, source, value);
    }

    public <P, A> SqlLambdaBuilder le(Func1<P, A> source, BaseAlias sIdx, Object value) {
        return le(true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder le(boolean nullJudge, Func1<P, A> source, Object value) {
        return le(nullJudge, source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder le(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                value,
                SqlKeywords.LE,
                true
        );
    }


    public <P, A, B, C> SqlLambdaBuilder le(Func1<P, A> a, Func1<B, C> b) {
        return le(a, b, ALIAS(0), ALIAS(0));
    }

    public <P, A, B, C> SqlLambdaBuilder le(Func1<P, A> a, Func1<B, C> b, BaseAlias aIdx, BaseAlias bIdx) {
        return condition2(a, b, aIdx, bIdx, SqlKeywords.LE);
    }

    public <P, A> SqlLambdaBuilder le(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return le(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder le(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.LE, consumer);
    }

    public <P, A> SqlLambdaBuilder lt(Func1<P, A> source, Object value) {
        return lt(true, source, value);
    }

    public <P, A> SqlLambdaBuilder lt(Func1<P, A> source, BaseAlias sIdx, Object value) {
        return lt(true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder lt(boolean nullJudge, Func1<P, A> source, Object value) {
        return lt(nullJudge, source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder lt(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                value,
                SqlKeywords.LT,
                false
        );
    }


    public <P, A, B, C> SqlLambdaBuilder lt(Func1<P, A> a, Func1<B, C> b) {
        return lt(a, b, ALIAS(0), ALIAS(0));
    }

    public <P, A, B, C> SqlLambdaBuilder lt(Func1<P, A> a, Func1<B, C> b, BaseAlias aIdx, BaseAlias bIdx) {
        return condition2(a, b, aIdx, bIdx, SqlKeywords.LT);
    }

    public <P, A> SqlLambdaBuilder lt(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return lt(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder lt(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.LT, consumer);
    }

    public <P> SqlLambdaBuilder in(Func1<P, ?> source, Object... value) {
        return in(true, source, value);
    }

    public <P> SqlLambdaBuilder in(Func1<P, ?> source, BaseAlias sIdx, Object... value) {
        return in(true, source, sIdx, value);
    }

    public <P> SqlLambdaBuilder in(boolean nullJudge, Func1<P, ?> source, Object... value) {
        return in(nullJudge, source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder in(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object... value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                value,
                SqlKeywords.IN,
                true
        );
    }

    public <P> SqlLambdaBuilder in(Func1<P, ?> func, Consumer<SqlLambdaBuilder> consumer) {
        return in(func, ALIAS(0), consumer);
    }

    public <P> SqlLambdaBuilder in(Func1<P, ?> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.IN, consumer);
    }

    public <P> SqlLambdaBuilder notIn(Func1<P, ?> source, Object... value) {
        return notIn(true, source, value);
    }

    public <P, A> SqlLambdaBuilder notIn(Func1<P, A> source, BaseAlias sIdx, Object... value) {
        return notIn(true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder notIn(boolean nullJudge, Func1<P, A> source, Object... value) {
        return notIn(nullJudge, source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder notIn(boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object... value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                value,
                SqlKeywords.NOT_IN,
                false
        );
    }

    public <P, A> SqlLambdaBuilder notIn(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return notIn(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder notIn(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.NOT_IN, consumer);
    }

    public <P, A> SqlLambdaBuilder like(Func1<P, A> source, Object value) {
        return like(true, source, value);
    }

    public <P, A> SqlLambdaBuilder like(Func1<P, A> source, BaseAlias sIdx, Object value) {
        return like(false, true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder like(boolean nullJudge, Func1<P, A> source, Object value) {
        return like(false, nullJudge, source, ALIAS(0), value);
    }

    public <P, A> SqlLambdaBuilder like(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return like(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder like(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.LIKE, consumer);
    }


    /**
     * @param nullJudge 是否需要判断空值, 默认为 true, 即遇到值为空不添加这个判断条件
     * @param source    源字段
     * @param sIdx      表索引, 从 0 开始
     * @param value     手动添加 * 代表模糊位置
     * @param <P>       generic P
     * @param <A>       generic A
     * @return return
     */
    public <P, A> SqlLambdaBuilder like(boolean left, boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                ObjectUtil.isEmpty(value) ? null : (left ? "" : "%") + String.valueOf(value).replace("*", "%") + "%",
                SqlKeywords.LIKE,
                true
        );
    }

    public <P, A> SqlLambdaBuilder notLike(Func1<P, A> source, Object... value) {
        return notLike(true, source, value);
    }

    public <P, A> SqlLambdaBuilder notLike(Func1<P, A> source, BaseAlias sIdx, Object... value) {
        return notLike(true, source, sIdx, value);
    }

    public <P, A> SqlLambdaBuilder notLike(boolean nullJudge, Func1<P, A> source, Object... value) {
        return notLike(false, nullJudge, source, ALIAS(0), value);
    }

    /**
     * @param nullJudge 是否需要判断空值, 默认为 true, 即遇到值为空不添加这个判断条件
     * @param source    源字段
     * @param sIdx      表索引, 从 0 开始
     * @param value     手动添加 * 代表模糊位置
     * @param <P>       generic P
     * @param <A>       generic A
     * @return return
     */
    public <P, A> SqlLambdaBuilder notLike(boolean left, boolean nullJudge, Func1<P, A> source, BaseAlias sIdx, Object... value) {
        return condition(
                nullJudge,
                source,
                sIdx,
                ObjectUtil.isEmpty(value) ? null : (left ? "" : "%") + String.valueOf(value).replace("*", "%") + "%",
                SqlKeywords.NOT_LIKE,
                false
        );
    }

    public <P, A> SqlLambdaBuilder notLike(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return notLike(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder notLike(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.NOT_LIKE, consumer);
    }

    public <P, A> SqlLambdaBuilder exists(Func1<P, A> func, Consumer<SqlLambdaBuilder> consumer) {
        return exists(func, ALIAS(0), consumer);
    }

    public <P, A> SqlLambdaBuilder exists(Func1<P, A> func, BaseAlias sIdx, Consumer<SqlLambdaBuilder> consumer) {
        return subQuery(func, sIdx, SqlKeywords.EXISTS, consumer);
    }

    public <P, A> SqlLambdaBuilder having(Func1<P, A> func, Object condition) {
        groupRawSql.append(SqlKeywords.HAVING).append("(")
                .append(condition)
                .append(")");
        return this;
    }

    public <P, A> SqlLambdaBuilder and(Consumer<SqlLambdaBuilder> consumer) {
        SqlLambdaBuilder sqlLambdaBuilder = copy(this);
        sqlLambdaBuilder.setRawCondition(true);
        consumer.accept(sqlLambdaBuilder);
        whereRawSql.append(SqlKeywords.AND).append("(")
                .append(sqlLambdaBuilder.build())
                .append(")");
        return this;
    }

    public <P, A> SqlLambdaBuilder or(Consumer<SqlLambdaBuilder> consumer) {
        SqlLambdaBuilder sqlLambdaBuilder = copy(this);
        sqlLambdaBuilder.setRawCondition(true);
        consumer.accept(sqlLambdaBuilder);
        whereRawSql.append(SqlKeywords.OR).append("(")
                .append(sqlLambdaBuilder.build())
                .append(")");
        return this;
    }

    public <P> SqlLambdaBuilder group(Func1<P, ?> func, BaseAlias alias) {
        Class realClass = LambdaUtil.getRealClass(func);
        group(StrUtil.format(TABLE_FIELD,
                realClass.getSimpleName(),
                alias.get(),
                SqlBuilderUtil.resolveFieldName(func)
        ));
        return this;
    }

    public <P> SqlLambdaBuilder group(Func1<P, ?> func) {
        return group(func, ALIAS(0));
    }


    /**
     * 默认全倒序
     *
     * @param func
     * @param idx
     * @return return SqlLambdaBuilder
     * @throws Exception
     */
    public <A, B> SqlLambdaBuilder order(Func1<A, B> func, BaseAlias idx, boolean direct) {
        this.orderList.add(StrUtil.format(SELECT_AS,
                LambdaUtil.getRealClass(func).getSimpleName(), idx,
                SqlBuilderUtil.resolveFieldName(func), direct ? SqlKeywords.ASC : SqlKeywords.DESC));
        return this;
    }

    public <A, B> SqlLambdaBuilder order(String sort, boolean direct) {
        this.orderList.add(sort + " " + (direct ? SqlKeywords.ASC : SqlKeywords.DESC));
        return this;
    }

    public <A, B> SqlLambdaBuilder order(Func1<A, B> func, boolean direct) {
        return order(func, ALIAS(0), direct);
    }

    @Override
    public String genLogic() {
        return logic.keySet().stream().map(logicObj -> SqlKeywords.AND + StrUtil.format(
                WHERE_CONDITION_RAW(logicObj.getRealClazz().getSimpleName(),
                        ALIAS(logic.get(logicObj).get()),
                        StrUtil.toUnderlineCase(logicObj.getField().getName())),
                SqlKeywords.EQ,
                "0"
        )).collect(Collectors.joining(" "));
    }

    public String build() {
        StringBuilder rawSql = this.getRawSql();
        Map<String, String> map = new HashMap<>();
        boolean empty = selectFieldList.isEmpty();


        aliasMap.forEach((k, v) -> {
            for (int i = 0; i < v.size(); i++) {
                String alias = v.get(i);
                map.put(k + "@" + ALIAS(i), alias);
                if (empty) {
                    if ((prefix.equals("") && alias.length() == 1)
                            || (!prefix.equals("") && alias.startsWith(prefix))
                    ) {
                        selectFieldList.add(alias + ".*");

                    }
                }
            }
            // 为 select(Class )这种情况预留
            v.add("*");
        });
        innerMap.forEach((k, v) -> {
            map.put(k, v.build());
        });

        String finalSql;
        if (isRawCondition) {
            finalSql = rawSql.toString();
        } else {
            finalSql = SqlKeywords.SELECT + CollUtil.join(selectFieldList, ",") + rawSql.toString();
        }
        finalSql = finalSql.replaceAll("`\\*`", "*");
        if (!orderList.isEmpty()) {
            finalSql += SqlKeywords.ORDER + " " + String.join(",", orderList);
        }
        FreemarkerEngine freemarkerEngine = new FreemarkerEngine();
        String render = freemarkerEngine.getTemplate(finalSql).render(map);
        return render.replaceAll("\\s+", " ");
    }


    /**
     * 函数
     *
     * @return
     */
    public SqlLambdaFun fun(String funStr) {
        return new SqlLambdaFun(funStr);
    }

    public class SqlLambdaFun {

        public SqlLambdaFun(String org) {
            this.org = org;
        }

        List<String> args = new ArrayList<>();
        String org;

        public <P> SqlLambdaFun bind(Func1<P, ?> fun) {
            return bind(fun, ALIAS(0));
        }

        public <P> SqlLambdaFun bind(Func1<P, ?> fun, BaseAlias alias) {
            Class<P> realClass = LambdaUtil.getRealClass(fun);
            String arg = StrUtil.format(TABLE_FIELD, realClass.getSimpleName(), alias.get(), SqlBuilderUtil.resolveFieldName(fun));
            args.add(arg);
            return this;
        }

        public String end() {
            String format = StrUtil.format(org, args.toArray());
            return StrUtil.format(" ({}) ", format);
        }

        public String condition() {
            String format = StrUtil.format(org, args.toArray());
            return StrUtil.format(" ({}) {} {}", format);
        }

        public <P> String as(Func1<P, ?> fun) {
            Class<P> realClass = LambdaUtil.getRealClass(fun);
            String format = StrUtil.format(org, args.toArray());
            return StrUtil.format(" ({}) {} ", format, SqlBuilderUtil.resolveFieldName(fun));
        }
    }

}
