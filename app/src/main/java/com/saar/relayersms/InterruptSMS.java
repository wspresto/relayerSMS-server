package com.saar.relayersms;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;


/**
 * Essentially this entire object is copy pasted from stackoverflow. It's like wow, well theres only one way to get the information we need.
 * There is no original thought here, except the intent.
 * @author stackoverflow
 *
 */
public class InterruptSMS extends BroadcastReceiver {

    private void errLog(String line) {
        System.out.println(line);
    }
    // Retrieve SMS
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        errLog("broadcast detected.");
        if ( extras != null )
        {
            Object[] smsextras = (Object[]) extras.get( "pdus" );

            for ( int i = 0; i < smsextras.length; i++ )
            {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])smsextras[i]);
                String str = smsmsg.getMessageBody();

                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(smsmsg.getOriginatingAddress()));

                Cursor c = context.getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME},null,null,null);
                try {
                    c.moveToFirst();
                    String  displayName = c.getString(0);
                    String ContactName = displayName;

                    TextMessage sms = new TextMessage();
                    sms.setTextMessage(0, str.getBytes());

                    String address = ContactName + "," + smsmsg.getOriginatingAddress();
                    sms.setHeader(address.getBytes());
                    TextMessageServer.push(sms);


/*                    errLog("TEXT:" + str);
                    errLog("SentBy:" + ContactName);*/
                } catch (Exception e) {
                    String ContactName = "Unknown";

                    TextMessage sms = new TextMessage();
                    sms.setTextMessage(0, str.getBytes());

                    String address = ContactName + "," + smsmsg.getOriginatingAddress();

                    sms.setHeader(address.getBytes());

                    TextMessageServer.push(sms);
                }finally{
                    c.close();
                }

                }

            }   

            

        }




		
	}

		

