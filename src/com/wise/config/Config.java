package com.wise.config;

public class Config {

	public static final String URL   = "http://www.muliatrack.com/wspub0/service.asmx/";
	
	public static  String BaseUrl    = URL;
	public static  String USER_LOGIN = BaseUrl + "Login_json";// �û���¼
	public static  String Register   = BaseUrl + "RegisterTrackMe_json";// ���ObjectId
	public static  String Check      = BaseUrl + "InsertEvent_json";//���°��
	public static  String GetObjectInfo    = BaseUrl + "GetObjectInfo_json"; //��ȡ��Ϣ
//	public static  String UPDATE_LOCATION  = BaseUrl + "UpdateGPSDate_json";// ����λ��
	/*2016-07-18   ������λ����Ϣ�ӿ� UpdateGPSDate2_json */
	public static  String UPDATE_LOCATION  = BaseUrl + "UpdateGPSDate2_json";// ����λ��
	
	public static  String UpdateObjectInfo = BaseUrl + "UpdateObjectInfo"; //������Ϣ
	
	/**
	 * ������Ϣ
	 */
	public static final String Shared_Preferences ="TrackMe";
	public static final String CopyRight = "Copyright @ 2016 WiseCar";
	public static final String UpdateTime = "Last Updated: 2016-10-28";
	public static final boolean ODG = true;
	/**
	 * gps��λ
	 */
	public static final String gps_flag = "2";
	/**
	 * wifi��λ
	 */
	public static final String wifi_flag = "1";
	
	public static int notification_id = 19134639;
}
