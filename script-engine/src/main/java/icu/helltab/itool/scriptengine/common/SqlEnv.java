package icu.helltab.itool.scriptengine.common;

public class SqlEnv extends ScriptEnv {

	@Override
	public void debug(Object... msg) {
		testResult.append("sql-debug", msg);
	}

	public void error(Object... msg) {
		testResult.append("sql-error",  msg);
	}

}
