package com.saar.relayersms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created by wpreston on 12/9/14.
 */
public class TextMessageHistoryBook {
    TextMessage [] smsHistory;
    public TextMessageHistoryBook(Context context, AddressBook contacts) {
        ArrayList<TextMessage> txts = new ArrayList<TextMessage>();
        String [] whereColsEqual = {"address", "body", "date", "_id"};
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), whereColsEqual, null, null, null);
        String number = "";
        String body   = "";
        String time   = "";
        String name   = "";
        cursor.moveToFirst();
        Contact contact = null;

        do {
            number = AddressBook.cleanPhoneNumber(cursor.getString(0));
            body   = TextMessage.escapeJSON(cursor.getString(1));
            contact   = contacts.getContactByNumber(number);
            if (contact == null) {
                continue;
            }
            name = contact.getAuthor();
            contact.incrementMessages();
            time   = cursor.getString(2);
            txts.add(new TextMessage(number,body, name, "Me", time));
        } while (cursor.moveToNext());
        cursor.close();
        cursor = context.getContentResolver().query(Uri.parse("content://sms/sent"), whereColsEqual, null, null, null);
        cursor.moveToFirst();

        do {
            number = AddressBook.cleanPhoneNumber(cursor.getString(0));
            body   = TextMessage.escapeJSON(cursor.getString(1));
            contact   = contacts.getContactByNumber(number);
            if (contact == null) {
                continue;
            }
            name = contact.getAuthor();
            contact.incrementMessages();
            time   = cursor.getString(2);
            txts.add(new TextMessage(number, body, "Me", name, time));
        } while (cursor.moveToNext());
        cursor.close();

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
