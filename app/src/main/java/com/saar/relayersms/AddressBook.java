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
    Contact [] contacts;
    public AddressBook(Context context) {
        this.context = context;
        Cursor people = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //Cursor numbers = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        Contact contact;
        String name;
        String number;
        int nameFieldColumnIndex;
        int numberFieldColumnIndex;
        while(people.moveToNext()) {
            nameFieldColumnIndex = people.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            name = people.getString(nameFieldColumnIndex);

            if (Integer.parseInt(people.getString(people.getColumnIndex(ContactsContract.PhoneLookup.HAS_PHONE_NUMBER))) > 0) {
                number = this.getPhoneNumber(people.getString(people.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                //System.out.println(name + " has number " + number);
            } else {
                //System.out.println(name + " does not have a number");
                continue;
            }
            contact = new Contact(name, cleanPhoneNumber(number));
            if (isValidPhoneNumber(contact.getID())) {
                contacts.add(contact);
            }
        }
        people.close();
        if (contacts.size() < 1) {
            this.contacts = new Contact[0];
        } else {
            Contact [] objs = new Contact[contacts.size()];
            for (int c = 0; c < contacts.size(); c++) {
                objs[c] = contacts.get(c);
            }
            this.contacts = objs; //throw the book at em~!
        }
    }
    private String getPhoneNumber(String id)
    {
        String number = "";
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);

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
        return this.contacts;
    }
    public static String cleanPhoneNumber(String digits) {
        digits = digits.trim(); //clean whitespaces
        String [] funnyStuff = new String[] {"-", "#", " ", "\\(", "\\)"};
        for (String replaceMe : funnyStuff) { //no funny stuff ok
            digits = digits.replaceAll(replaceMe, "");
        }
        return digits;
    }
    public static boolean isValidPhoneNumber(String digits) {
        int len = digits.length();
        return (len >= 7);
    }
    public Contact getContactByNumber(String number) {
        for (Contact contact : this.contacts) {
            if (contact.getID().equalsIgnoreCase(number)) {
                return contact;
            }
        }
        return null;
    }
}
