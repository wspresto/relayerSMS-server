package com.saar.relayersms;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.Menu;
import android.view.MenuItem;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import android.provider.Telephony;
import android.view.View;
import android.widget.TextView;


public class sms_activity extends Activity {

    /*==									RELAYER SMS 									==*/
    private InterruptSMS callbackSMS;
    private IntentFilter infil;
    IncomingThread serverSMS;
    private final int incomingPort = 8080; //sms messages from the world
    public static boolean isServerAlive = false;
    private Activity that = null; //
    private class IncomingThread extends Thread{ //sms messages to the world, incoming from netbook
        Context context;
        public IncomingThread(Context context) {
            this.context = context;
        }
        public void run() {
            new TextMessageServer(context, incomingPort);
        }
    }
    /**
     * android voodoo to be notified with txt messages
     */
    private void acceptSMS() {
        //launch the relay service which creates callbacks/interrupts
        //for when an sms arrives

        callbackSMS = new InterruptSMS();
        infil    = new IntentFilter();
        infil.addAction("android.provider.Telephony.SMS_DELIVER"); //IS THIS NEEDED?!?!
        registerReceiver(callbackSMS, infil);
    }

    private void log(String line) {
	System.out.println(line);
    }

    public void killServer(View e) {
        if (isServerAlive) {
            isServerAlive = false;
            unregisterReceiver(callbackSMS);
            //finish();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_activity);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sms_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void init() {
        if (!isServerAlive) {
            isServerAlive = true;

            acceptSMS();
            serverSMS = new IncomingThread(this);
            serverSMS.start();
        }
    }
}
