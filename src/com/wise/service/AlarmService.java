package com.wise.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * @author Wu
 * 
 * 十分钟上传一次位置信息
 */
public class AlarmService extends Service{
	
	private int TEN_MINUTES = 1000 * 60 * 5;// five minutes
	
	private Handler objHandler = new Handler();
	
	private Runnable mTasks = new Runnable(){
		public void run(){
			Log.i("LocationService", "定时任务...............");		
			Intent intent = new Intent(LocationService.ACC_ON);
			sendBroadcast(intent);
			// 添加具体需要做的事情：
			objHandler.postDelayed(mTasks, TEN_MINUTES);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		objHandler.removeCallbacks(mTasks);
		objHandler.postDelayed(mTasks, 1000);
		Log.i("LocationService", "定时服务启动  onCreate:");
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.i("LocationService", "定时服务  onStartCommand:");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy(); 
		objHandler.removeCallbacks(mTasks);
		Log.i("LocationService", "定时服务          onDestroy:");
	}

}
