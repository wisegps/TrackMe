package com.wise.config;

public class Config {

	public static final String URL   = "http://www.muliatrack.com/wspub0/service.asmx/";
	
	public static  String BaseUrl    = URL;
	public static  String USER_LOGIN = BaseUrl + "Login_json";// 用户登录
	public static  String Register   = BaseUrl + "RegisterTrackMe_json";// 获得ObjectId
	public static  String Check      = BaseUrl + "InsertEvent_json";//上下班打卡
	public static  String GetObjectInfo    = BaseUrl + "GetObjectInfo_json"; //获取信息
//	public static  String UPDATE_LOCATION  = BaseUrl + "UpdateGPSDate_json";// 更新位置
	/*2016-07-18   更改了位置信息接口 UpdateGPSDate2_json */
	public static  String UPDATE_LOCATION  = BaseUrl + "UpdateGPSDate2_json";// 更新位置
	
	public static  String UpdateObjectInfo = BaseUrl + "UpdateObjectInfo"; //更新信息
	
	/**
	 * 配置信息
	 */
	public static final String Shared_Preferences ="TrackMe";
	public static final String CopyRight = "Copyright @ 2016 WiseCar";
	public static final String UpdateTime = "Last Updated: 2016-10-28";
	public static final boolean ODG = true;
	/**
	 * gps定位
	 */
	public static final String gps_flag = "2";
	/**
	 * wifi定位
	 */
	public static final String wifi_flag = "1";
	
	public static int notification_id = 19134639;
}
