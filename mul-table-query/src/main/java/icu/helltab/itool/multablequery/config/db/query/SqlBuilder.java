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

	public SqlBuilder group(String ...groups) {
		appendKeywords(SqlKeywords.GROUP)
			.append(joint(", ", "","", groups));
		return this;
	}


}
