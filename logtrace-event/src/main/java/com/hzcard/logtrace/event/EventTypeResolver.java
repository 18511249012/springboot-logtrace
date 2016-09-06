package com.hzcard.logtrace.event;

import javax.servlet.http.HttpServletRequest;

public interface EventTypeResolver {

	/**
	 * 根据请求，返回事件名�?
	 * @param request
	 * @return
	 */
	String eventGen(HttpServletRequest request);
}
