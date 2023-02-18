package icu.helltab.itool.multablequery.config.db.query;


import java.util.function.Consumer;

import lombok.Data;

@Data
public class SqlBuilderWhere {
	private SqlBuilder builder;

	public static SqlBuilderWhere build(SqlBuilder builder) {
		SqlBuilderWhere builderJoin = new SqlBuilderWhere();
		builderJoin.builder = builder;
		return builderJoin;
	}



	public SqlBuilderWhereJudge and() {
		return and("");
	}
	public SqlBuilderWhereJudge or() {
		return or("");
	}
	public SqlBuilderWhereJudge and(String condition) {
		StringBuilder cache = builder.newKeywords(SqlKeywords.AND)
			.append(condition);
		return SqlBuilderWhereJudge.build(this, cache.toString());
	}

	public SqlBuilderWhereJudge or(String condition) {
		StringBuilder cache = builder.newKeywords(SqlKeywords.OR)
			.append(condition);
		return SqlBuilderWhereJudge.build(this, cache.toString());
	}
	public SqlBuilderWhere and(Consumer<SqlBuilder> inner) {
		inner.accept(new SqlBuilder());
		return this;
	}

	public SqlBuilderWhere or(Consumer<SqlBuilder> inner) {
		inner.accept(new SqlBuilder());
		return this;
	}



	/**
	 * 标记 where 结束
	 *
	 * @return
	 */
	public SqlBuilder endWhere() {
		return this.builder;
	}


}
