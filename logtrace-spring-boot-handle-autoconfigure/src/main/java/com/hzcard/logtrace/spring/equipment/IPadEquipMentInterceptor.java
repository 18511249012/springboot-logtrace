package com.hzcard.logtrace.spring.equipment;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;

import com.hzcard.logtrace.spring.boot.handle.interceptor.EquipmentTypeEnum;
import com.hzcard.logtrace.spring.boot.handle.interceptor.EventHandlerInterceptor;

public class IPadEquipMentInterceptor extends EventHandlerInterceptor {

	public IPadEquipMentInterceptor(ApplicationContext context) {
		super(context);
	}

	@Override
	public EquipmentTypeEnum resolve(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null || userAgent.trim().length() == 0 || userAgent.indexOf("(") < 0
				|| userAgent.indexOf(")") < 0)
			return EquipmentTypeEnum.NOTIDENTITY;
		
		String osPlatform = userAgent.substring(userAgent.indexOf("(") + 1, userAgent.indexOf(")")).split(";")[0];
		if(!osPlatform.equals("iPad"))
			return EquipmentTypeEnum.NOTIDENTITY;
		if(userAgent.startsWith("Mozilla"))           //iPad web
			return EquipmentTypeEnum.IPADWEB;
		else if(userAgent.startsWith("EasyPoints"))   //iPadÔ­Éú
			return EquipmentTypeEnum.IPADNATIVE;
		else
			return EquipmentTypeEnum.NOTIDENTITY;
	}

}
