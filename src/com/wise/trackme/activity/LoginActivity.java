package com.wise.trackme.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.wise.config.BaseClass;
import com.wise.config.Config;
import com.wise.crash.AppManager;
import com.wise.service.NetThread;

import android.Manifest;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private static final int login    = 1; // ��¼
	private static final int register = 2; // ע��
	private static final int GET_DATA = 3;
	private static final int SAVE     = 4;
	private static final CharSequence text = null;

	private ProgressDialog Dialog;
	private EditText et_account, et_password;
	private CheckBox cb_isSavePwd;
	private TextView tv_lastUpdate,tv_setting;

	private String name = "";
	private String password = "";
	private boolean LoginNote = true;
	private String ObjectId = "";
	private String Adress = "";
	private boolean isNeedConfig = false;
	private String GSMVoiceNum;
	private String ObjectRegNum;	
	
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	
	private Button bt_login;
	private boolean isLogout = false;
	
	
	private String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
	private int LOCATION_PERMISSION_REQUEST = 200; 



	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManager.getAppManager().addActivity(this); 
		
		if(BaseClass.isAndroidM()){
			
			
			//�ж��Ƿ���Ȩ��
			// Here, thisActivity is the current activity
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

			//����Ȩ��
			ActivityCompat.requestPermissions(this,
			                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
			                LOCATION_PERMISSION_REQUEST);
			//�ж��Ƿ���Ҫ ���û����ͣ�ΪʲôҪ�����Ȩ��
			ActivityCompat.shouldShowRequestPermissionRationale(this,
			                Manifest.permission.READ_CONTACTS);
			 Log.e("TEST_BUG", "Ȩ������  �� ����6.0");
			
			}
			
			 Log.e("TEST_BUG", "Ȩ������  �� .......");
		}
		
		editor = getSharedPreferences("ip", MODE_PRIVATE).edit();
    	pref = getSharedPreferences("ip", MODE_PRIVATE); 
    	setConfig(pref.getString("host_ip", Config.URL));
		getSp();
		if(!isGPSOpen(LoginActivity.this)){
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(getResources().getString(R.string.prompt));
			dialog.setMessage(getResources().getString(R.string.open_gps));
			dialog.setPositiveButton(getResources().getString(R.string.ok),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// ת���ֻ����ý��棬�û�����GPS
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, 5); // ������ɺ󷵻ص�ԭ���Ľ���
						}
					});
			dialog.setNeutralButton(getResources().getString(R.string.cancel), new android.content.DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
					Toast.makeText(LoginActivity.this,getResources().getString(R.string.turn_on_gps_please), Toast.LENGTH_SHORT).show();
					LoginActivity.this.finish();
				}
			} );
			dialog.show();
		}
		init();
	}
	
	
	

	
	private void init(){
		if(isLogout){
			setContentView(R.layout.activity_login);
			initView();			
		}else{
			Intent intent = new Intent(LoginActivity.this,
					LocationActivity.class);
			startActivity(intent);
			finish();
		}
		
		Log.e("TEST_BUG", "�ֻ���Ϣ �� " + BaseClass.getMacAdress(getApplicationContext()));
	}

	private void initView() {
		tv_lastUpdate = (TextView) findViewById(R.id.tv_lastUpdate);
		tv_lastUpdate.setText(Config.UpdateTime );
		
		et_account = (EditText) findViewById(R.id.et_account);
		et_password = (EditText) findViewById(R.id.et_password);

		et_password.setTypeface(Typeface.DEFAULT);
		et_password.setTransformationMethod(new PasswordTransformationMethod());
		
		tv_setting = (TextView)findViewById(R.id.tv_setting);
		tv_setting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LayoutInflater factory = LayoutInflater.from(LoginActivity.this);//��ʾ��  
	            final View view = factory.inflate(R.layout.edit_layout, null);//���������final��  
	            final EditText edit=(EditText)view.findViewById(R.id.editText);//�����������  	
	            edit.setText(pref.getString("host_ip", Config.URL));
	            new AlertDialog.Builder(LoginActivity.this)  
	            .setIcon(R.drawable.search_pop_total_icon_d)
	                 .setTitle(getResources().getString(R.string.setting_ip))//��ʾ�����  
	                 .setView(view)  
	                 .setPositiveButton(getResources().getString(R.string.ok), new android.content.DialogInterface.OnClickListener() {  
	                                @Override  
	                                public void onClick(DialogInterface dialog,  
	                                        int which) {  
	                                    //�¼�  
	                                	
	                                	if("".equals(edit.getText().toString().trim()) ){
	                                		Toast.makeText(LoginActivity.this,getResources().getString(R.string.ip_empty), Toast.LENGTH_SHORT).show();
	                                		return;
	                                	}
	                                	
	                                	editor.putString("host_ip", edit.getText().toString().trim());
	                                	editor.commit();
	                                	setConfig(edit.getText().toString().trim());
	                               }  
	                            })
	                 .setNegativeButton(getResources().getString(R.string.cancel), null).create().show();  
			}
		});	
		bt_login = (Button) findViewById(R.id.bt_login);
		bt_login.setOnClickListener(new ButtonListener());
		cb_isSavePwd = (CheckBox) findViewById(R.id.cb_password);
		// �����Ƿ���ʾ��ס����
		cb_isSavePwd.setChecked(LoginNote);
		et_account.setText(name);
		et_password.setText(password);

		cb_isSavePwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				LoginNote = cb.isChecked();
			}
		});
	}



	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case login:
				if (Dialog != null) {
					Dialog.dismiss();
				}
				String result = msg.obj.toString();
				Log.e("TEST_BUG", "��¼������Ϣ   �� " + result);
				if ("".equals(result)) {
				} else if ("null".equals(result.substring(1, 5))) {
					Toast.makeText(LoginActivity.this, R.string.username_dont_match, Toast.LENGTH_SHORT) .show();
					bt_login.setEnabled(true);
				} else if (result.indexOf("Exception") != -1) {
					Toast.makeText(LoginActivity.this, R.string.network_wrong, Toast.LENGTH_SHORT).show();
					bt_login.setEnabled(true);
				} else {
					Log.e("TEST_BUG", "--------- �� " + Adress);
					try {
						JSONObject userInfo = new JSONObject(result.substring(
								1, result.length()));
						if (Adress.equals("")) {
							bt_login.setEnabled(true);
							exit();
						} else {
							String time = getTime();
							Log.e("TEST_BUG", "--------- �� " + Adress + " == "  + time);
							List<NameValuePair> params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("ObjectID", time));
							params.add(new BasicNameValuePair("Imei", Adress));
							params.add(new BasicNameValuePair("GroupCode",userInfo.getString("GroupCode")));
							params.add(new BasicNameValuePair("CustomerID",userInfo.getString("UserID")));
							new Thread(new NetThread.postDataThread(handler, Config.Register, params, register)) .start();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						bt_login.setEnabled(true);
					}
				}
				break;
			case register:
				if (Dialog != null) {
					Dialog.dismiss();
				}
				String requestEvent = (String) msg.obj;
				Log.e("TEST_BUG", "ע�᷵����Ϣ   �� " + requestEvent);
				
				if ("(\"-1\");".equals(requestEvent)) {
					bt_login.setEnabled(true);
					Toast.makeText(LoginActivity.this,
							getString(R.string.init_time_error), 0).show();
				} else if (requestEvent.indexOf("Exception") != -1) {
					bt_login.setEnabled(true);
					Toast.makeText(LoginActivity.this,
							msg.obj + getString(R.string.try_again), 0).show();
				} else {
					System.out.println(requestEvent);
					ObjectId = requestEvent.substring(2,
							(msg.obj.toString()).length() - 3);
					Log.e("TEST_BUG", "ObjectId������Ϣ   �� " + ObjectId);
					isSave();
					if (isNeedConfig) {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("ObjectID", ObjectId));
						new Thread(new NetThread.postDataThread(handler,
								Config.GetObjectInfo, params, GET_DATA))
								.start();
					} else {
						Intent intent = new Intent(LoginActivity.this,
								LocationActivity.class);				
						startActivity(intent);
						finish();
					}
				}
				break;
			case GET_DATA:
				try {
					String result1 = msg.obj.toString();
					JSONObject jsonObject = new JSONObject(result1.substring(1,result1.length() - 2));
					GSMVoiceNum = jsonObject.getString("GSMVoiceNum");
					ObjectRegNum = jsonObject.getString("ObjectRegNum");
					editor.putString("nick_name", ObjectRegNum);
                	editor.commit();
					FristSetUp();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case SAVE:
				if (Dialog != null) {
					Dialog.dismiss();
				}
				if (msg.obj.toString().indexOf("-1") >= 0) {
					Toast.makeText(getApplicationContext(),
							R.string.commit_fail, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.commit_success, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(LoginActivity.this,
							LocationActivity.class);
					startActivity(intent);
					finish();
				}
				break;
			}
		}
	};

	private class ButtonListener implements OnClickListener {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_login:
				if ("".equals(et_account.getText().toString().trim())) {
					Toast.makeText(LoginActivity.this,
							getString(R.string.User_name_cannot_be_empty),
							Toast.LENGTH_SHORT).show();
					return;
				} else if ("".equals(et_password.getText().toString().trim())) {
					Toast.makeText(LoginActivity.this,
							getString(R.string.Password_cannot_be_empty),
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					bt_login.setEnabled(false);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					name = et_account.getText().toString().trim();
					password = et_password.getText().toString().trim();

					params.add(new BasicNameValuePair("p_strUserName", name));
					params.add(new BasicNameValuePair("p_stdPassword", password));
					new Thread(new NetThread.postDataThread(handler,
							Config.USER_LOGIN, params, login)).start();
					Dialog = ProgressDialog.show(LoginActivity.this,"",getString(R.string.in_login), true);
				}
				break;
			default:
				return;
			}
		}

	}

	private void isSave() {
		/* �����û��˺� */
		SharedPreferences preferences = getSharedPreferences(
				Config.Shared_Preferences, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString("LoginName", name);
		editor.putString("ObjectId", ObjectId);
		editor.putString("Adress", Adress);
		if (LoginNote) {
			editor.putString("LoginPws", password);
		} else {
			editor.putString("LoginPws", "");
		}
		editor.putBoolean("LoginNote", LoginNote);
		editor.commit();
	}

	// �������״��
	public boolean checkNetWorkStatus(Context context) {
		boolean result;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null && netinfo.isConnected()) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/*// ��ʾû�йȸ����
	private void ExitDialog() {
		AlertDialog.Builder bulder = new AlertDialog.Builder(LoginActivity.this);
		bulder.setTitle(R.string.prompt);// ���ñ���
		bulder.setMessage(R.string.google_unalive);
		bulder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						LoginActivity.this.finish();
					}
				});
		bulder.show();
	}*/

	protected void onResume() {
		super.onResume();
		if (checkNetWorkStatus(LoginActivity.this)) {

		} else {
			AlertDialog.Builder setNet = new Builder(LoginActivity.this);
			setNet.setTitle(getString(R.string.prompt));
			setNet.setMessage(getString(R.string.set_net));
			setNet.setPositiveButton(getString(R.string.sure_set),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(
									"android.settings.WIRELESS_SETTINGS"));
						}
					});
			setNet.setNegativeButton(getString(R.string.button_cancle),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							LoginActivity.this.finish();
						}
					});

			setNet.show();
		}
	}

	// ��������ת��Ϊʱ���
	public String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd- HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		long time = curDate.getTime();
		return time + "";
	}

	/**
	 * ��ס����֮����ʾ����
	 */
	private void getSp() {// ��ȡsharedPreferences������Ϣ
		SharedPreferences pref = getSharedPreferences(Config.Shared_Preferences, Context.MODE_PRIVATE);
		name      = pref.getString("LoginName", "");
		password  = pref.getString("LoginPws", "");
		Adress    = pref.getString("Adress", "");
		LoginNote = pref.getBoolean("LoginNote", false);
		isLogout  = pref.getBoolean("isLogout", true);
		if (Adress.equals("")) {
			isNeedConfig = true;
			Adress = BaseClass.getMacAdress(getApplicationContext());
		}
	}

	public void exit() {
		AlertDialog.Builder builder = new Builder(LoginActivity.this);
		builder.setTitle(getString(R.string.prompt));
		builder.setMessage(getString(R.string.cannot_get_driver_id));
		builder.setPositiveButton(R.string.sure_set,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						LoginActivity.this.finish();
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						LoginActivity.this.finish();
					}
				});
		builder.show();
		builder.setCancelable(false);
	}

	public void FristSetUp() {
		View view_Phone = LayoutInflater.from(LoginActivity.this).inflate(
				R.layout.device, null);
		final EditText et_device = (EditText) view_Phone
				.findViewById(R.id.et_device);
		et_device.setText(ObjectRegNum);
		AlertDialog.Builder addPhoneBuilder = new AlertDialog.Builder(
				LoginActivity.this);
		addPhoneBuilder.setTitle(R.string.divices_name);
		addPhoneBuilder.setView(view_Phone);
		addPhoneBuilder.setPositiveButton(R.string.sure_set,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String device = et_device.getText().toString().trim();
						if (device.equals("")) {
							Toast.makeText(LoginActivity.this, "device's name can't be null!", Toast.LENGTH_SHORT).show();
							return;
						} else {
							Dialog = ProgressDialog.show(LoginActivity.this,
									"", getString(R.string.in_commit), true);
							List<NameValuePair> params = new ArrayList<NameValuePair>();
						
							params.add(new BasicNameValuePair("p_strObjectID",ObjectId));
							params.add(new BasicNameValuePair("p_strSIM",GSMVoiceNum));
							params.add(new BasicNameValuePair("p_strVehicleName",device));		
							params.add(new BasicNameValuePair("p_strDriverName",""));
							params.add(new BasicNameValuePair("p_strDriveMobile",""));
							params.add(new BasicNameValuePair("p_strVehicleModel",""));
							params.add(new BasicNameValuePair("p_strVehicleBrand",""));
							params.add(new BasicNameValuePair("p_flFuelRatio","0"));
							params.add(new BasicNameValuePair("p_strRegistration",""));
							params.add(new BasicNameValuePair("p_strInsurance",""));
							params.add(new BasicNameValuePair("p_strPermit",""));
							params.add(new BasicNameValuePair("p_strLicense",""));
							params.add(new BasicNameValuePair("p_strLastService",""));
							params.add(new BasicNameValuePair("p_intServiceOdo","0"));
							params.add(new BasicNameValuePair("p_intFuelType","0"));			
							
							new Thread(new NetThread.postDataThread(handler,
									Config.UpdateObjectInfo, params, SAVE))
									.start();
						}
					}
				});
		addPhoneBuilder.setNegativeButton(R.string.button_cancle, null);
		addPhoneBuilder.show();
	}
	
	
	/**
	 * @param url
	 */
	public static void setConfig(String url){
		Config.BaseUrl = url;		
		Config.USER_LOGIN = Config.BaseUrl + "Login_json";// �û���¼
		Config.Register = Config.BaseUrl + "RegisterTrackMe_json";// ���ObjectId
		Config.Check = Config.BaseUrl + "InsertEvent_json";//���°��
		Config.GetObjectInfo = Config.BaseUrl + "GetObjectInfo_json"; //��ȡ��Ϣ
		
//		Config.UPDATE_LOCATION = Config.BaseUrl + "UpdateGPSDate_json";// ����λ��
		/*2016-07-18   ������λ����Ϣ�ӿ� UpdateGPSDate2_json ����� ����λ�õ�ʱ�� */
		Config.UPDATE_LOCATION = Config.BaseUrl + "UpdateGPSDate2_json";// ����λ��
		Config.UpdateObjectInfo = Config.BaseUrl + "UpdateObjectInfo"; //������Ϣ
		
	}
	
	
	/**
     * �ж�GPS�Ƿ�����GPS����AGPS����һ������Ϊ�ǿ�����
     * @param context
     * @return true ��ʾ����
     */
    public static boolean isGPSOpen(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // ͨ��GPS���Ƕ�λ����λ������Ծ�ȷ���֣�ͨ��24�����Ƕ�λ��������Ϳտ��ĵط���λ׼ȷ���ٶȿ죩
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        // ͨ��WLAN���ƶ�����(3G/2G)ȷ����λ�ã�Ҳ����AGPS������GPS��λ����Ҫ���������ڻ��ڸ������Ⱥ��ï�ܵ����ֵȣ��ܼ��ĵط���λ��
//        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps) {
            return true;
        }else{
        	return false;
        }      
    }

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode == 5){
    		if(!LoginActivity.isGPSOpen(LoginActivity.this)){
    			Toast.makeText(LoginActivity.this,getResources().getString(R.string.turn_on_gps_please), Toast.LENGTH_SHORT).show();
    			LoginActivity.this.finish();
    		}else{
    			Toast.makeText(LoginActivity.this,getResources().getString(R.string.open_gps_success), Toast.LENGTH_SHORT).show();
    		}	
    	}	
    }
	
	
}
