package icu.helltab.itool.scriptengine.javascript;

import icu.helltab.itool.scriptengine.common.ScriptEnv;

public class JavascriptEnv extends ScriptEnv {
	public void debug(Object... msg) {
		testResult.append("js-debug",  msg);
	}


	public void error(Object... msg) {
		testResult.append("js-error",  msg);
	}

}
