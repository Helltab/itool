package icu.helltab.itool.common.http;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpPagedInfo<T> {
    private long count;
    List<T> list;
}
