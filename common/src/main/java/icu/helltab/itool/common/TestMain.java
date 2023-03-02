package icu.helltab.itool.common;


import java.io.Console;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.LazilyLoadedCtor;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.expression.engine.rhino.RhinoEngine;

public class TestMain {

	public static void log(Object ...arg) {
		if(ObjectUtil.isEmpty(arg)) {
			System.out.println();
			return;
		}
		String collect = Arrays.stream(arg).map(Object::toString).collect(Collectors.joining(", "));
		System.out.println(collect);
	}
	public static class SafeEngine extends RhinoEngine {
		@Override
		public Object eval(String expression, Map<String, Object> context) {
			final Context ctx = Context.enter();
			// 使用安全的上下文
			ScriptableObject scope = ScriptRuntime.initSafeStandardObjects(ctx, new Global(), true);
//			Global scope = new Global();
			context.forEach((key, value) -> {
				// 将java对象转为js对象后放置于JS的作用域中
				ScriptableObject.putProperty(scope, key, Context.javaToJS(value, scope));
			});
			ScriptableObject.putProperty(scope, "env", new TestMain());

			final Object result = ctx.evaluateString(scope, expression, "rhino.js", 1, null);
			Context.exit();
			return result;
		}
	}

	public static void print(String... msg) {
		System.out.println(String.join(",", msg));
	}

	public interface LogFun {
		void print(String... msg);
	}

	public static void main(String[] args) throws ScriptException {
		RhinoEngine rhinoEngine = new SafeEngine();
		MapBuilder<String, LogFun> builder = MapUtil.builder();
		builder.put("log", TestMain::print);

		Object b = rhinoEngine.eval("(a=>{var c = 1;that.log(java);  return 1;})(1)",
			MapUtil.of(
				"b", new int[]{23, 24})
		);


		System.out.println(b);
	}
}
