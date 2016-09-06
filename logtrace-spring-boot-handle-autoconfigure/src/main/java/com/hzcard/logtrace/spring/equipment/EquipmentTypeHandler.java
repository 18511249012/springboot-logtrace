package com.hzcard.logtrace.spring.equipment;

import javax.servlet.http.HttpServletRequest;

import com.hzcard.logtrace.spring.boot.handle.interceptor.EquipmentTypeEnum;

public interface EquipmentTypeHandler {
	/**
	 * �����豸����
	 * @param request
	 * @return
	 */
	EquipmentTypeEnum resolve(HttpServletRequest request);
	
}
