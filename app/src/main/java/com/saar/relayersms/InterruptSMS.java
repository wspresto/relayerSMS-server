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
import android.telephony.gsm.SmsMessage;

/**
 * Essentially this entire object is copy pasted from stackoverflow. It's like wow, well theres only one way to get the information we need.
 * There is no original thought here, except the intent.
 * @author stackoverflow
 *
 */
public class InterruptSMS extends BroadcastReceiver {

    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private Context mContext;
    private Intent mIntent;

    private void errLog(String line) {
        System.out.println(line);
    }
    // Retrieve SMS
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if ( extras != null )
        {

            Object[] smsextras = (Object[]) extras.get( "pdus" );

            for ( int i = 0; i < smsextras.length; i++ )
            {

                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])smsextras[i]);
                String str = smsmsg.getMessageBody().toString();
                Uri uri = Uri.withAppendedPath(
                        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(smsmsg.getOriginatingAddress()));


                String name = "?";

                ContentResolver contentResolver = context.getContentResolver();
                Cursor contactLookup = contentResolver.query(uri, new String[] {
                        BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
                        null, null, null);

                try {
                    if (contactLookup != null && contactLookup.getCount() > 0) {
                        contactLookup.moveToNext();
                        name = contactLookup.getString(contactLookup
                                .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                        // String contactId =
                        // contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
                    }
                } catch(Exception e) {

                }

                /*     do code               */
                TextMessage sms = new TextMessage();
                sms.setTextMessage(0, str.getBytes());

                String address = name + "," + smsmsg.getOriginatingAddress();

                sms.setHeader(address.getBytes());
                    
                TextMessageServer.msgQueue.add(sms);
                errLog("SMS RECEIVED!!!");
                }

            }   

            

        }




		
	}

		

