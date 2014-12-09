package com.saar.relayersms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by wpreston on 12/9/14.
 */
public class TextMessageHistoryBook {
    TextMessage [] smsHistory;
    public TextMessageHistoryBook(Context context) {
        ArrayList<TextMessage> txts = new ArrayList<TextMessage>();
        String [] whereColsEqual = {"_id", "body", "address","date"};
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), whereColsEqual, null, null, null);
        cursor.moveToFirst();

        do {
            txts.add(new TextMessage(cursor.getString(0), cursor.getString(1), cursor.getString(2), "Me", cursor.getString(3)));
        } while (cursor.moveToNext());
        cursor = context.getContentResolver().query(Uri.parse("content://sms/sent"), whereColsEqual, null, null, null);
        cursor.moveToFirst();

        do {
            txts.add(new TextMessage(cursor.getString(0), cursor.getString(1), "Me", cursor.getString(2), cursor.getString(3)));
        } while (cursor.moveToNext());

        if (txts.size() < 1) {
            this.smsHistory = new TextMessage[0];
        } else {
            TextMessage [] smsObjs = new TextMessage[txts.size()];
            for (int c = 0; c < txts.size(); c++) {
                smsObjs[c] = txts.get(c);
            }

            this.smsHistory = smsObjs;

        }
    }
    public TextMessage [] getTexts() {
        return this.smsHistory;
    }
}
