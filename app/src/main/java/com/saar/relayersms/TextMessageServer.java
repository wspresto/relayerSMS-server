package com.saar.relayersms;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import android.telephony.SmsManager;
import org.json.*;
import java.util.StringTokenizer;

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
	public TextMessageServer(int port) {
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
    private boolean isPUT(String line) {
        if (line.contains("PUT")) {

            return true;
        } else {
            return false;
        }

    }
    private String getValFromKey(StringTokenizer map, String key) {
        String token = "";
        while(map.hasMoreTokens()) {
            token = map.nextToken();
            if(token.equalsIgnoreCase(key)) {
                return map.nextToken();
            }
        }
        return "";
    }
    private TextMessage parseTextMessageJSON(String json) {
        TextMessage txt = null;
        try {
            JSONObject msg = new JSONObject(json);
            txt = new TextMessage(msg.getString("id"), msg.getString("content"), msg.getString("author"), msg.getString("timestamp"));
        } catch (org.json.JSONException e) {
            errLog("err parsing JSON from PUT payload!!!!!");
        }
        return txt;
    }
	/**
	 * assumes socks is ready to go!
	 * assumes server socket is already binded.
	 */
	private void runServer() {
		if(!socks.isBound()) {
			return;
		}
		Socket client;
        int clientCount = 0;
		while(true) {

			//assume a text message is being sent.....ie only 160 bytes to be received...
			try {
				client = socks.accept();
                errLog("Now serving:" + clientCount);
                clientCount++;

                StringTokenizer headerMap = null;
                InputStream in   = client.getInputStream();

                int maxReceiveSize  = 500;

                byte [] bite = new byte[1];
                int count = 99999;
                String clientRequestHeader = ""; //request header
                while (count > 0) {
                    count = in.read(bite);
                    errLog("checking");
                    clientRequestHeader += new String(bite);
                    if (containsCRLF(clientRequestHeader)) {
                        break; //out
                    }
                }
                headerMap =  new StringTokenizer(clientRequestHeader);
                //determine if PUT or GET
                //if PUT, create a new text message from the JSON payload and add that msgQueue, then send it using handleTextMEssageInterrupt
                if (isPUT(clientRequestHeader)) {
                    int payloadSize = 0;
                    errLog("Client is asking to PUT JSON payload");
                    //determine Content-Length: <decimal>
                    payloadSize = Integer.parseInt(getValFromKey(headerMap, "Content-Length"));
                    errLog("Client is sending payload of size:" + payloadSize);
                    byte [] payload = new byte[payloadSize];
                    count = in.read(payload, 0, payloadSize);
                    sendSMS(parseTextMessageJSON(new String(payload)));
                } else {
                    //TODO: determine if /messages/ or /authors/
                    //TODO: /authors/ reads back all contact information. could be a lot. BIG TODO!!!

                    OutputStream out = client.getOutputStream();
                    String JSON_PAYLOAD = "HTTP/1.1 200 OK\r\n"+
                            "Content-Type: application/json\r\n" +
                            "Connection: keep-alive\r\n" +
                            "Access-Control-Allow-Origin: *\r\n\r\n";

                    JSON_PAYLOAD += "{" + "\"messages\":[";
                    if (msgQueue.size() > 0) {
                        JSON_PAYLOAD += msgQueue.get(0).toJSON();
                        for(int t = 1 ; t < msgQueue.size(); t++) {
                            JSON_PAYLOAD += "," + msgQueue.get(t).toJSON();
                        }
                    }
                    JSON_PAYLOAD += "]}\r\n";
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

    }
    private void errLog(String line) {
        System.out.println(line);
    }
}
