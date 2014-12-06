package com.saar.relayersms;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.util.ArrayList;
import java.util.Collection;

import java.util.StringTokenizer; //a llist

/**
 * a simple map of contact name and phone number
 */
public class AddressBook {
    Context context = null;
    public AddressBook(Context context) {
        this.context = context;
    }
    private String getPhoneNumber(String id)
    {
        String number = "";
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone._ID + " = " + id, null, null);

        if(phones.getCount() > 0)
        {
            while(phones.moveToNext())
            {
                number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            }
        }

        phones.close();

        return number;
    }
    public Contact [] getContacts() {
        Cursor people = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //Cursor numbers = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        String name;
        String number;
        int nameFieldColumnIndex;
        int numberFieldColumnIndex;
        while(people.moveToNext()) {
            nameFieldColumnIndex = people.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            name = people.getString(nameFieldColumnIndex);

            if (Integer.parseInt(people.getString(people.getColumnIndex(ContactsContract.PhoneLookup.HAS_PHONE_NUMBER))) > 0) {
                number = this.getPhoneNumber(people.getString(people.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                System.out.println(name + " has number " + number);
            } else {
                System.out.println(name + " does not have a number");
                continue;
            }

            contacts.add(new Contact(name, number));
        }

        people.close();
        if (contacts.size() < 1) {
            return new Contact[0];
        } else {
            Contact [] objs = new Contact[contacts.size()];
            for (int c = 0; c < contacts.size(); c++) {
                objs[c] = contacts.get(c);
            }
            return objs; //throw the book at em~!
        }

    }
}
