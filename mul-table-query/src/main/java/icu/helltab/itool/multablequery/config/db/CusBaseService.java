package icu.helltab.itool.multablequery.config.db;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import icu.helltab.itool.common.ex.CusException;
import icu.helltab.itool.common.http.HttpPaged;
import icu.helltab.itool.common.http.HttpPagedInfo;
import icu.helltab.itool.common.http.HttpResult;
import icu.helltab.itool.common.http.ThreadLocalUtil;
import icu.helltab.itool.multablequery.config.db.multi.MySqlRunner;
import icu.helltab.itool.multablequery.config.db.query.lambda.SqlLambdaBuilder;

/**
 * 多数据源 Service 配置, 如果只有一个数据源, 不需要做扩展
 *
 * @param <M>
 * @param <T>
 * @see
 */
public abstract class CusBaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {
    protected MySqlRunner mySqlRunner;


    @PostConstruct
    public void init() {
        this.mySqlRunner = getMySqlRunner();
    }

    protected abstract MySqlRunner getMySqlRunner();

    public boolean remove(Consumer<LambdaQueryWrapper<T>> consumer) {
        return handle(consumer, this::remove);
    }

    public boolean has(Consumer<LambdaQueryWrapper<T>> consumer) {
        return count(consumer) > 0;
    }

    public int count(Consumer<LambdaQueryWrapper<T>> consumer) {
        return handle(consumer, this::count);
    }

    public T getOne(Consumer<LambdaQueryWrapper<T>> consumer) {
        return handle(consumer, this::getOne);
    }

    public List<T> list(Consumer<LambdaQueryWrapper<T>> consumer) {
        return handle(consumer, this::list);
    }

    public HttpPagedInfo<T> pageQuery(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> lambda = new LambdaQueryWrapper<>();
        consumer.accept(lambda);
        HttpResult<Object> build = HttpResult.build();
        HttpPaged paged = build.getPaged();
        if (paged == null) {
            build.error("尝试在接口上添加 @Paged 注解");
            return new HttpPagedInfo<>(0, 0, 0, null);
        }
        Page<T> page = this.page(ThreadLocalUtil.get(new Page<>(paged.getPageNum(), paged.getPageSize())), lambda);
        return new HttpPagedInfo<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    private <E> E handle(Consumer<LambdaQueryWrapper<T>> consumer,
                         Function<LambdaQueryWrapper<T>, E> fun) {
        LambdaQueryWrapper<T> lambda = new LambdaQueryWrapper<>();
        consumer.accept(lambda);
        return fun.apply(lambda);
    }

    private <E> E handle(Consumer<LambdaQueryWrapper<T>> consumer,
                         BiFunction<Object, LambdaQueryWrapper<T>, E> fun, Object arg) {
        LambdaQueryWrapper<T> lambda = new LambdaQueryWrapper<>();
        consumer.accept(lambda);
        return fun.apply(arg, lambda);
    }

    //	private final static String SELECT_ONE = "select temp.* from ({}) temp limit 1";
    private final static String SELECT_LIMIT = "select temp.* from ({}) temp limit {}, {}";
    private final static String SELECT_COUNT = "select count(1) from ({}) temp";

    public <E> List<E> exList(Consumer<SqlLambdaBuilder> consumer, Class<E> clazz) {
        String finalSql = SqlLambdaBuilder.lambda(consumer);
        return listCustom(finalSql, clazz);
    }


    public <E> E exOne(Consumer<SqlLambdaBuilder> consumer, Class<E> clazz) {
        String finalSql = SqlLambdaBuilder.lambda(consumer);
        return getOneCustom(StrUtil.format(SELECT_LIMIT, finalSql, 0, 1), clazz);
    }

    public long exCount(Consumer<SqlLambdaBuilder> consumer) {
        String finalSql = SqlLambdaBuilder.lambda(consumer);
        return getOneCustom(StrUtil.format(SELECT_COUNT, finalSql), long.class);
    }

    private <E> E getOneCustom(String sql, Class<E> clazz) {
        Map<String, Object> stringObjectMap = getMySqlRunner().selectOne(sql);
        if (clazz.isPrimitive() || clazz == String.class) {
            return (E) stringObjectMap.values().stream().findFirst().orElse(null);
        }
        return BeanUtil.toBean(stringObjectMap, clazz);
    }

    private <E> List<E> listCustom(String sql, Class<E> clazz) {
        List<Map<String, Object>> list = getMySqlRunner().selectList(sql);
        if (clazz.isPrimitive() || clazz == String.class) {
            return list.stream()
                    .map(x -> (E) x.values().stream().findFirst().orElse(null))
                    .collect(Collectors.toList());
        }
        return list.stream().map(x -> BeanUtil.toBean(x, clazz))
                .collect(Collectors.toList());
    }

    public Boolean exHas(Consumer<SqlLambdaBuilder> consumer) {
        String finalSql = SqlLambdaBuilder.lambda(s -> {
            s.selectRaw("count(1)");
            consumer.accept(s);
        });
        long o = getOneCustom(finalSql, Long.TYPE);
        return o > 0;
    }

    public <E> HttpPagedInfo<E> exPage(Consumer<SqlLambdaBuilder> consumer, Class<E> clazz) throws Exception {
        HttpResult<Object> result = HttpResult.build();
        HttpPaged paged = result.getPaged();
        if (paged == null) {
            throw new CusException("尝试在接口上添加 @Page ");
        }
        String finalSql = SqlLambdaBuilder.lambda(consumer);
        List<E> list = listCustom(StrUtil.format(SELECT_LIMIT,
                finalSql, paged.getFrom(), paged.getPageSize()), clazz);
        long count = getOneCustom(StrUtil.format(SELECT_COUNT, finalSql), long.class);
        return new HttpPagedInfo<>(paged.getPageNum(), paged.getPageSize(), count, list);
    }


}
