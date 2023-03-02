package icu.helltab.itool.scriptengine.common;

import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.util.ObjectUtil;


public abstract class ScriptEnv {

	protected TestResult testResult;
	public Map<String, Object> args;

	public abstract void debug(Object... msg);

	public abstract void error(Object... msg);

	public ScriptEnv addArgs(Map<String, Object> args) {
		if (ObjectUtil.isNull(args)) return this;
		if (ObjectUtil.isNull(this.args)) this.args = new HashMap<>();
		this.args.putAll(args);
		return this;
	}

	;

	public TestResult getTestResult() {
		return testResult;
	}

	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}
}
