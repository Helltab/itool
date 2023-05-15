package icu.helltab.itool.multablequery.config.db.query.lambda.alias;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 14:00
 * @desc 子查询别名
 * @see
 */
public class SubQueryAlias extends BaseAlias{
	public SubQueryAlias(int idx) {
		super(idx);
	}

	@Override
	protected String initAlias() {
		return "SUB_QUERY";
	}
}
