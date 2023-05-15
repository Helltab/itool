package icu.helltab.itool.multablequery.config.db.query.lambda.alias;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:59
 * @desc 别名设置, 用来标识在同一 Sql 中的相同表
 * @see
 */
public abstract class BaseAlias {
	protected int idx;
	protected String aliasName;

	protected abstract String initAlias();

	protected BaseAlias(int idx) {
		this.idx = idx;
		this.aliasName = initAlias();
	}

	public String get() {
		return this.aliasName + "_" + this.idx;
	}
	public String  get(int idx) {
		return this.aliasName + "_" + idx;
	}

	@Override
	public String toString() {
		return get();
	}
}
