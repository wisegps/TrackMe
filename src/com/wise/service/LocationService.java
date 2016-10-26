package com.wise.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.wise.config.Config;
import com.wise.config.Msg;
import com.wise.config.Tools;
import com.wise.core.MyApplication1;
import com.wise.model.DialogDismissEvent;
import com.wise.model.LocationBuffer;
import com.wise.model.OpenGPSEvent;
import com.wise.model.UIUpdataEvent;
import com.wise.model.UploadBufferLocation;
import com.wise.trackme.activity.LoginActivity;

import de.greenrobot.event.EventBus;

import android.R;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service {
	
	private static final String TAG = "LocationService";
	private static final String CHECK_OUT = "com.wise.checkout";
	private static final String CHECK_In  = "com.wise.checkin";
	public static final String ACC_ON     = "com.wise.acc.on";
	
	public static final String UPLOAD_L_B     = "com.wise.upload_lb";
	
	private LocationManager locationManager;
	private LocationListner gpsListner = null;
	private LocationListner wifiListner = null;
	private UploadBroadCast ploadBroadCast;

	boolean isUpload_check    = false;
	private String flag     = ""; // ��λ��ʽ
	private String ObjectId = "";
	private WakeLock wakeLock;
	boolean isCheckOut = false;
	boolean isCheckIn  = false;
	private String StatusDes = "";
	private Location mLocation;
	
	private String lat  = "0";
	private String lon  = "0";
	
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	private boolean isFirstStrat = false;
	private boolean isLocationOk = false;
	
	
	private MyApplication1 app;
	
	
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		
		app = (MyApplication1)getApplication();
		
		initBro();
		
		
		// ��ȡϵͳ�Դ�google��λ�������
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		wifiListner = new LocationListner();
		gpsListner  = new LocationListner();
		
		editor = getSharedPreferences(Config.Shared_Preferences, MODE_PRIVATE).edit();
    	pref = getSharedPreferences(Config.Shared_Preferences, MODE_PRIVATE); 

		Log.i(TAG, "LocationService onCreate");
		PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
				.getClass().getCanonicalName());
		wakeLock.acquire();
		ploadBroadCast = new UploadBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CHECK_OUT);
		filter.addAction(CHECK_In);
		filter.addAction(ACC_ON);
		
		filter.addAction(UPLOAD_L_B);
		
		registerReceiver(ploadBroadCast, filter);
		
		SharedPreferences preferences = getSharedPreferences(Config.Shared_Preferences, Context.MODE_PRIVATE);
		ObjectId = preferences.getString("ObjectId", "");
		
		Log.e("TEST_BUG", "����ȡ��id    �� " + ObjectId);
		
		// ��λ
		locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,1000 * 30, 0, wifiListner);
		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,1000 * 30, 0, gpsListner);		
	}

	/**   
	 * @Description: ���͹㲥������ʱ����  
	 * @param:       
	 * @return: void       
	 */  
	private void startAlarmService(){
		Intent alarm_service = new Intent("start_my_alarm_service");
		sendBroadcast(alarm_service);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		isFirstStrat = intent.getBooleanExtra("isFirstStart",false);
		Log.i(TAG, "�Ƿ��һ����������--------------------------------------------------" + " " + isFirstStrat);
		Test("onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * @author Wu
	 * 
	 * λ�ñ仯������
	 */
	private class LocationListner implements LocationListener {
		public void onLocationChanged(Location location) {
			Log.i(TAG, "��λ�ɹ�------------------:" + location.getProvider() + "   ״̬����" +  StatusDes);	
			isLocationOk = true;
			if (location.getProvider().equals("gps")) {
				flag = Config.gps_flag;
				// gps ��λ��ɾ��wifi��λ
				if (wifiListner != null) {
					locationManager.removeUpdates(wifiListner);
					wifiListner = null;
				}
			} else {
				flag = Config.wifi_flag;
			}
			mLocation = location;
			Log.i(TAG, ObjectId 
					+ "\n" + String.valueOf(mLocation.getLatitude()) 
					+ "\n" + String.valueOf(mLocation.getLongitude()) 
					+ "\n" + flag 
					+ "\n" + StatusDes);
//			updataLatLon(mLocation);
			editor.putString("lat", String.valueOf(location.getLatitude()));
			editor.putString("lon", String.valueOf(location.getLongitude()));
			editor.putString("gps_flag", flag);
			editor.commit();
			if (isUpload_check) {
				Log.d(TAG, "isUpload == " + isUpload_check );
				isUpload_check = false;
				updateLocation(mLocation, flag);
			}
			if(isFirstStrat){
				isFirstStrat = false;	
				startAlarmService();//��λ�ɹ�֮������ʱ�ύ��������
				
//				Intent intent_start_alarm_service = new Intent("alarm_service");
//				intent_start_alarm_service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				sendBroadcast(intent_start_alarm_service);
			}
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
		public void onProviderDisabled(String provider) {}
	};

	/**
	 * ���͹㲥 ����ҳ��ȥǩ��
	 * 
	 * @param location
	 * @param gps_flag
	 */
	public void updateLocation(Location location, String gps_flag) {
		if (location != null) {
			Intent intent = new Intent("location");
			intent.putExtra("lat", String.valueOf(location.getLatitude()));
			intent.putExtra("lon", String.valueOf(location.getLongitude()));
			intent.putExtra("gps_flag", gps_flag);
			intent.putExtra("isCheckOut", isCheckOut);
			intent.putExtra("isCheckIn", isCheckIn);
			sendBroadcast(intent);
			if(isCheckOut || isCheckIn){
				isCheckOut = false;
				isCheckIn  = false;
				Test("updateLocation");
			}
		} 
	}
	
//	/**
//	 * @param location
//	 */
//	private void updataLatLon(Location location){
//		if (location != null) {
//			Intent intent = new Intent("latlon");
//			intent.putExtra("lat", String.valueOf(location.getLatitude()));
//			intent.putExtra("lon", String.valueOf(location.getLongitude()));
//			sendBroadcast(intent);
//			Log.e("LocationService", "����֪ͨ����ҳ���¾�γ�ȣ�");
//		}
//	}
	
	
	/**
	 * Handler ��Ϣ����
	 */
	private Handler mHandler = new Handler(){
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
			case Msg.UPDATA_LOCATION:		
				
				if(msg.obj.toString().contains("Exception")){
					Toast.makeText(LocationService.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
				}else{
					Log.i(TAG, "ÿ������ӽ��ύ���� ������Ϣ��2222222222�� :" + msg.obj.toString());
					
					if(mLocation != null){
						new Thread(new NetThread.GetLocation(String .valueOf(mLocation.getLatitude()),String .valueOf(mLocation.getLongitude()),
								mHandler,Msg.GET_FIVE_LOACTION, LocationService.this)).start();
					}else {
						new Thread(new NetThread.GetLocation(lat,lon,mHandler,Msg.GET_FIVE_LOACTION, LocationService.this)).start();
					}
				}
				break;
			case Msg.GET_FIVE_LOACTION:
				if(msg.obj.toString().contains("Exception")){
					Toast.makeText(LocationService.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
				}else{
					editor.putString("detailLocation", msg.obj.toString());
	            	editor.commit();
					Log.i("LocationService", "����Ӹ���һ�ε���λ�ã�333333333����" + msg.obj.toString());
					if(mLocation != null){
						EventBus.getDefault().post(new UIUpdataEvent(msg.obj.toString(),mLocation)); 
					}else {
						EventBus.getDefault().post(new UIUpdataEvent(msg.obj.toString(),lat,lon)); 
					}
					
					// 2016-06-21 �ύû�����绺�������//
					if(isLoadlb){
						upload_lb ++ ;
						if(upload_lb<app.locationBuffer.size()){
							Intent lb = new Intent(LocationService.UPLOAD_L_B);
		        			sendBroadcast(lb);
						}else {
							app.locationBuffer.clear();
							upload_lb = 0;
							isLoadlb = false;
							Log.e(TAG, "�ύ���````````````````````");
						}
					}
				}
				break;
			}	
		};
	};
	

	/**
	 * @author Wu
	 *
	 * �㲥������
	 */
	class UploadBroadCast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(!LoginActivity.isGPSOpen(LocationService.this)){
				
				EventBus.getDefault().post(new OpenGPSEvent("open_gps"));
				
			}else {
				if (intent.getAction().equals(CHECK_OUT)) {				
					isUpload_check = intent.getBooleanExtra("isUpload", false);				
					Log.i(TAG, "�°� ��" + isUpload_check);
					isCheckOut = true;
					isCheckIn = false;
					StatusDes = "ACC ON, Check Out";				
					// ��λ�°�
					if (gpsListner != null) {
						locationManager.removeUpdates(gpsListner);
						gpsListner = null;
					}
					if (wifiListner != null) {
						locationManager.removeUpdates(wifiListner);
						wifiListner = null;
					}
					gpsListner = new LocationListner();
					wifiListner = new LocationListner();
					locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,1000 * 30 , 0, gpsListner);
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000 * 30 , 0, wifiListner);
					Test("UploadBroadCast");
				}else if(intent.getAction().equals(CHECK_In)){
					//�����λ���ɹ�����ʧ��ʾ����ʾ��λʧ��
					if(!isLocationOk){
						EventBus.getDefault().post(new DialogDismissEvent("L_ERROE"));
						return;
					}
					
					isUpload_check = intent.getBooleanExtra("isUpload", false);
					Log.i(TAG, "�ϰ� ��" + isUpload_check);
					isCheckIn = true;
					isCheckOut = false;
					StatusDes = "ACC ON, Check In";
					// ��λ�ϰ�
					if (gpsListner != null) {
						locationManager.removeUpdates(gpsListner);
						gpsListner = null;
					}
					if (wifiListner != null) {
						locationManager.removeUpdates(wifiListner);
						wifiListner = null;
					}
					gpsListner = new LocationListner();
					wifiListner = new LocationListner();
					locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,1000 * 30, 0, gpsListner);
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000 * 30, 0, wifiListner);
					Test("UploadBroadCast");
				}else if(intent.getAction().equals(ACC_ON)){
					Log.d(TAG, "ÿ������ӽ��յ�һ�ι㲥 ��111111��  :Acc  on  " + mLocation);
					// ������յ��Ĺ㲥�� ÿ������ӽ��յ�һ�ι㲥ȥ���¿ͻ�����λ����Ϣ  ACC ON app��½�Ϳ�ʼÿ������Ӹ���һ��	
					
					if(isNetworkAvailable(context)){
						if(mLocation != null){
							List<NameValuePair> params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("ObjectID", ObjectId));
							params.add(new BasicNameValuePair("Lat", String .valueOf(mLocation.getLatitude())));
							params.add(new BasicNameValuePair("Lon", String .valueOf(mLocation.getLongitude())));
							params.add(new BasicNameValuePair("GPSFlag", flag));
							params.add(new BasicNameValuePair("StatusDes", "ACC ON"));
							/*2016-07-18   ������λ����Ϣ�ӿ� UpdateGPSDate2_json  ������ϴ�ʱ�����*/
							params.add(new BasicNameValuePair("GPSTime", Tools.getCurrentTime()));
							
							Log.e(TAG, "�ύʱ�� v   " + Tools.getCurrentTime());
							
							new Thread(new NetThread.postDataThread(mHandler,Config.UPDATE_LOCATION, params,Msg.UPDATA_LOCATION)).start();
						}else {
							lat  = pref.getString("lat", "0");//��һ�ε�����
							lon  = pref.getString("lon", "0");//��һ�ε�����
							List<NameValuePair> params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("ObjectID", ObjectId));
							params.add(new BasicNameValuePair("Lat", lat));
							params.add(new BasicNameValuePair("Lon", lon));
							params.add(new BasicNameValuePair("GPSFlag", flag));
							params.add(new BasicNameValuePair("StatusDes", "ACC ON"));
							/*2016-07-18   ������λ����Ϣ�ӿ� UpdateGPSDate2_json  ������ϴ�ʱ�����*/
							params.add(new BasicNameValuePair("GPSTime", Tools.getCurrentTime()));
							new Thread(new NetThread.postDataThread(mHandler,Config.UPDATE_LOCATION, params,Msg.UPDATA_LOCATION)).start();
						}
					}else{
						if(mLocation != null){
							 LocationBuffer lb = new LocationBuffer();
							 lb.setGPSFlag(flag);
							 lb.setLat(String .valueOf(mLocation.getLatitude()));
							 lb.setLon(String .valueOf(mLocation.getLongitude()));
							 lb.setGPSTime(Tools.getCurrentTime());
							 app.locationBuffer.add(lb);
							 Log.e(TAG, "�л��������� 11111111");
						}else{
							 LocationBuffer lb = new LocationBuffer();
							 lb.setGPSFlag(flag);
							 lb.setLat(lat);
							 lb.setLon(lon);
							 lb.setGPSTime(Tools.getCurrentTime());
							 app.locationBuffer.add(lb);
							 Log.e(TAG, "�л��������� 22222");
						}	
					}	
				}else if(intent.getAction().equals(UPLOAD_L_B)){
					
					if(upload_lb < app.locationBuffer.size()){
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("ObjectID", ObjectId));
						params.add(new BasicNameValuePair("Lat", app.locationBuffer.get(upload_lb).getLat()));
						params.add(new BasicNameValuePair("Lon", app.locationBuffer.get(upload_lb).getLon()));
						params.add(new BasicNameValuePair("GPSFlag", app.locationBuffer.get(upload_lb).getGPSFlag()));
						params.add(new BasicNameValuePair("StatusDes", "ACC ON"));
						/*2016-07-18   ������λ����Ϣ�ӿ� UpdateGPSDate2_json  ������ϴ�ʱ�����*/
						params.add(new BasicNameValuePair("GPSTime", app.locationBuffer.get(upload_lb).getGPSTime()));
						
						new Thread(new NetThread.postDataThread(mHandler,Config.UPDATE_LOCATION, params,Msg.UPDATA_LOCATION)).start();
						
						Log.e(TAG, "�ύ��������    ++++++++++++++++ ");
					}
					
				}
			}	
		}
	}
	
	int upload_lb = 0;
	boolean isLoadlb = false;
	
	private void Test(String tag){
		Log.i(TAG, tag + "=======" + "isUpload = " + isUpload_check + " , isCheckOut = " + isCheckOut
				+ " , isCheckIn = " + isCheckIn);
	}
	
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "LocationService  onDestroy()" );
		try {
			Test("onDestroy");
			if (ploadBroadCast != null) {
				unregisterReceiver(ploadBroadCast);
			}
			if (gpsListner != null) {
				locationManager.removeUpdates(gpsListner);
				gpsListner = null;
			}
			if (wifiListner != null) {
				locationManager.removeUpdates(wifiListner);
				wifiListner = null;
			}
			wakeLock.release();
			wakeLock = null;
			int notification_id = 19134639;
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			nManager.cancel(notification_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		app.locationBuffer.clear();
		unregisterReceiver(networkChangeReceiver);

	}
	
	
	
	/**
     * �������
     */
    class NetworkChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isAvailable()){
            	if(app.locationBuffer.size()>0){
            		isLoadlb = true;
            		Intent lb = new Intent(LocationService.UPLOAD_L_B);
        			sendBroadcast(lb);
            	}
            	
            }else{
                Toast.makeText(LocationService.this,"Network is unavailable!",Toast.LENGTH_SHORT).show();
            }
        }
    }
    
  
    
    
    
    
    private IntentFilter filter;/** ����ϵͳ����仯�㲥 */
    private NetworkChangeReceiver networkChangeReceiver;

    
    /**
     * ��ʼ���㲥
     */
    private void initBro() {
        filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver,filter);
    }


	
    /**
     * ��鵱ǰ�����Ƿ����
     * @param activity
     * @return
     */

    public static boolean isNetworkAvailable(Context activity){
        Context context = activity.getApplicationContext();
        // ��ȡ�ֻ��������ӹ�����󣨰�����wi-fi,net�����ӵĹ���
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null){
            return false;
        } else{
            // ��ȡNetworkInfo����
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0){
                for (int i = 0; i < networkInfo.length; i++){
                    // �жϵ�ǰ����״̬�Ƿ�Ϊ����״̬
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }
	
	
}