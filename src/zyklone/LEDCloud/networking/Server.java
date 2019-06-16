package zyklone.LEDCloud.networking;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import zyklone.LEDCloud.LEDCloud;

/**
 * The projects starting-class.<br>
 * This class instantiates an LEDCloud-object, and listens on an open tcp-port (6639 by default) for new connections.
 * It reads a message from an accepted message and updates the LEDCloud's status according to the message's content.
 * 
 * @author Zyklone
 */
public class Server{
	
	private LEDCloud ledCloud;
	private static JSONParser parser = new JSONParser();
	private int port = 6639;
	private ServerSocket acceptor = null;
	private Socket connection;
	private InputStreamReader inputStream;

	private Server() {
		this.ledCloud = new LEDCloud();
		start();
	}
	
	private Server(int port) {
		this.port = port;
		this.ledCloud = new LEDCloud();
	}
	
	/**
	 * Loops endlessly, waiting for new json-messages to update the status of the LEDCloud.<br>
	 * First the LEDCloud-Runnable is started in a new thread.
	 * Then an endless loop waits for new incoming connections and accepts them.
	 * The connected client has 5 seconds to send a message, before the connection is automatically terminated.
	 * If a message was received, a valid command is extracted if possible, and the LEDCloud's status is updated accordingly.
	 * The connection is then terminated.
	 */
	private void start() {
		new Thread(ledCloud).start();
		try {
			acceptor = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true) {
			System.out.println("Listening for new connections.");
			acceptConnection();
			try {
				char[] inputBuffer = new char[500];
				if(inputStream.read(inputBuffer, 0, 500) != -1) {
					String message = (new String(inputBuffer)).trim();
					System.out.println("Message received: " + message);
					//process message
					processCommand(message);
				}
				closeConnection();
			} catch (IOException ioe) {
				System.out.println("Connection timeout. No message recieved after 5 seconds.");
				closeConnection();
			}
		}
	}
	
	/**
	 * Closes the Socket and its inputStream, if it is not closed already.
	 */
	protected void closeConnection() {
		if(this.inputStream!= null) {
			try {
				this.inputStream.close();
			} catch(IOException ioe) {}
		}
		if(this.connection != null) {
			try {
				this.connection.close();
			} catch(IOException ioe) {}
		}
	}
	
	/**
	 * Takes the incoming message, tries to convert it into a json-object and extract a valid command from it.
	 * If the contained information can be parsed successfully and is complete,
	 * the cloud's status is set accordingly.
	 * @param message the json-string to process
	 */
	private void processCommand(String message){
		JSONObject json = extractValidJSON(message);
		if(json == null)
			return;
		if(!json.containsKey("command"))
			return;
		switch((String)json.get("command")){
			case "fixed-color": ledCloud.setFixedColorMode(extractColorFromJSON(json));
				break;
			case "rainbow": ledCloud.setRainbowMode();
				break;
			case "patterns": ledCloud.setPatternsMode();
				break;
			case "add-pattern": ledCloud.addPattern();
				break;
			case "music": ledCloud.setMusicMode();
				break;
			case "notification": ledCloud.playNotification(extractColorFromJSON(json));
				break;
			case "alarm": processAlarmCommand(json);
		}
	}
	
	/**
	 * Tries to extract a time out of the passed JSON-Object, parse it and set the clouds status to alarm-mode.
	 * @param json the json-object that should contain the alarm time
	 */
	private void processAlarmCommand(JSONObject json) {
		if(!json.containsKey("time")) {
			System.out.println("The received alarm command did not contain a time. Ignoring the command.");
			return;
		}
		try {
			ledCloud.setAlarm(LocalTime.parse((String) json.get("time")));
		} catch(DateTimeParseException dtpe) {
			System.out.println("The received time for the alarm has an invalid format. Ignoring the command.");
		}
	}
	
	/**
	 * Tries to extract a Color-object from a json-object.
	 * @param json the json-object the Color should be extracted from
	 * @return the extracted Color-object, or null if no Color-object could be created from the passed json-object
	 */
	private Color extractColorFromJSON(JSONObject json) {
		if(json.get("red") instanceof Long && json.get("green") instanceof Long && json.get("blue") instanceof Long) {
			int red = ((Long)json.get("red")).intValue();
			if(red < 0)
				red = 0;
			if(red > 255)
				red = 255;
			int green = ((Long)json.get("green")).intValue();
			if(green < 0)
				green = 0;
			if(green > 255)
				green = 255;
			int blue = ((Long)json.get("blue")).intValue();
			if(blue < 0)
				blue = 0;
			if(blue > 255)
				blue = 255;
			return new Color(red, green, blue);
		}
		return null;
	}
	
	/**
	 * Tries to extract a valid json-message out of a String and convert it into a json-object.
	 * @param originalMessage the message that should be converted into a json-object.
	 * @return the extracted json-object, or null if the message does not contain a valid json-string
	 */
	private JSONObject extractValidJSON(String originalMessage)
	{
		if(originalMessage == null || !originalMessage.contains("{") || !originalMessage.contains("}"))
			return null;
		
		String extractedJSONString = "";
		String substring = "";
        int depth = 0;
        // search for a complete json-message inside of the string, based on braces (curly brackets)
        // if the string contains more than one valid json-message, the last one is chosen
        for(Character c : originalMessage.toCharArray())
        {
            if(c.equals('{'))
                depth++;

            if(depth > 0)
                substring = substring.concat(c.toString());
            //System.out.println("Extracting: " + substring);
            
            if(c.equals('}') && depth != 0)
                depth--;
            
            if(depth == 0 && !substring.isEmpty()) {
                extractedJSONString = new String(substring);
                substring = "";
            }
        }
        
        try {
        	return (JSONObject) parser.parse(extractedJSONString);
        } catch (ParseException e) {
			System.err.println("Reading coin-infos from the file failed. Wrong format!");
			System.err.println("Importing coin-infos from file failed.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Waits until there is a new incoming connect-request, accepts the connection and sets the classes inputStream accordingly.
	 * The read-timeout is set to 5 seconds.
	 * This is used as a threshold in which the client has to send a message, before the connection is terminated.
	 */
	private void acceptConnection() {
		try {
			connection = acceptor.accept();
			connection.setSoTimeout(5000);
			this.inputStream = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
			System.out.println("New connection accepted.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//LEDCloud cloud = new LEDCloud();
		Server server = new Server();
		//new Thread(cloud).start();
		server.start();
	}
}
