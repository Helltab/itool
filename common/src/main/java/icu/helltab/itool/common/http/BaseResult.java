package icu.helltab.itool.common.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;

public abstract class BaseResult implements Serializable {
    protected String msg;
    private final ArrayList<String> msgList = new ArrayList();

    protected BaseResult() {
    }

    public BaseResult error(Object... msgArray) {
        this.errorDetail();
        return this.setMsg(msgArray);
    }

    public BaseResult errorF(String template, Object... vars) {
        return this.error(StrUtil.format(template, vars));
    }

    public BaseResult setMsg(Object... msgArray) {
        Arrays.stream(msgArray).filter(Objects::nonNull).distinct().forEach((x) -> {
            this.msgList.add(x.toString());
        });
        return this;
    }

    public BaseResult setMsgF(String template, Object... vars) {
        this.setMsg(StrUtil.format(template, vars));
        return this;
    }

    protected abstract void errorDetail();

    public final String getMsg() {
        if (this.msgList.isEmpty()) {
            return this.msg;
        } else {
            return this.msgList.size() == 1 ? (String)this.msgList.get(0) : this.msgList.toString();
        }
    }

    public String toString() {
        JSONConfig jsonConfig = new JSONConfig();
        jsonConfig.setIgnoreError(true);
        jsonConfig.setIgnoreNullValue(false);
        return JSONUtil.toJsonStr(this, jsonConfig);
    }
}
