package icu.helltab.itool.multablequery.config.db.query;

import lombok.Data;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:55
 * @desc 解决多条件 join 的问题
 * @see
 */
@Data
public class SqlBuilderJoin {
	protected SqlBuilder builder;

	public static SqlBuilderJoin build(SqlBuilder builder) {
		SqlBuilderJoin builderJoin = new SqlBuilderJoin();
		builderJoin.builder = builder;
		return builderJoin;
	}

	public SqlBuilder on(String condition) {
		builder.fromRawSql.append(SqlKeywords.ON)
			.append(condition)
		;
		return this.builder;
	}
}
