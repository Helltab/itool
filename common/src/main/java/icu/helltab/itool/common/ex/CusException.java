package icu.helltab.itool.common.ex;

import cn.hutool.core.util.StrUtil;

/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 14:45
 * @desc 自定义异常, 可以打印模板消息
 * @see
 */
public class CusException extends Exception{
	public CusException(String template, Object... args) {
		super(StrUtil.format(template, args));
	}
}
