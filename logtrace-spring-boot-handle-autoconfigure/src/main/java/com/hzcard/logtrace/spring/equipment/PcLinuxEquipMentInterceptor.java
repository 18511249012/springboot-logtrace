package com.hzcard.logtrace.spring.equipment;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;

import com.hzcard.logtrace.spring.boot.handle.interceptor.EquipmentTypeEnum;
import com.hzcard.logtrace.spring.boot.handle.interceptor.EventHandlerInterceptor;

public class PcLinuxEquipMentInterceptor extends EventHandlerInterceptor {

	public PcLinuxEquipMentInterceptor(ApplicationContext context) {
		super(context);
	}

	@Override
	public EquipmentTypeEnum resolve(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null || userAgent.trim().length() == 0 || userAgent.indexOf("(") < 0
				|| userAgent.indexOf(")") < 0)
			return EquipmentTypeEnum.NOTIDENTITY;
		String[] osPlatforms = userAgent.substring(userAgent.indexOf("(") + 1, userAgent.indexOf(")")).split(";");
		if(osPlatforms.length<=1)
			return EquipmentTypeEnum.NOTIDENTITY;
		if (!osPlatforms[1].startsWith("Linux"))
			return EquipmentTypeEnum.NOTIDENTITY;
		if(userAgent.startsWith("Mozilla"))
			return EquipmentTypeEnum.LINUXWEB;		//linux web
		return EquipmentTypeEnum.NOTIDENTITY;
	}

}
