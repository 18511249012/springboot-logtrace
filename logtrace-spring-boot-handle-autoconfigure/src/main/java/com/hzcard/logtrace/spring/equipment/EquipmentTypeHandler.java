package com.hzcard.logtrace.spring.equipment;

import javax.servlet.http.HttpServletRequest;

import com.hzcard.logtrace.spring.boot.handle.interceptor.EquipmentTypeEnum;

public interface EquipmentTypeHandler {
	/**
	 * 解析设备类型
	 * @param request
	 * @return
	 */
	EquipmentTypeEnum resolve(HttpServletRequest request);
	
}
