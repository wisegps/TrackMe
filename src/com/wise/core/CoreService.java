package com.wise.core;

import com.wise.service.AlarmService;
import com.wise.service.LocationService;
import com.wise.trackme.activity.LocationActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class CoreService extends Service {

	private int TEN_MINUTES = 1000 * 5;// five 
	private ServiceBroadcast mServiceBroadcast;
	private boolean isStop = false;
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		pref = getSharedPreferences("trackme", MODE_PRIVATE); 
		editor = getSharedPreferences("trackme", MODE_PRIVATE).edit();
		isStop = pref.getBoolean("isStop_location_alarm", false);
		// 注册广播接收类
		mServiceBroadcast = new ServiceBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("start_my_location_service");
		filter.addAction("start_my_alarm_service");
		filter.addAction("stop_service");
		registerReceiver(mServiceBroadcast, filter);
		objHandler.postDelayed(mTasks, 1000);
		Log.i("LocationService", "核心服务：" + "onCreate()");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.i("LocationService", "核心服务：" + "onStartCommand()--" + isStop);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		objHandler.removeCallbacks(mTasks);
		Log.i("LocationService", "核心服务：" + "onDestroy()");
	}
	
	
	
	/**   
	 * @Description:发送广播启动定位服务  
	 * @param:       
	 * @return: void       
	 */  
	private void startLocationService(){
		Intent location_service = new Intent("start_my_location_service");
		sendBroadcast(location_service);
	}
	
	
	/**   
	 * @ClassName:  ServiceBroadcast   
	 * @Description:广播接收器  
	 * @author: Android_Robot  
	 * @date:   2016-4-28 下午5:41:15   
	 *      
	 */
	class ServiceBroadcast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals("start_my_location_service")){
				Intent location_service = new Intent(context, LocationService.class);    
		    	location_service.putExtra("isFirstStart", true);
				startService(location_service);// 启动service获取位置
			}else if(intent.getAction().equals("start_my_alarm_service")){
				Intent intent_5_minutes_updata_service = new Intent(context, AlarmService.class);
				startService(intent_5_minutes_updata_service);
			}else if(intent.getAction().equals("stop_service")){
				isStop = false;
				Log.i("LocationService", "0000000000000--" + isStop);
				objHandler.postDelayed(mTasks, 1000);
			}
		}	
	}
	
	private Handler objHandler = new Handler();
	
	private Runnable mTasks = new Runnable(){
		public void run(){
			Log.i("LocationService", "核心服务心跳包！！！！！！！！---" + isStop );
			if(!isStop){
				startLocationService();//发送广播启动定位服务
			}	
		}
	};	

}
