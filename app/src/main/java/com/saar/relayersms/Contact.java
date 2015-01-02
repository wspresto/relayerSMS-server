package com.saar.relayersms;

/**
 * Created by wpreston on 12/6/14.
 */
public class Contact {
    String author;
    String id;
    private int numMessages = 0;
    public Contact(String name, String number) {
        author = name;
        id     = number;
    }
    public String getID() {
        return this.id;
    }
    public String getAuthor() {
        return this.author;
    }
    public String toJSON() {
        String JSON = "\"author\"" + ":" +"\"" + this.getAuthor() + "\"";
        JSON       += ",";
        JSON       += "\"id\"" + ":" +"\"" + this.getID() + "\"";
        return "{"+JSON+"}";
    }
    public boolean hasMessages() {
        return this.numMessages > 0;
    }
    public int incrementMessages() {
        return this.numMessages++;
    }
}
