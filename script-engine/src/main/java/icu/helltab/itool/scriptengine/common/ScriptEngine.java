package icu.helltab.itool.scriptengine.common;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import cn.hutool.json.JSONUtil;

public abstract class ScriptEngine<T> {

	protected ScriptEnv scriptEnv;


	protected abstract ScriptEnv initScriptEnv(Map<String, Object> context);

	private void init(Map<String, Object> context) {
		this.scriptEnv = initScriptEnv(context);
		this.scriptEnv.setTestResult( new TestResult());
	}

	;

	/**
	 * 检查表达式
	 *
	 * @param expression
	 * @return
	 * @throws Exception
	 */
	protected abstract void check(String expression) throws ScriptCheckException;

	protected abstract T execHandle(String expression, Map<String, Object> context, Function<String, T> function);


	public TestResult test(String expression, Map<String, Object> context, Function<String, T> function) {
		init(context);
		TestResult testResult = scriptEnv.getTestResult();
		testResult.append("script-test", "start");
		try {
			check(expression);
			execHandle(expression, context, function);
		} catch (ScriptCheckException e) {
			testResult.append("script-check", e.getMessage());
		}
		return testResult.append("script-test", "end");
	}


	/**
	 * 执行脚本
	 *
	 * @param expression
	 * @param context
	 * @param function
	 * @return
	 * @throws ScriptCheckException
	 */
	public T exec(String expression, Map<String, Object> context, Function<String, T> function) throws ScriptCheckException {
		init(context);
		check(expression);
		return execHandle(expression, context, function);
	}

}
