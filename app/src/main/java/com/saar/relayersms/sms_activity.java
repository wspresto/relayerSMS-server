package com.saar.relayersms;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.SmsManager;
import android.view.View;


public class sms_activity extends Activity implements TextMessageCallback{

    /*==									RELAYER SMS 									==*/
    private InterruptSMS callbackSMS;
    private IntentFilter infil;
    IncomingThread serverSMS;
    private final int incomingPort = 8080; //sms messages from the world

    WakeLock wakeLock; //kudos to stack overflow lol

    private class IncomingThread extends Thread{ //sms messages to the world, incoming from netbook
        TextMessageCallback callOnMe;
        public IncomingThread(TextMessageCallback callMe) {
            callOnMe = callMe;
        }
        public void run() {
            new TextMessageServer(callOnMe, incomingPort);
        }
    }
    /**
     * android voodoo to be notified with txt messages
     */
    private void listenForSMS() {
        //launch the relay service which creates callbacks/interrupts
        //for when an sms arrives

        callbackSMS = new InterruptSMS();
        infil    = new IntentFilter();
        infil.addAction("android.provider.Telephony.RECEIVE_SMS");
        registerReceiver(callbackSMS, infil);

    }

    /**
     * create a server thread.
     * register an sms callback
     * @param e
     */
    public void init() {
        listenForSMS();
        //TODO: 4/10 idk why but the power lock mngr thingy is broken....
/*        PowerManager mgr = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();*/
        serverSMS = new IncomingThread(this);
        serverSMS.start();


    }

    /* btn events */
    public void parseIP(View e) {
        //EditText inputField = (EditText) findViewById(R.id.ipInput);
        //serverIP = inputField.getText().toString();
        //updateServerIP();
        //DO NOTHING!!!

    }
    public void killServer(View e) {
        wakeLock.release();
        unregisterReceiver(callbackSMS);
        serverSMS.destroy();

        finish();
    }

    @Override
    public void processTextMessage(TextMessage msg) {
        SmsManager mail = SmsManager.getDefault();
        mail.sendTextMessage(msg.getID(), null, msg.getMessage(), null, null);

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
}
