package icu.helltab.itool.common.http;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;

public class HttpPaged {
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String KEY_PAGE_NUM = "pageNum";
    public static final String KEY_PAGE_SIZE = "pageSize";
    private int pageNum;
    private int pageSize;
    private long total;
    private int from;
    private int to;

    public HttpPaged(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return this.pageNum;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotal() {
        return this.total;
    }

    public int getFrom() {
        return (this.pageNum - 1) * this.pageSize;
    }

    public int getTo() {
        return this.pageNum * this.pageSize;
    }

    public String toString() {
        JSONConfig jsonConfig = new JSONConfig();
        jsonConfig.setOrder(true);
        jsonConfig.setIgnoreError(true);
        jsonConfig.setIgnoreNullValue(false);
        return JSONUtil.toJsonStr(this, jsonConfig);
    }
}
