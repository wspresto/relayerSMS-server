package com.saar.relayersms;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import android.content.Context;
import android.telephony.SmsManager;
import org.json.*;

/**
 * TMS will receive sms notifications.
 * When an interrupt is accepted, the server will send the text message object to a callback object.
 * @author keiser88
 *
 */
public class TextMessageServer implements TextMessageCallback{
    public static ArrayList<TextMessage> msgQueue;
	TextMessageCallback callback;
	int serverPort;
	ServerSocket socks;
	byte [] txt;
    Context context;
	public TextMessageServer(Context context, int port) {
        this.context = context;
        msgQueue = new ArrayList<TextMessage>(); //save the txt messages for the current session
		socks = null;
		serverPort = port;
		txt = new byte[TextMessage.headerSize + TextMessage.textMessageSize];
		this.callback = callback;
		if(setupServer()) {
			runServer();	
		} else {
			return;
		}
	}
	private boolean setupServer() {
		try{
            socks = new ServerSocket(serverPort, 100);
            socks.setReuseAddress(true);
		} catch(Exception e) {
			System.out.println("failed to bind socket to port:" + serverPort);
			return false;
		}
		return true;
	}
    private boolean containsCRLF(String line) {
        if (line.contains("\r\n\r\n")) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isPOST(String line) {
        String [] pieces = line.split("\n");
        if (pieces[0].contains("POST")) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isOPTIONS(String line) {
        String [] pieces = line.split("\n");
        if (pieces[0].contains("OPTIONS")) {

            return true;
        } else {
            return false;
        }

    }
    private String getValFromKey(StringTokenizer map, String key) {
        String token = "";
        String val = "";
        String line = "";
        while(map.hasMoreTokens()) {
            token = map.nextToken();
            if(token.equalsIgnoreCase(key)) {
                val = map.nextToken();
                line = val;
                if (line.contains(",")) {
                    while (val.contains(",")) {
                        val = map.nextToken();
                        line += " " + val;
                    }
                }
                return line;
            }
        }
        return "";
    }
    private String readClientUntilCRLF(InputStream client) {
        String line = "";
        byte [] bite = new byte[1];
        int count = 99999;
        try {
            while (count > 0) {
                count = client.read(bite);
                line += new String(bite);
                if (containsCRLF(line)) {
                    break; //out
                }
            }
        } catch(Exception eeeeee) {
            return "";
        }
        return line;
    }
    private TextMessage parseTextMessageJSON(String json) {
        TextMessage txt = null;
        try {
            JSONObject msg = new JSONObject(json);
            txt = new TextMessage(msg.getString("number"), msg.getString("content"), msg.getString("author"), msg.getString("recipient"), msg.getString("timestamp"));
        } catch (org.json.JSONException e) {
            errLog("err parsing JSON from POST payload!!!!!");
        }
        return txt;
    }
    private void runServer() {
        if(!socks.isBound()) {
			return;
		}
		Socket client;
        int clientCount = 0;

        //build address book
/*        System.out.println("beginning to search for contacts"); //TESTING!!!*/
        AddressBook contacts = new AddressBook(this.context);
/*
        System.out.println("begin reading all sms messages");*/
        String oldTxtJSON = buildOldTxtJSON(contacts);
        String contactsJSON = buildContactsJSON(contacts);
		while(true) {
			//assume a text message is being sent.....ie only 160 bytes to be received...
			try {
				client = socks.accept();
                errLog("Now serving:" + clientCount);
                clientCount++;

                StringTokenizer headerMap = null;
                InputStream in   = client.getInputStream();
                OutputStream out = client.getOutputStream();
                int maxReceiveSize  = 500;


                String clientRequestHeader = readClientUntilCRLF(in); //request header
                headerMap = new StringTokenizer(clientRequestHeader);
                //determine if POST or GET
                //if POST, create a new text message from the JSON payload and add that msgQueue, then send it using handleTextMEssageInterrupt
                if (isPOST(clientRequestHeader)) {
                    String restURI = getValFromKey(headerMap, "POST");
                    String JSON = "";
                    if (restURI.equals("/message/")) {
                        int payloadSize = 0;
                        errLog("Client is asking to POST JSON payload");

                        String response = "HTTP/1.1 201 CREATED\r\n"+
                                "Content-Type: application/json\r\n" +
                                "Connection: keep-alive\r\n" +
                                "Access-Control-Allow-Origin: *\r\n\r\n";

                        //determine Content-Length: <decimal>
                        String contentLength = getValFromKey(headerMap, "Content-Length:");
                        if (contentLength.length() < 1) {
                            errLog("Client is sending payload of size: who knows?");
                            response = "HTTP/1.1 404 File Not Found\r\n"+
                                    "Access-Control-Allow-Origin: *\r\n\r\n";
                        } else {
                            payloadSize = Integer.parseInt(contentLength);
                            errLog("Client is sending payload of size:" + payloadSize);
                            byte [] payload = new byte[payloadSize];
                            int count = in.read(payload, 0, payloadSize);
                            if (count > 0) {
                                JSON = new String(payload);
                                errLog("Client posted this: " + JSON);
                            } else {
                                errLog("Client posted nothing...");
                            }

                        }

                        out.write(response.getBytes());
                        out.close();
                        sendSMS(parseTextMessageJSON(JSON));
                    }
                } else if (isOPTIONS(clientRequestHeader)) {
                    String restURI = getValFromKey(headerMap, "OPTIONS");
                    if (restURI.equalsIgnoreCase("/message/")) {
                        String responseHeader = "HTTP/1.1 200 OK\r\n" +
                                "Access-Control-Allow-Origin: " + getValFromKey(headerMap, "Origin:") + "\r\n" +
                                "Content-Length: 0\r\n" +
                                "Server: relayerServlet (Java)\r\n" +
                                "Connection: Keep-Alive\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Access-Control-Allow-Headers: " + getValFromKey(headerMap, "Access-Control-Request-Headers:") + "\r\n" +
                                "Access-Control-Allow-Methods: " + getValFromKey(headerMap, "Access-Control-Request-Method")  + "\r\n\r\n";
                        out.write(responseHeader.getBytes());
                        out.close();
                    }
                } else {

                    //Since this servlet only deals with JSON we can assume the content type requested is json or nothing
                    String JSON_PAYLOAD = "";

                    //DONE: determine if /messages/ or /authors/
                    //DONE: /authors/ reads back all contact information. could be a lot.
                    String restURI = getValFromKey(headerMap, "GET");
                    if (restURI.equalsIgnoreCase("/messages/")) {
                        JSON_PAYLOAD = "HTTP/1.1 200 OK\r\n"+
                                "Content-Type: application/json\r\n" +
                                "Connection: keep-alive\r\n" +
                                "Access-Control-Allow-Origin: *\r\n\r\n";
                        JSON_PAYLOAD += "{" + "\"messages\":[";
                        if (msgQueue.size() > 0) {

                            JSON_PAYLOAD += msgQueue.get(0).toJSON();
                            msgQueue.get(0).markAsRead();
                            for(int t = 1 ; t < msgQueue.size(); t++) {
                                JSON_PAYLOAD += "," + msgQueue.get(t).toJSON();
                                msgQueue.get(t).markAsRead();
                            }
                        }
                        JSON_PAYLOAD += "]}\r\n";
                    } else if (restURI.equalsIgnoreCase("/contacts/")) {
                        JSON_PAYLOAD = "HTTP/1.1 200 OK\r\n"+
                                "Content-Type: application/json\r\n" +
                                "Connection: keep-alive\r\n" +
                                "Access-Control-Allow-Origin: *\r\n\r\n";
                        JSON_PAYLOAD += contactsJSON;
                    } else if (restURI.equalsIgnoreCase("/messages-history/")) {
                        JSON_PAYLOAD = "HTTP/1.1 200 OK\r\n"+
                                "Content-Type: application/json\r\n" +
                                "Connection: keep-alive\r\n" +
                                "Access-Control-Allow-Origin: *\r\n\r\n";
                        JSON_PAYLOAD += oldTxtJSON;
                    } else {
                        JSON_PAYLOAD = "HTTP/1.1 404 File Not Found\r\n"+
                                "Access-Control-Allow-Origin: *\r\n\r\n";
                    }
                    out.write(JSON_PAYLOAD.getBytes());
                    out.close();
                }
                in.close();
			} catch(Exception e) {
				errLog("Server has crashed.");
				continue;
			}
		}
	}
    public static boolean push(TextMessage txt) {
        msgQueue.add(txt);
        return true;
    }
    private String buildOldTxtJSON(AddressBook contacts) {
        TextMessage [] txts = new TextMessageHistoryBook(this.context, contacts).getTexts();
        if (txts.length < 1) {
            errLog("Previous text messages were not read!!!");
            return "{\"messages\":[]}\r\n";
        } else {
            String JSON = "{" + "\"messages\":[";
            JSON += txts[0].toJSON();
            for (int txt = 1; txt < txts.length; txt++) {
                JSON += "," + txts[txt].toJSON();
            }
            JSON += "]}\r\n";
            return JSON;
        }
    }
    private String buildContactsJSON(AddressBook book) {
        Contact [] contacts = book.getContacts();
        ArrayList<Contact> contactsWithMessages = new ArrayList<Contact>();
        for (Contact c : contacts) { //FILTER OUT CONTACTS WITH NO MESSAGES
            if (c.hasMessages()) {
                contactsWithMessages.add(c);
            }
        }
        if (contactsWithMessages.size() < 1) {
            errLog("No contacts were found. We cannot start the servlet.");
            return "{" + "\"contacts\":[" + "]}\r\n";
        } else {
            errLog("" + contactsWithMessages.size() + " contacts were found.");
        }

        String contactsJSON = "{" + "\"contacts\":[";
        contactsJSON += contactsWithMessages.get(0).toJSON();
        for (int contact = 1; contact < contactsWithMessages.size(); contact++) {
            contactsJSON += "," + contactsWithMessages.get(contact).toJSON();
        }
        contactsJSON += "]}\r\n";

        return contactsJSON;
    }
    //This method is deprecated
/*	private void createSMS(byte [] bytes) {
		TextMessage msg = new TextMessage();
		byte [] txt    = new byte[TextMessage.textMessageSize]; //limit of 160 characters
		byte [] header = new byte[TextMessage.headerSize];
		int b;
		//copy header section
		for(b = 0; b < header.length ; b++) {
			header[b] = bytes[b];
		}
		//copy txt section
		for(b = 0; b < txt.length ; b++) {
			txt[b] = bytes[b + header.length];
		}
		msg.setHeader(header);
		msg.setTextMessage(0, txt);
		processTextMessage(msg);
	}*/

    public void sendSMS(TextMessage msg) {
        SmsManager mail = SmsManager.getDefault();
        mail.sendTextMessage(msg.getID(), null, msg.getMessage(), null, null);
        TextMessageServer.push(msg);
    }
    private void errLog(String line) {
        System.out.println(line);
    }
}
