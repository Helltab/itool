package icu.helltab.itool.multablequery.config.db.query;

import java.util.function.Consumer;

import lombok.Data;

import static icu.helltab.itool.multablequery.config.db.query.SqlBuilderUtil.joint;


@Data
public class SqlBuilder {
	/**
	 * 只生成条件
	 */
	protected boolean isRawCondition = false;
	private final StringBuilder rawSql = new StringBuilder();
	protected boolean hasWhere = false;
	protected boolean hasFrom = false;

	public static String of(Consumer<SqlBuilder> consumer) {
		SqlBuilder builder = new SqlBuilder();
		consumer.accept(builder);
		return builder.build();
	}



	public String build() {
		return rawSql.toString();
	}


	/**
	 * 添加关键字
	 *
	 * @param keywords
	 * @return
	 */
	public StringBuilder appendKeywords(SqlKeywords keywords) {
		rawSql.append(keywords.getName());
		return rawSql;
	}
	public StringBuilder newKeywords(SqlKeywords keywords) {
		return new StringBuilder().append(keywords.getName());
	}


	/**
	 * 添加 select
	 * // todo 校验 case where 等情况
	 *
	 * @param select
	 * @return
	 */
	public SqlBuilder select(Object... select) {
		rawSql.delete(0, rawSql.length());
		appendKeywords(SqlKeywords.SELECT)
			.append(joint(", ","", "", select))
		;
		return this;
	}


	/**
	 * 添加 from
	 * 适配 inner join 的方式
	 *
	 * @param from
	 * @return
	 */
	public SqlBuilder from(String... from) {
		if(!hasFrom) {
			appendKeywords(SqlKeywords.FROM);
			hasFrom = true;
		}
		rawSql.append(joint(", ", "","", from));
		return this;
	}
	public SqlBuilderJoin join(Consumer<SqlBuilder> inner, String alias) {
		SqlBuilder sqlBuilder = new SqlBuilder();
		inner.accept(sqlBuilder);
		appendKeywords(SqlKeywords.JOIN)
			.append(sqlBuilder.build())
			.append(" ")
			.append(alias)
		;
		return SqlBuilderJoin.build(this);
	}

	/**
	 * 通用 join
	 * @param join
	 * @param joinKeywords
	 * @return
	 */
	public SqlBuilderJoin join(String join, SqlKeywords joinKeywords) {
		appendKeywords(joinKeywords)
			.append(join)
		;
		return SqlBuilderJoin.build(this);
	}

	/**
	 * 交集
	 * @param join
	 * @return
	 */
	public SqlBuilderJoin join(String join) {
		return join(join, SqlKeywords.JOIN);
	}

	/**
	 * 左表全集
	 * @param join
	 * @return
	 */
	public SqlBuilderJoin leftJoin(String join) {
		return join(join, SqlKeywords.LEFT_JOIN);
	}

	/**
	 * 右表全集
	 * @param join
	 * @return
	 */
	public SqlBuilderJoin rightJoin(String join) {
		return join(join, SqlKeywords.RIGHT_JOIN);
	}

	/**
	 * 左右表全集
	 * @param join
	 * @return
	 */
	public SqlBuilderJoin fullJoin(String join) {
		return join(join, SqlKeywords.FULL_JOIN);
	}

	/**
	 * where 开始
	 * 添加 1=1 来适配条件
	 *
	 * @return
	 */
	public SqlBuilderWhere where() {
		if(!hasWhere) {
			appendKeywords(SqlKeywords.WHERE).append(" 1=1 ");
			hasWhere = true;
		}
		return SqlBuilderWhere.build(this);
	}

	public void  where(String condition) {
		if(!hasWhere) {
			if(isRawCondition) {
				rawSql.append(condition);
			}else {
				appendKeywords(SqlKeywords.WHERE).append(condition);
			}
			hasWhere = true;
			return;
		}
		appendKeywords(SqlKeywords.AND).append(condition);
	}



	public SqlBuilder group(String ...groups) {
		appendKeywords(SqlKeywords.GROUP)
			.append(joint(", ", "","", groups));
		return this;
	}
	public SqlBuilder having(String ...having) {
		appendKeywords(SqlKeywords.HAVING)
			.append(joint(", ", "","", having));
		return this;
	}
	public SqlBuilder order(String ...orders) {
		appendKeywords(SqlKeywords.ORDER)
			.append(joint(", ", "","", orders));
		return this;
	}
	public SqlBuilder limit(long from, long to) {
		appendKeywords(SqlKeywords.LIMIT)
			.append(joint(", ", "","", from, to));
		return this;
	}




}
