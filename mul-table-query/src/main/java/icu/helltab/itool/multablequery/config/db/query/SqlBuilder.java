package icu.helltab.itool.multablequery.config.db.query;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import icu.helltab.itool.multablequery.config.db.query.lambda.SqlLambdaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:54
 * @desc 这里是自定义 sql 拼装的主类
 * @see SqlLambdaBuilder
 */
@Data
public class SqlBuilder {
	/**
	 * 只生成条件
	 */
	protected boolean isRawCondition = false;
//	private final StringBuilder rawSql = new StringBuilder();
	protected final StringBuilder selectRawSql = new StringBuilder();
	protected final StringBuilder fromRawSql = new StringBuilder();
	protected final StringBuilder whereRawSql = new StringBuilder();
	protected final StringBuilder groupRawSql = new StringBuilder();
	protected final StringBuilder orderRawSql = new StringBuilder();
	protected final StringBuilder otherRawSql = new StringBuilder();
	protected Map<LogicObj, AtomicInteger> logic = new HashMap<>();

	protected boolean hasWhere = false;
	protected boolean hasFrom = false;
	protected boolean hasGroup = false;

	@Data
	@AllArgsConstructor
	protected static class LogicObj {
		Class<?> realClazz;
		Field field;
	}

	public StringBuilder getRawSql() {

		return selectRawSql
				.append(fromRawSql)
				.append(whereRawSql)
				.append(genLogic())
				.append(groupRawSql)
				.append(orderRawSql)
				.append(otherRawSql)
				;
	}

	public String build() {
		return getRawSql().toString();
	}

	public String genLogic() {return "";};

	/**
	 * 添加 from
	 * 适配 inner join 的方式
	 *
	 * @param from
	 * @return
	 */
	public SqlBuilder from(String... from) {
		if(!hasFrom) {
			fromRawSql.append(SqlKeywords.FROM);
			hasFrom = true;
		}

		fromRawSql.append(SqlBuilderUtil.joint(", ", "","", from));
		return this;
	}

	public void  where(String condition) {
		if(!hasWhere) {
			if(!isRawCondition) {
				whereRawSql.append(SqlKeywords.WHERE);
			}
			whereRawSql.append(condition);
			hasWhere = true;
			return;
		}
		whereRawSql.append(SqlKeywords.AND).append(condition);
	}

	/**
	 * 通用 join
	 * @param join
	 * @param joinKeywords
	 * @return
	 */
	public SqlBuilderJoin join(String join, SqlKeywords joinKeywords) {
		fromRawSql.append(joinKeywords)
				.append(join)
		;
		return SqlBuilderJoin.build(this);
	}

	public SqlBuilder group(String ...groups) {
		if(!hasGroup) {
			groupRawSql.append(SqlKeywords.GROUP);
			hasGroup = true;
		}
		groupRawSql.append(SqlBuilderUtil.joint(", ", "","", groups));
		return this;
	}


}
