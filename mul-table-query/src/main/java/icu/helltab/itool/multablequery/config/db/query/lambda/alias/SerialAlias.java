package icu.helltab.itool.multablequery.config.db.query.lambda.alias;

import icu.helltab.itool.multablequery.config.db.query.lambda.alias.BaseAlias;

public class SerialAlias extends BaseAlias {
	public SerialAlias(int idx) {
		super(idx);
	}

	@Override
	protected String initAlias() {
		return "SERIAL";
	}
}
