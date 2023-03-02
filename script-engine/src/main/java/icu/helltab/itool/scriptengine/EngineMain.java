
package icu.helltab.itool.scriptengine;

import icu.helltab.itool.scriptengine.common.TestResult;
import icu.helltab.itool.scriptengine.javascript.SafeJsEngine;

public class EngineMain {
	public static void main(String[] args)  {
		TestResult test = SafeJsEngine.instance().test("env.debug(2)", null, null);
		System.out.println(test);
	}

}
