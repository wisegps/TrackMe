//package com.wise.core;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.IBinder;
//import android.util.Log;
//
//public class LoactionCoreService extends Service {
//
//	
//	private int TEN_MINUTES = 1000 * 10;// five minutes
//	
//	
//	@Override
//	public IBinder onBind(Intent intent) {
//		return null;
//	}
//	
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		
//		objHandler.postDelayed(mTasks, 1000);
//		
//		
//		Log.e("AAA", "��λ����" + "onCreate()");
//	}
//	
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.e("AAA", "��λ����" + "onStartCommand()");	
//		startAlarmService();
//		return super.onStartCommand(intent, flags, startId);
//	}
//	
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		objHandler.removeCallbacks(mTasks);
//		Log.e("AAA", "��λ����" + "onDestroy()");
//	}
//	
//	
//	/**   
//	 * @Description: ���͹㲥������ʱ����  
//	 * @param:       
//	 * @return: void       
//	 */  
//	private void startAlarmService(){
//		Intent alarm_service = new Intent("start_my_alarm_service");
//		sendBroadcast(alarm_service);
//	}
//	
//	
//	private Handler objHandler = new Handler();
//	
//	private Runnable mTasks = new Runnable(){
//		public void run(){
//			
//			Log.e("AAA", "��ʱ��ȡ��λ����------go spurs go --------");		
//			// ��Ӿ�����Ҫ�������飺
//			objHandler.postDelayed(mTasks, TEN_MINUTES);
//		}
//	};
//}
