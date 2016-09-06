package com.hzcard.logtrace.spring.boot.handle.interceptor;

public enum EquipmentTypeEnum {
	NOTIDENTITY(null),  //�޷�ʶ��
	IPHONEWEB(4), //iphone web-----4
	WINDOWNWEB(1), //windows web ---1
	IPADWEB(4),    //ipad web    ---4
	MACONTOSHIWEB(1), // mac web  ---1
	ANDROIDWEB(4),    //��׿ web   ---4
	IPHONENATIVE(2),  //iPhone ԭ��    ---2
	IPADNATIVE(2),    //ipad ԭ��         ---2
	ANROIDNATIVE(3),   //��׿  ԭ��         ----3
	LINUXWEB(1);        //LINUX WEB   ---1
	
	private Integer clientType;
	
	private EquipmentTypeEnum(Integer clientType){
		this.clientType = clientType;
	}
	
	public Integer getClientType(){
		return this.clientType;
	}
	
}
