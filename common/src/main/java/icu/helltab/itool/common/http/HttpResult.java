package icu.helltab.itool.common.http;

import lombok.Data;

/**
 * @Description 接口通用返回封装
 * @Author helltab
 * @Date 2021/12/8 10:55
 */
@Data
public class HttpResult<T> extends BaseHttpResult<T> {
    private transient HttpPaged paged;
    private transient StackTraceElement controllerStack;

    public HttpResult() {
    }


    /**
     * get result from threadLocal
     *
     * @return
     */
    public static <T> HttpResult<T> build() {
        return ThreadLocalUtil.get(new HttpResult<T>());
    }
    public static <T> HttpResult<T> build(T data) {
        HttpResult<T> result = ThreadLocalUtil.get(new HttpResult<T>());
        result.setData(data);
        return result;
    }

    public static <T> HttpResult<T> build(Process<HttpResult<T>> consumer) throws Exception {
        HttpResult<T> httpResult = build();
        httpResult.controllerStack = Thread.currentThread().getStackTrace()[2];
        consumer.run(httpResult);
        return httpResult;
    }

    @Override
    protected HttpStatusInf initSuccessStatus() {
        return HttpStatusEnum.SUCCESS;
    }

    @Override
    protected HttpStatusInf initFailStatus() {
        return HttpStatusEnum.FAIL;
    }

    public interface Process<T> {
        void run(T t) throws Exception;
    }
    public interface Process2<T, M> {
        M run(T t) throws Exception;
    }

    public boolean ifSuccess() {
        return this.getCode() == HttpStatusEnum.SUCCESS.getCode();
    }


}
