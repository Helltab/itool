package icu.helltab.itool.multablequery.config.db.query.lambda;

import icu.helltab.itool.multablequery.config.db.query.SqlBuilder;
import icu.helltab.itool.multablequery.config.db.query.SqlBuilderJoin;

/**
 * lambda join
 */
public class SqlLambdaBuilderJoin extends SqlBuilderJoin {
	public SqlLambdaBuilderJoin(SqlBuilderJoin join) {
		builder = join.getBuilder();
	}

	public SqlBuilder on(String condition) {

		return this.builder;
	}
}
