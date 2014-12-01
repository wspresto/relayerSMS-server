package com.saar.relayersms;

/**
 * Represents a text message.
 * @author keiser88
 *
 */
public class TextMessage {
	char [] txt;
	char [] header;
	int carCount;
	final public static int headerSize      = 80;
	final public static int textMessageSize = 160;
	
	public TextMessage() {
		txt    = new char[textMessageSize]; //limit of 160 characters
		header = new char[headerSize];
		carCount = 0;
	}
	public void processHeader(byte [] bites) {
		//TODO: glean header information... model after relational database <id,name>
		
	}
	/**
	 * used to build the message
	 * @return the position of the next byte to be copied.
	 */
	public int setTextMessage(int offset, byte [] bites) {
		int b;
		for(b = offset;b < bites.length && b < txt.length ; b++) {
			txt[b] = (char)bites[b];
		}
		return b;
	}
	public void setHeader(byte [] bites) {
		for(int b = 0;b < header.length && b < bites.length ; b++) {
			header[b] = (char)bites[b];
		}		
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
	public String getSender() {
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
	public String getID() {
		String [] tuples = getTuples();
		String id;
		if(tuples.length < 2)
			return "";
		else {
			id = tuples[1];
			id = id.trim();
		}
			
			return id;
		
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
        return "{"+JSON+"}";
    }
	public char [] getTxt() {
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
	}

}
