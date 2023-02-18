package icu.helltab.itool.common.http;


public abstract class BaseHttpResult<T> extends BaseResult {
    private int code;
    private HttpPaged paged;
    private T data;
    private final HttpStatusInf successStatus = this.initSuccessStatus();
    private final HttpStatusInf failStatus = this.initFailStatus();

    protected abstract HttpStatusInf initSuccessStatus();

    protected abstract HttpStatusInf initFailStatus();

    public BaseHttpResult() {
        this.code = this.successStatus.getCode();
        this.msg = this.successStatus.getDesc();
    }

    protected void errorDetail() {
        this.code = this.failStatus.getCode();
        this.msg = this.failStatus.getDesc();
    }

    public BaseResult status(HttpStatusInf status) {
        this.code = status.getCode();
        this.msg = status.getDesc();
        return this;
    }

    public int getCode() {
        return this.code;
    }

    public BaseResult setCode(int code) {
        this.code = code;
        return this;
    }

    public T getData() {
        return this.data;
    }

    public BaseResult setData(T data) {
        this.data = data;
        return this;
    }

    public HttpPaged getPaged() {
        return this.paged;
    }

    public void setPaged(HttpPaged paged) {
        this.paged = paged;
    }
}
