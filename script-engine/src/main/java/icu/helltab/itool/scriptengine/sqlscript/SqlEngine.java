package icu.helltab.itool.scriptengine.sqlscript;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import icu.helltab.itool.scriptengine.common.ScriptCheckException;
import icu.helltab.itool.scriptengine.common.ScriptEngine;
import icu.helltab.itool.scriptengine.common.ScriptEnv;
import icu.helltab.itool.scriptengine.common.SqlEnv;

public class SqlEngine extends ScriptEngine<List<Map<String, Object>>> {

	@Override
	protected ScriptEnv initScriptEnv(Map<String, Object> context) {
		return new SqlEnv();
	}

	@Override
	protected void check(String expression) throws ScriptCheckException {

	}

	@Override
	protected List<Map<String, Object>> execHandle(String expression, Map<String, Object> context, Function<String, List<Map<String, Object>>> function) {
		return null;
	}
}
