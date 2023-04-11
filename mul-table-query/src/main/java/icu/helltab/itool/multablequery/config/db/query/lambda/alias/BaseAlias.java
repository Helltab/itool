package icu.helltab.itool.multablequery.config.db.query.lambda.alias;

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
