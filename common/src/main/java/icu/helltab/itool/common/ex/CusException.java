package icu.helltab.itool.common.ex;

import cn.hutool.core.util.StrUtil;

/**
 * 自定义异常
 */
public class CusException extends Exception{
	public CusException(String template, Object... args) {
		super(StrUtil.format(template, args));
	}
}
