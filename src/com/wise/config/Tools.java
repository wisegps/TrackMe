package com.wise.config;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {
	
	public static String getCurrentTime(){
		SimpleDateFormat    formatter    =   new    SimpleDateFormat    ("yyyy-MM-dd HH:mm:ss");       
		Date    curDate    =   new    Date(System.currentTimeMillis());//��ȡ��ǰʱ��       
		String    str    =    formatter.format(curDate); 
		return str;
	}

}
