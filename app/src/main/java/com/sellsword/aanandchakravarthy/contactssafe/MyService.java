package com.sellsword.aanandchakravarthy.contactssafe;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

public class MyService extends Service {
    LocalBroadcastManager broadcaster;

    public static String VASANTH_NO = "10";
    public static String PERCENT = "PERCENT";
    public static String CONTACT = "CONTACT";
    public MyService() {
    }
    public void sendResult(int percentComplete,ContactRep rep) {
        Intent intent = new Intent(VASANTH_NO);
        intent.putExtra(PERCENT,percentComplete);
        intent.putExtra(CONTACT,rep);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public void onCreate(){
        broadcaster = LocalBroadcastManager.getInstance(this);

        Log.d("vasanth","creating service");
    }

     @Override
    public int onStartCommand (Intent intent,
                        int flags,
                        int startId){
        Log.d("vasanth","service onstart");
        MyThread thread = new MyThread();
        thread.start();

        return START_STICKY;
    }
    class MyThread extends Thread{
        public void run(){
            Log.d("vasanth","running thread");
            int counter = 0;
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
                    int num_contacts = phones.getCount();
                    while (phones.moveToNext())
                    {
                        String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        ContactRep val = new ContactRep();
                        val.name = name;
                        val.phoneno.add(phoneNumber);
                        Log.d("vasanth",name);
                        Log.d("phoneNumber",phoneNumber);
                        counter = counter + 1;
                        int percent = (int) ((float)counter/(float)num_contacts*100f);
                        sendResult(percent,val);


                    }
                    phones.close();


                stopSelf();

        }
    }
}
