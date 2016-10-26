package com.wise.service;

import com.wise.config.Config;
import com.wise.trackme.activity.LocationActivity;
import com.wise.trackme.activity.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StartReceiver extends BroadcastReceiver{
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	int notification_id=19134639;
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(ACTION)){			
			//�ж�ʱ����Ҫ��λ����
			SharedPreferences preferences = context.getSharedPreferences(Config.Shared_Preferences, Context.MODE_PRIVATE);
			boolean onOrOff = preferences.getBoolean("static", false);
			if(onOrOff){
				System.out.println("��������");
				Intent startService = new Intent(context, LocationService.class);
				context.startService(startService);
				NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
				Notification notification = new Notification();
		    	notification.icon = R.drawable.ic_launcher;
		    	notification.tickerText = context.getString(R.string.app_name);
		    	notification.flags |= Notification.FLAG_ONGOING_EVENT;
		    	notification.defaults |= Notification.DEFAULT_SOUND;
		    	Intent notificationIntent =new Intent(context, LocationActivity.class); // �����֪ͨ��Ҫ��ת��Activity
		       	PendingIntent contentItent = PendingIntent.getActivity(context, 0, notificationIntent, 0);    	
		    	notification.setLatestEventInfo(context, context.getString(R.string.app_name), context.getString(R.string.service), contentItent);
		    	nm.notify(notification_id, notification);
			}
		}
	}
}
