package icu.helltab.itool.multablequery.config.db.query;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import static icu.helltab.itool.multablequery.config.db.query.SqlBuilderUtil.*;

/**
 * 运算规则
 */
@Data
public class SqlBuilderWhereJudge {
	private SqlBuilderWhere where;
	private String cache;

	public static SqlBuilderWhereJudge build(SqlBuilderWhere where, String cache) {
		SqlBuilderWhereJudge condition = new SqlBuilderWhereJudge();
		condition.where = where;
		condition.cache = cache;
		return condition;
	}

	public SqlBuilderWhere judge(SqlKeywords keywords, Object val) {
		return judge(keywords, val, true);
	}

	public SqlBuilderWhere judge(SqlKeywords keywords, Object val, boolean raw) {
		if(ObjectUtil.isEmpty(val)) {
			return where;
		}
		where.getBuilder().getRawSql()
			.append(cache)
			.append(keywords.getName())
			.append(raw ? wrap("'", val) : val)
		;
		return where;
	}

	public SqlBuilderWhere eq(Object val) {
		return judge(SqlKeywords.EQ, val);
	}

	public SqlBuilderWhere ne(Object val) {
		return judge(SqlKeywords.NE, val);
	}

	public SqlBuilderWhere gt(Object val) {
		return judge(SqlKeywords.GT, val);
	}

	public SqlBuilderWhere gte(Object val) {
		return judge(SqlKeywords.GE, val);
	}

	public SqlBuilderWhere lt(Object val) {
		return judge(SqlKeywords.LT, val);
	}

	public SqlBuilderWhere lte(Object val) {
		return judge(SqlKeywords.LE, val);
	}

	public SqlBuilderWhere eq(boolean raw, Object val) {
		return judge(SqlKeywords.EQ, val, raw);
	}

	public SqlBuilderWhere ne(boolean raw, Object val) {
		return judge(SqlKeywords.NE, val, raw);
	}

	public SqlBuilderWhere gt(boolean raw, Object val) {
		return judge(SqlKeywords.GT, val, raw);
	}

	public SqlBuilderWhere gte(boolean raw, Object val) {
		return judge(SqlKeywords.GE, val, raw);
	}

	public SqlBuilderWhere lt(boolean raw, Object val) {
		return judge(SqlKeywords.LT, val, raw);
	}

	public SqlBuilderWhere lte(boolean raw, Object val) {
		return judge(SqlKeywords.LE, val, raw);
	}

	public SqlBuilderWhere in(Object ...condition) {
		return in(false, condition);
	}

	public SqlBuilderWhere in(boolean raw, Object... condition) {
		if(ObjectUtil.isAllEmpty(condition)) return where;
		where.getBuilder().getRawSql()
			.append(cache)
			.append(SqlKeywords.IN.getName())
			.append(wrap("(", raw ? condition : joint(",", "'", "'", condition), ")"))
		;
		return where;
	}

	public SqlBuilderWhere exists(String condition) {
		if(ObjectUtil.isEmpty(condition)) {
			return where;
		}
		where.getBuilder().getRawSql()
			.append(cache)
			.append(SqlKeywords.EXISTS.getName())
			.append(wrap("(", condition, ")"))
		;
		return where;
	}
}
