package com.hzcard.logtrace.spring.boot.handle.interceptor;

public enum EquipmentTypeEnum {
	NOTIDENTITY(null),  //无法识别
	IPHONEWEB(4), //iphone web-----4
	WINDOWNWEB(1), //windows web ---1
	IPADWEB(4),    //ipad web    ---4
	MACONTOSHIWEB(1), // mac web  ---1
	ANDROIDWEB(4),    //安卓 web   ---4
	IPHONENATIVE(2),  //iPhone 原生    ---2
	IPADNATIVE(2),    //ipad 原生         ---2
	ANROIDNATIVE(3),   //安卓  原生         ----3
	LINUXWEB(1);        //LINUX WEB   ---1
	
	private Integer clientType;
	
	private EquipmentTypeEnum(Integer clientType){
		this.clientType = clientType;
	}
	
	public Integer getClientType(){
		return this.clientType;
	}
	
}
