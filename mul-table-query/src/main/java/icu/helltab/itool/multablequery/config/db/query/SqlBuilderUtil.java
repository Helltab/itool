package icu.helltab.itool.multablequery.config.db.query;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.SqlUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.helltab.itool.common.ex.CusException;

/**
 * sql 生成框架工具类
 */
public class SqlBuilderUtil {
	static <T> String joint(String flag, String prefix, String suffix, T... values) {
		return Arrays.stream(values)
			.filter(ObjectUtil::isNotEmpty)
			.map(x -> x instanceof Number ? String.valueOf(x) : prefix + x.toString() + suffix)
			.collect(Collectors.joining(flag));
	}

	static <T> String wrap(String pre, T value, String after) {
		if (value instanceof Number) return String.valueOf(value);
		return pre + value.toString() + after;
	}

	static <T> String wrap(String pre, T value) {
		return wrap(pre, value, pre);
	}

	public static String prettySql(String rawSql) {
		return SqlUtil.formatSql(rawSql);
	}


	//////////lambda

	public static <P> String getLambdaFieldName(Func1<P, ?> func1) throws Exception {
		String fieldName = LambdaUtil.getFieldName(func1);
		Class<P> realClass = LambdaUtil.getRealClass(func1);

		StringBuilder sb = new StringBuilder();
		Field field = ReflectUtil.getField(realClass, fieldName);
		TableField tableField = field.getAnnotation(TableField.class);
		TableName tableName = realClass.getAnnotation(TableName.class);
		if (ObjectUtil.isNull(tableName)) {
			throw new CusException(realClass + ": 无 TableName 注解");
		}
		sb.append(tableName.value()).append(".")
			.append(ObjectUtil.isNull(tableField)
				? fieldName
				: tableField.value()
			);
		return sb.toString();
	}

	public static <P,A> String resolveFieldName(Func1<P, A> func1) {
		Class<P> aClass = LambdaUtil.getRealClass(func1);
		String fieldName = LambdaUtil.getFieldName(func1);
		Field field = ReflectUtil.getField(aClass, fieldName);
		TableField annotation = field.getAnnotation(TableField.class);
		return (annotation != null && StrUtil.isNotBlank(annotation.value()))
			?annotation.value()
			:StrUtil.toUnderlineCase(fieldName);

	}
	public static <T> String resolveTableName(Class<T> tableClass) {
		TableName annotation = tableClass.getAnnotation(TableName.class);
		return annotation != null
			?annotation.value()
			:StrUtil.toUnderlineCase(tableClass.getSimpleName());
	}



}
