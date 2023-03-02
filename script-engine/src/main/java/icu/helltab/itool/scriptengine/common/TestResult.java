package icu.helltab.itool.scriptengine.common;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 脚本测试结果
 */
public class TestResult {
	private StringBuffer msg;

	public synchronized TestResult append(String result) {
		if (msg == null) msg = new StringBuffer();
		msg.append(result).append("\n");
		return this;
	}
	public synchronized TestResult append(String title, Object ...msg) {
		if (msg == null) return this;
		String result = Arrays.stream(msg).map(Object::toString)
			.collect(Collectors.joining(", "));
		append("[" + title + "]:\t" + result);
		return this;
	}

	@Override
	public String toString() {
		return msg.toString();
	}
}
