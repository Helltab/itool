package icu.helltab.itool.common.http;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 14:44
 * @desc 分页结果
 * @see
 */
@Data
@AllArgsConstructor
public class HttpPagedInfo<T> {
    private long pageNum;
    private long pageSize;
    private long count;
    List<T> list;
}
