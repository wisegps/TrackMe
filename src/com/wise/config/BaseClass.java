package com.wise.config;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BaseClass {
	public static String getMacAdress(Context context){
		try {
			//��ȡMAC��ַ  Android 6.0 ��ȡmac ��������
			String wifiAddress = "";
			
			if(!isAndroidM()){
				WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);		 
				WifiInfo info = wifi.getConnectionInfo();
				wifiAddress = info.getMacAddress();
			}else{
				wifiAddress = macAddress();
			}
			Log.e("TEST_BUG", "�ֻ���Ϣwifimac �� " + wifiAddress);
			if(wifiAddress != null){
				return wifiAddress;
			}
			String Imei = ((TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE)).getDeviceId();
			Log.e("TEST_BUG", "�ֻ���Ϣimei �� " + Imei);
			if(Imei != null){
				return Imei;
			}
			String BluetoothAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
			Log.e("TEST_BUG", "�ֻ���Ϣ BluetoothAddress  �� " + BluetoothAddress);
			return BluetoothAddress;
		} catch (Exception e){
			return "";
		}
	}
	
	// ����Ȥ�����ѿ��Կ���NetworkInterface��Android FrameWork����ôʵ�ֵ�
    public static String macAddress() throws SocketException {
            String address = null;
            // �ѵ�ǰ�����ϵķ�������ӿڵĴ��� Enumeration������
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            Log.d("TEST_BUG", " interfaceName = " + interfaces );
            while (interfaces.hasMoreElements()) {
                NetworkInterface netWork = interfaces.nextElement();
                // �������Ӳ����ַ������ʹ�ø����ĵ�ǰȨ�޷��ʣ��򷵻ظ�Ӳ����ַ��ͨ���� MAC���� 
                byte[] by = netWork.getHardwareAddress();
                if (by == null || by.length == 0) {
                    continue;
                }
                StringBuilder builder = new StringBuilder();
                for (byte b : by) {
                    builder.append(String.format("%02X:", b));
                }
                if (builder.length() > 0) {
                    builder.deleteCharAt(builder.length() - 1);
                }
                String mac = builder.toString();
                Log.d("TEST_BUG", "interfaceName="+netWork.getName()+", mac="+mac);
                // ��·�����������豸��MAC��ַ�б�����ӡ֤�豸Wifi�� name �� wlan0
                if (netWork.getName().equals("wlan0")) {
                    Log.d("TEST_BUG", " interfaceName ="+netWork.getName()+", mac="+mac);
                    address = mac;
                }
            }
            return address;
        } 
    
    
    public static boolean isAndroidM(){

        return(Build.VERSION.SDK_INT>22);

    }
}
