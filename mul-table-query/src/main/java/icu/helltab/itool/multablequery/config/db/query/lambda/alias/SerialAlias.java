package icu.helltab.itool.multablequery.config.db.query.lambda.alias;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:59
 * @desc 正常的 sql 序号别名
 * @see
 */
public class SerialAlias extends BaseAlias {
	public SerialAlias(int idx) {
		super(idx);
	}

	@Override
	protected String initAlias() {
		return "SERIAL";
	}
}
