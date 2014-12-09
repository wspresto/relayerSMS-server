package com.saar.relayersms;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Represents a text message.
 * @author keiser88
 *
 */
public class TextMessage {
	char [] txt;
	char [] header;
    private String timestamp;
    private String author;
    private String recipient; //default value
    private String content;
    private String id; //phone number

	int carCount;
	final public static int headerSize      = 80;
	final public static int textMessageSize = 160;

    /**
     * This contstructor is primarily used for when SMS' arrive
     */
	public TextMessage() {
		txt    = new char[textMessageSize]; //limit of 160 characters
		header = new char[headerSize];
		carCount = 0;
        timestamp = new Timestamp(new Date().getTime()).toString();
        this.recipient = "Me";
	}

    /**
     * This method is the entry point for sending a new text message
     * @param id
     * @param message
     * @param author
     * @param recipient
     * @param timestamp
     */
    public TextMessage(String id, String message, String author, String recipient, String timestamp) {
        this.id = id;
        this.content = message;
        this.author = author;
        this.recipient = recipient;
        this.timestamp = timestamp;
    }

	/**
	 * used to build the message when the phone receives an sms
	 * @return the position of the next byte to be copied.
	 */
	public int setTextMessage(int offset, byte [] bites) {
		int b;
		for(b = offset;b < bites.length && b < txt.length ; b++) {
			txt[b] = (char)bites[b];
		}
        this.content = this.getMessage();
		return b;
	}
	public void setHeader(byte [] bites) {
		for(int b = 0;b < header.length && b < bites.length ; b++) {
			header[b] = (char)bites[b];
		}

        this.author = decodeSender();
        this.id = decodeID();
	}

	public char [] getHeader() {
		return header;
	}
	private String [] getTuples() {
		String headerLine = new String(header);
		//assume CSV name,id
		String [] tuples = headerLine.split(",");
		
		return tuples;
	}
	private String decodeSender() {
		String [] tuples = getTuples();
		String name;
		if(tuples.length < 2)
			return "";
		else {
			name = tuples[0].replace((char) 0, ' ');
			name = name.trim();
		}
        return name;
	}
	private String decodeID() {
		String [] tuples = getTuples();
		String id;
		if(tuples.length < 2)
			return "";
		else {
			id = tuples[1];
			id = AddressBook.cleanPhoneNumber(id);
		}
			
        return id;
	}
    public String getSender() {
        return this.author;
    }
    public String getID() {
        return this.id;
    }
    public String getRecipient() {
        return this.recipient;
    }
    public String getTimestamp() {
        return this.timestamp;
    }
	public String getMessage() {
		String msg = new String(txt);
		msg        = msg.replace((char) 0, ' ');
		msg        = msg.trim();
		return msg;
	}
    public String toJSON() {
        String JSON = "\"content\"" + ":" +"\"" + this.getMessage() + "\"";
        JSON       += ",";
        JSON       += "\"author\"" + ":" +"\"" + this.getSender() + "\"";
        JSON       += ",";
        JSON       += "\"timestamp\"" + ":" +"\"" + this.getTimestamp() + "\"";
        JSON       += ",";
        JSON       += "\"number\"" + ":" +"\"" + this.getID() + "\"";
        JSON       += ",";
        JSON       += "\"recipient\"" + ":" +"\"" + this.getID() + "\"";
        return "{"+JSON+"}";
    }
/*	public char [] getTxt() {
		return txt;
	}
	public byte [] getBytes() {

		byte [] bites = new byte[txt.length + header.length];
		int b;
		for(b = 0; b < header.length ; b++) {
			bites[b] = (byte) header[b];
		}

		for(b = 0; b < txt.length; b++) {
			bites[b + header.length] = (byte) txt[b];
		}
		return bites;
	}*/

}
