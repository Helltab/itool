package icu.helltab.itool.multablequery.config.db.query.lambda.alias;

/**
 * 子查询别名
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
