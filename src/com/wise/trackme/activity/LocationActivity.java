package com.wise.trackme.activity;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.config.Config;
import com.wise.config.Msg;
import com.wise.config.Tools;
import com.wise.core.CoreService;
import com.wise.crash.AppManager;
import com.wise.model.DialogDismissEvent;
import com.wise.model.FiveMinutesEvent;
import com.wise.model.OpenGPSEvent;
import com.wise.model.UIUpdataEvent;
import com.wise.model.UploadBufferLocation;
import com.wise.service.LocationService;
import com.wise.service.NetThread;
import com.wise.service.AlarmService;
import com.wise.service.NetThread.postDataThread;

import de.greenrobot.event.EventBus;

public class LocationActivity extends Activity {
	
	public static final String CHECK_OUT = "com.wise.checkout";
	public static final String CHECK_In = "com.wise.checkin";

	private ProgressDialog Dialog;//��ʾ��
	private TextView tv_location, tv_time, tv_device_name,tv_coordinates;
	private NotificationManager nm;
	private Notification notification;

	private String lat  = "";
	private String lon  = "";
	private String flag = "";// ��λ״̬
	private MyBroadCast myBroadCast;
	private String ObjectId = "";
	// �洢�����λ��
	private String detailLocation = "";
	private String ObjectRegNum   ="";
	boolean isSatellite = true;
	private AlertDialog.Builder dialog_gps = null;
	
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*ActionBar actionBar = getActionBar();  
		actionBar.setDisplayHomeAsUpEnabled(true); */ 
		setContentView(R.layout.activity_location);

		AppManager.getAppManager().addActivity(this); 

		editor = getSharedPreferences(Config.Shared_Preferences, MODE_PRIVATE).edit();
    	pref = getSharedPreferences(Config.Shared_Preferences, MODE_PRIVATE); 
		
    	editor.putBoolean("isLogout", false);
		editor.commit();
		
//    	Intent location_service = new Intent(LocationActivity.this, LocationService.class);    	
//    	location_service.setAction("start_location_service");
//    	location_service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    	location_service.putExtra("isFirstStart", true);
//		startService(location_service);// ����service��ȡλ��
		
		//ע��EventBus  
      	EventBus.getDefault().register(this);  
		init();
		
		if(!isServiceRunning(LocationActivity.this,"com.wise.service.LocationService")){
			//�������ķ��� һֱ����
    		startCoreService();
        }
	
	}

	 /**   
     * @Description: �жϷ����Ƿ������� 
     * @param: @param mContext
     * @param: @param className �ǰ���+���������
     * @param: @return      
     * @return: boolean       
     */  
    public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        			mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(1000);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
	
	/**
	 * �������ĳ�פ����
	 */
	private void startCoreService(){
		SharedPreferences.Editor editor = getSharedPreferences("trackme", MODE_PRIVATE).edit();
		SharedPreferences pref = getSharedPreferences("trackme", MODE_PRIVATE); 
        
		editor.putBoolean("isStop_location_alarm", false);
    	editor.commit();
    	/** 
         * ����Service����Service��Activity��������Ȼ���� 
         */  
    	startService(new Intent(LocationActivity.this, CoreService.class));
    	sendBroadcast(new Intent("stop_service"));

	}
	
	
	
	
	/**
	 * @param event
	 */
	public void onEventMainThread(UIUpdataEvent event) {
		if(!TextUtils.isEmpty(event.getDetailLocation())){
			tv_location.setText(event.getDetailLocation());
		}
		
		tv_time.setText(getTime());
		tv_coordinates.setText( "Lat : " + event.getLat() + "\n" + "Lon: " + event.getLon());
		tv_device_name.setText(ObjectRegNum);
		
	}
	
	/**
	 * @param event
	 */
	public void onEventMainThread(FiveMinutesEvent event) {
		tv_time.setText(getTime());
		tv_coordinates.setText( "Lat : " + event.getLocation().getLatitude() + "\n" + "Lon: " + event.getLocation().getLongitude());
	}
	
	
	/**
	 * @param event 
	 */
	public void onEventMainThread(DialogDismissEvent event) {
		
		if("L_ERROE".equals(event.getmMsg())){
			Toast.makeText(getApplicationContext(), "Can Not Get Location", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(), event.getmMsg(), Toast.LENGTH_SHORT).show();
		}
		
		if (Dialog != null) {
			Dialog.dismiss();
		}
	}
	
	
	/**
	 * @param event
	 */
	public void onEventMainThread(OpenGPSEvent event) {  		
		if(dialog_gps ==null){
			dialog_gps = new AlertDialog.Builder(LocationActivity.this);
			dialog_gps.setTitle(getResources().getString(R.string.prompt));
			dialog_gps.setMessage(getResources().getString(R.string.open_gps));
			dialog_gps.setPositiveButton(getResources().getString(R.string.ok),
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// ת���ֻ����ý��棬�û�����GPS
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, 5); // ������ɺ󷵻ص�ԭ���Ľ���
						}
					});
			dialog_gps.setNeutralButton(getResources().getString(R.string.cancel), new android.content.DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
					dialog_gps = null;
				}
			} );
			dialog_gps.show();
		}
	}
	
	
	
	
	/**
	 * ����¼�����
	 */
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!LoginActivity.isGPSOpen(LocationActivity.this)){
				dialog_gps = new AlertDialog.Builder(LocationActivity.this);
				dialog_gps.setTitle(getResources().getString(R.string.prompt));
				dialog_gps.setMessage(getResources().getString(R.string.open_gps));
				dialog_gps.setPositiveButton(getResources().getString(R.string.ok),
						new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// ת���ֻ����ý��棬�û�����GPS
								Intent intent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(intent, 5); // ������ɺ󷵻ص�ԭ���Ľ���
							}
						});
				
				dialog_gps.setNeutralButton(getResources().getString(R.string.cancel), new android.content.DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				} );
				dialog_gps.show();
			}else if(!isConnected(LocationActivity.this)){
				Toast.makeText(LocationActivity.this,
						getString(R.string.network_wrong), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			else{
				switch (v.getId()) {
				case R.id.bt_on_work:
					AlertDialog.Builder bulder1 = new AlertDialog.Builder(
							LocationActivity.this);
					bulder1.setTitle(R.string.prompt);// ���ñ���
					bulder1.setMessage(R.string.acquire_gps);
					bulder1.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									try {
										Intent intent = new Intent(CHECK_In);
										intent.putExtra("isUpload", true);
										sendBroadcast(intent);
										Dialog = ProgressDialog
												.show(LocationActivity.this,"",getString(R.string.in_commit),true);
										Dialog.setCancelable(false);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
					bulder1.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Dialog = ProgressDialog.show(
											LocationActivity.this,"",getString(R.string.in_commit), true);
									Dialog.setCancelable(false);
									onWork();
								}
							});
					bulder1.show();
					break;
				case R.id.bt_off_work:
					Dialog = ProgressDialog.show( LocationActivity.this,"",getString(R.string.in_commit), true);
					Dialog.setCancelable(false);
					offWork();
					break;
				}
			}
		}
	};
	
	
	/**
	 * Handler ��Ϣ����
	 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Msg.CHECK_IN:
				checkInResult(msg.obj.toString());
				break;
			case Msg.CHECK_OUT:
				checkOutResult(msg.obj.toString());
				break;
			case Msg.GET_LOCATION:				
				detailLocation = (String) msg.obj;
				tv_location.setText(detailLocation);
				tv_time.setText(getTime());
				tv_coordinates.setText( "Lat : " + lat + "\n" + "Lon: " + lon);
				tv_device_name.setText(ObjectRegNum);

				editor.putString("detailLocation", detailLocation);
            	editor.commit();
				
				break;
			case Msg.GET_DATA:
				try {
					String result = msg.obj.toString();
					JSONObject jsonObject = new JSONObject(result.substring(1,result.length() - 2));
					ObjectRegNum = jsonObject.getString("ObjectRegNum");
					tv_device_name.setText(ObjectRegNum);
					editor.putString("nick_name", ObjectRegNum);
                	editor.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case Msg.UPDATA_LOCATION_AND_CHECK:
				Log.e("LocationService", "ǩ�����Ҹ���λ�� : " + msg.obj.toString());
				if(msg.obj.toString().contains("Exception")){
					EventBus.getDefault().post(new DialogDismissEvent("Exception")); 
				}else{
					onWork();
				}
				break;
			}
		}
	};

	/**
	 * onWork
	 */
	public void onWork() {
		if (lat.equals("")) {
			Toast.makeText(LocationActivity.this, R.string.location_error,
					Toast.LENGTH_SHORT).show();
			if (Dialog != null) {
				Dialog.dismiss();
			}
			return;
		} else {	
			workMethod(1);
		}
	}
	
	
	/**
	 * ��ʱ���Ƿ��ύ����λ��
	 */
	private void isUpdataLocation(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ObjectID", ObjectId));
		params.add(new BasicNameValuePair("Lat", lat));
		params.add(new BasicNameValuePair("Lon", lon));
		params.add(new BasicNameValuePair("GPSFlag", flag));
		params.add(new BasicNameValuePair("StatusDes", "ACC ON, Check In"));
		/*2016-07-18   ������λ����Ϣ�ӿ� UpdateGPSDate2_json  ������ϴ�ʱ�����*/
		params.add(new BasicNameValuePair("GPSTime", Tools.getCurrentTime()));
		
		
		// ���¿ͻ�λ��
		new Thread(new NetThread.postDataThread(handler,Config.UPDATE_LOCATION, params, 
				Msg.UPDATA_LOCATION_AND_CHECK)).start();
	}

	/**
	 * offWork
	 */
	private void offWork() {
		if (lat == null || lon == null || "".equals(lat) || "".equals(lon)) {
			Toast.makeText(LocationActivity.this, R.string.location_error,
					Toast.LENGTH_SHORT).show();
			if (Dialog != null) {
				Dialog.dismiss();
			}
			return;
		} else {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("ObjectID", ObjectId));
			params.add(new BasicNameValuePair("EventType", "1"));
			// ���°�
			params.add(new BasicNameValuePair("EventStatus", "0"));
			params.add(new BasicNameValuePair("Lat", lat));
			params.add(new BasicNameValuePair("Lon", lon));
			params.add(new BasicNameValuePair("GPSFlag", flag));
			params.add(new BasicNameValuePair("StatusDes", "ACC On, Check Out"));
			new Thread(new postDataThread(handler, Config.Check, params, Msg.CHECK_OUT)).start();
		}
	}

	/**
	 * @param state
	 */
	public void workMethod(int state) {
		if (lat == null || lon == null || "".equals(lat) || "".equals(lon)) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.cannot_find), Toast.LENGTH_SHORT).show();
		} else {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("ObjectID", ObjectId));
			params.add(new BasicNameValuePair("EventType", "1"));
			// ���°�
			params.add(new BasicNameValuePair("EventStatus", state + ""));
			params.add(new BasicNameValuePair("Lat", lat));
			params.add(new BasicNameValuePair("Lon", lon));
			params.add(new BasicNameValuePair("GPSFlag", flag));
			new Thread(new postDataThread(handler, Config.Check, params, Msg.CHECK_IN)).start();
		}
	}

	/**
	 * �ж��Ƿ�ɹ�
	 * 
	 * @param str
	 */
	private void checkInResult(String str) {
		if (Dialog != null) {
			Dialog.dismiss();
		}
		if ("(0);".equals(str)) {
			//��ȡ�����λ��
			new Thread(new NetThread.GetLocation(lat, lon, handler,
					Msg.GET_LOCATION, LocationActivity.this)).start();
			Toast.makeText(LocationActivity.this, getString(R.string.commit_success), Toast.LENGTH_SHORT) .show();
		} else if ((str).indexOf("Exception") != -1) {
			Toast.makeText(
					LocationActivity.this,
					getString(R.string.commit_fail) + ","+ getString(R.string.try_again), Toast.LENGTH_SHORT)
					.show();
		}
	}

	
	
	/**
	 * @param str
	 */
	private void checkOutResult(String str) {
		if (Dialog != null) {
			Dialog.dismiss();
		}
		if ("(0);".equals(str)) {
			Toast.makeText(LocationActivity.this,getString(R.string.commit_success), Toast.LENGTH_SHORT) .show();
		}else{
			Toast.makeText(LocationActivity.this, getString(R.string.commit_fail) + ","+  getString(R.string.try_again), 
					Toast.LENGTH_SHORT);
		}
	}

	
	/**
	 * @author W
	 * 
	 * ��ҳ����֪ͨ ��ȡ��������λ�� ����ui
	 */
	class MyBroadCast extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("location")) {
				lat = intent.getStringExtra("lat");
				lon = intent.getStringExtra("lon");
				flag = intent.getStringExtra("gps_flag");
				boolean isCheckOut = intent.getBooleanExtra("isCheckOut", false);
				boolean isCheckIn = intent.getBooleanExtra("isCheckIn", false);
				Log.d("LocationService", "��ҳ�յ�֪ͨ====ǩ�����ύ����λ��====��" 
						+ "\n" + lat 
						+ "\n" + lon
						+ "\n" + flag 
						+ "\n" + "isCheckIn��" + isCheckIn
						+ "\n" + "isCheckOut��" + isCheckOut);
				new Thread(new NetThread.GetLocation(lat, lon, handler,Msg.GET_LOCATION, LocationActivity.this)).start();
//				if (isCheckOut) {
//					offWork();
//				}
	
				if(isCheckIn){
					Log.d("LocationService", "��ʼǩ��---------�ϰ�--------------------------��" );
					isUpdataLocation();
				}
			} else if (intent.getAction().equals("change_name")) {
				tv_device_name.setText(intent.getStringExtra("name"));
				editor.putString("nick_name",intent.getStringExtra("name"));
            	editor.commit();
			}
//            	else if(intent.getAction().equals("latlon")){
//				lat = intent.getStringExtra("lat");
//				lon = intent.getStringExtra("lon");
//				
////				Log.e("LocationService", "��ҳ�յ�֪ͨ==========��" 
////						+ "\n" + lat 
////						+ "\n" + lon);
//			}
//			else if(intent.getAction().equals("alarm_service")){
//				Intent intent_5_minutes_updata_service = new Intent(LocationActivity.this, AlarmService.class);
//				startService(intent_5_minutes_updata_service);
//			}
			
			
		}
	}

	protected boolean isRouteDisplayed() {
		return false;
	}


	protected void onDestroy() {
		if (myBroadCast != null) {
			unregisterReceiver(myBroadCast);
		}
		SharedPreferences preferences = getSharedPreferences(Config.Shared_Preferences, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.commit();
		super.onDestroy();
	}

	/**
	 * ��γ��ת��
	 * 
	 * @param str
	 * @return
	 */
	public static int StringToInt(String str) {
		Double point_doub = Double.parseDouble(str);
		return (int) (point_doub * 1000000);
	}
	
	/**�������η���*/
//	private long exitTime;
	
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)   
    {  
         if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)  
         {          
//             if((System.currentTimeMillis()-exitTime) > 2000)  //System.currentTimeMillis()���ۺ�ʱ���ã��϶�����2000  
//             {  
//              Toast.makeText(getApplicationContext(), R.string.press_again,Toast.LENGTH_SHORT).show();                                  
//              exitTime = System.currentTimeMillis();  
//             }  
//             else{  

//           }           
        	 startForegroundRun();
           return true;  
         }  
         return super.onKeyDown(keyCode, event);  
    }  
	

	// ��������ת��Ϊʱ���
	public String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		return str;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();  
        inflater.inflate(R.menu.main, menu);   
        return super.onCreateOptionsMenu(menu);
	}
	
	 /**
     * Overflow ����ʾͼ��
     * */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
    	// TODO Auto-generated method stub
    	if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {  
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {  
                try {  
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);  
                    m.setAccessible(true);  
                    m.invoke(menu, true);  
                } catch (Exception e) {  
                }  
            }  
        }  
    	return super.onMenuOpened(featureId, menu);
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Log.e("123", ObjectId);
			Intent intent = new Intent(LocationActivity.this,
					ConfigureActivity.class);
			intent.putExtra("ObjectId", ObjectId);
			startActivity(intent);
			break;
			
		case R.id.action_logout:
			editor.putBoolean("isLogout", true);
			editor.commit();
			stopService();
			
			LocationActivity.this.finish();
			break;
		/*case android.R.id.home:
			startForegroundRun();
			break;*/
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	
	/**
	 * ֹͣ  ��λ�Ͷ�ʱ�ύ���ݷ���
	 */
	public void stopService(){
    	stopService(new Intent(LocationActivity.this, LocationService.class)); 
    	stopService(new Intent(LocationActivity.this, AlarmService.class)); 
    	SharedPreferences.Editor editor = getSharedPreferences("trackme", MODE_PRIVATE).edit();
		SharedPreferences pref = getSharedPreferences("trackme", MODE_PRIVATE); 
    	editor.putBoolean("isStop_location_alarm", true);
    	editor.commit();
    }


	/**
	 * ��ʼ��
	 */
	private void init() {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		tv_location = (TextView) findViewById(R.id.tv_location);
		tv_time = (TextView) findViewById(R.id.tv_time);
		Button bt_on_work = (Button) findViewById(R.id.bt_on_work);
		bt_on_work.setOnClickListener(onClickListener);
		Button bt_off_work = (Button) findViewById(R.id.bt_off_work);
		bt_off_work.setOnClickListener(onClickListener);
		tv_device_name = (TextView) findViewById(R.id.tv_device_name);
		tv_coordinates = (TextView)findViewById(R.id.tv_coordinates);

		detailLocation = pref.getString("detailLocation", "");
		ObjectId       = pref.getString("ObjectId", "");
		Log.e("TEST_BUG", "ȡ��id    �� " + ObjectId);
		flag = pref.getString("gps_flag", Config.gps_flag);
		lat  = pref.getString("lat", "");
		lon  = pref.getString("lon", "");
		ObjectRegNum = pref.getString("nick_name", "");
		tv_device_name.setText(ObjectRegNum);
//		tv_location.setText(detailLocation);
		tv_time.setText(getTime());
//		tv_coordinates.setText( "Lat : " + lat + "\n" + "Lon: " + lon);
		if (!"".equals(lat) || !"".equals(lon)) {
			new Thread(new NetThread.GetLocation(lat, lon, handler,Msg.GET_LOCATION, LocationActivity.this)).start();
		}
		// ע��㲥������
		myBroadCast = new MyBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("location");
		filter.addAction("change_name");
		filter.addAction("latlon");
		filter.addAction("alarm_service");
		registerReceiver(myBroadCast, filter);
		
		
		// ��ȡ�˺���Ϣ
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ObjectID", ObjectId));
		new Thread(new NetThread.postDataThread(handler, Config.GetObjectInfo,params, Msg.GET_DATA)).start();
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// ��ȡ�˺���Ϣ
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ObjectID", ObjectId));
		new Thread(new NetThread.postDataThread(handler, Config.GetObjectInfo,
				params, Msg.GET_DATA)).start();
	}
	
	
	/**
	 * �ж������Ƿ�����
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context){
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null != connectivity) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (null != info && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	dialog_gps = null;
    	if(requestCode == 5){
    		if(!LoginActivity.isGPSOpen(LocationActivity.this)){
    			
    			Toast.makeText(LocationActivity.this,getResources().getString(R.string.open_gps_fail), Toast.LENGTH_SHORT).show();
    		}else{
    			Toast.makeText(LocationActivity.this,getResources().getString(R.string.open_gps_success), Toast.LENGTH_SHORT).show();
    		}	
    	}	
    }
    
    
    private void startForegroundRun(){
    	if(notification == null){
    		notification = new Notification();
    	}else{
    		notification.icon = R.drawable.ic_launcher;
    		notification.tickerText = getString(R.string.app_name);
    		notification.flags |= Notification.FLAG_ONGOING_EVENT;
    		notification.defaults |= Notification.DEFAULT_SOUND;
    		Intent notificationIntent = new Intent(LocationActivity.this,
    				LocationActivity.class); // �����֪ͨ��Ҫ��ת��Activity
    		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    		PendingIntent contentItent = PendingIntent.getActivity(
    				LocationActivity.this, 0, notificationIntent, 0);
    		notification.setLatestEventInfo(LocationActivity.this,
    				getString(R.string.app_name),
    				getString(R.string.service), contentItent);
    		nm.notify(Config.notification_id, notification);
    		moveTaskToBack(true);
    	}
    }


}