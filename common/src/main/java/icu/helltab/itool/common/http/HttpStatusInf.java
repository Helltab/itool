package icu.helltab.itool.common.http;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 14:44
 * @desc 状态码接口类, 适用于扩展
 * @see
 */
public interface HttpStatusInf {
    int getCode();

    String getDesc();
}
