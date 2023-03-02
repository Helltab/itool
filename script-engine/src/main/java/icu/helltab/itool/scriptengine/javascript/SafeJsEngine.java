package icu.helltab.itool.scriptengine.javascript;

import java.util.Map;
import java.util.function.Function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.StrUtil;
import icu.helltab.itool.scriptengine.common.ScriptCheckException;
import icu.helltab.itool.scriptengine.common.ScriptEngine;
import icu.helltab.itool.scriptengine.common.ScriptEnv;
import icu.helltab.itool.scriptengine.common.constants.ScriptConstants;

public class SafeJsEngine extends ScriptEngine<Object> {
	private final static String CLOSURE = "(()=>{{}})()";

	/**
	 * 禁止初始化
	 */
	private SafeJsEngine() {
	}

	public static SafeJsEngine instance() {
		return Singleton.get(SafeJsEngine.class);
	}

	public Object execHandle(String expression, Map<String, Object> context, Function<String, Object> function) {
		final Context ctx = Context.enter();
		// 使用安全的上下文
		ScriptableObject scope = ScriptRuntime.initSafeStandardObjects(ctx, new Global(), true);
		ScriptableObject.putProperty(scope, ScriptConstants.ENV, scriptEnv);
		final Object result = ctx.evaluateString(scope, StrUtil.format(CLOSURE, expression), "rhino.js", 1, null);
		Context.exit();
		return result;
	}


	@Override
	protected ScriptEnv initScriptEnv(Map<String, Object> context) {
		JavascriptEnv javascriptEnv = new JavascriptEnv();
		javascriptEnv.addArgs(context);
		return javascriptEnv;
	}

	@Override
	protected void check(String expression) throws ScriptCheckException {
	}
}

