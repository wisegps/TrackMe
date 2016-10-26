package com.wise.crash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.wise.service.AlarmService;
import com.wise.service.LocationService;
import com.wise.trackme.activity.LocationActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler{
	//ϵͳĬ�ϵ�UncaughtException������  
    private Thread.UncaughtExceptionHandler mExceptionHandler;  
      
    //CrashHandlerʵ��  
    private static CrashHandler INSTANCE=new CrashHandler();  
      
    private Context mContext;  
      
    //��ֻ֤��һ��CrashHandlerʵ��  
    private CrashHandler (){  
          
    }  
      
    public static CrashHandler getInstance(){  
        return INSTANCE;  
    }  
      
    public void init(Context context){  
        mContext=context;  
        //��ȡϵͳĬ�ϵ�UncaughtException������    
        mExceptionHandler =Thread.getDefaultUncaughtExceptionHandler();  
        //���ø�CrashHandlerΪ�����Ĭ�ϴ�����    
        Thread.setDefaultUncaughtExceptionHandler(this);  
    }  
      
    @Override  
    public void uncaughtException(Thread thread, Throwable ex) {  
        if(!handleException(ex) && mExceptionHandler!=null){  
            mExceptionHandler.uncaughtException(thread, ex);  
        }else {  
            try {  
                Thread.sleep(500);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            
            //����Ҫֹͣ���� ��Ȼ�ڶ��������᲻��
            
            mContext.stopService(new Intent(mContext, LocationService.class)); 
            mContext.stopService(new Intent(mContext, AlarmService.class)); 
            
            AppManager.getAppManager().finishAllActivity();
            android.os.Process.killProcess(android.os.Process.myPid()); 
            System.exit(1);
        }  
    }  
      
    private boolean handleException (Throwable throwable){  
        if(throwable==null){  
            return false;  
        }  
//        new Thread(new Runnable() {  
//              
//            @Override  
//            public void run() {  
//                Looper.prepare();  
////                Toast.makeText(mContext, "�ܱ�Ǹ,��������쳣,�����˳�.", Toast.LENGTH_SHORT).show();  
//                Looper.loop();  
//            }  
//        }){  
//        }.start();  
        //�ռ��豸������Ϣ   
        collectDeviceInfo(mContext);  
        //������־�ļ�   
        saveCrashInfo2File(throwable);
        return true;  
    }  
    
    
    
    String TAG = "TAG";
  //�����洢�豸��Ϣ���쳣��Ϣ  
    Map<String, String> infos = new HashMap<String, String>();  
  
    //���ڸ�ʽ������,��Ϊ��־�ļ�����һ����  
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); 
    /** 
     * �ռ��豸������Ϣ 
     * @param ctx 
     */  
    public void collectDeviceInfo(Context ctx) {  
        try {  
            PackageManager pm = ctx.getPackageManager();  
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);  
            if (pi != null) {  
                String versionName = pi.versionName == null ? "null" : pi.versionName;  
                String versionCode = pi.versionCode + "";  
                infos.put("versionName", versionName);  
                infos.put("versionCode", versionCode);  
            }  
        } catch (NameNotFoundException e) {  
            Log.e(TAG, "an error occured when collect package info", e);  
        }  
        Field[] fields = Build.class.getDeclaredFields();  
        for (Field field : fields) {  
            try {  
                field.setAccessible(true);  
                infos.put(field.getName(), field.get(null).toString());  
                Log.d(TAG, field.getName() + " : " + field.get(null));  
            } catch (Exception e) {  
                Log.e(TAG, "an error occured when collect crash info", e);  
            }  
        }  
    }  
  
    /** 
     * ���������Ϣ���ļ��� 
     *  
     * @param ex 
     * @return  �����ļ�����,���ڽ��ļ����͵������� 
     */  
    private String saveCrashInfo2File(Throwable ex) {  
          
        StringBuffer sb = new StringBuffer();  
        for (Map.Entry<String, String> entry : infos.entrySet()) {  
            String key = entry.getKey();  
            String value = entry.getValue();  
            sb.append(key + "=" + value + "\n");  
        }  
          
        Writer writer = new StringWriter();  
        PrintWriter printWriter = new PrintWriter(writer);  
        ex.printStackTrace(printWriter);  
        Throwable cause = ex.getCause();  
        while (cause != null) {  
            cause.printStackTrace(printWriter);  
            cause = cause.getCause();  
        }  
        printWriter.close();  
        String result = writer.toString();  
        sb.append(result);  
        try {  
            long timestamp = System.currentTimeMillis();  
            String time = formatter.format(new Date());  
            String fileName = "crash-" + time + "-" + timestamp + ".log";  
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
                String path = "/sdcard/crash/";  
                File dir = new File(path);  
                if (!dir.exists()) {  
                    dir.mkdirs();  
                }  
                FileOutputStream fos = new FileOutputStream(path + fileName);  
                fos.write(sb.toString().getBytes());  
                fos.close();  
            }  
            return fileName;  
        } catch (Exception e) {  
            Log.e(TAG, "an error occured while writing file...", e);  
        }  
        return null;  
    }  
    
    
    
    
    
}
