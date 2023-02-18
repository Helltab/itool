package icu.helltab.itool.multablequery.config.db.query;

import lombok.Data;

/**
 * join 条件
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
		builder.appendKeywords(SqlKeywords.ON)
			.append(condition)
		;
		return this.builder;
	}
}
