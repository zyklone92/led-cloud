package zyklone.LEDCloud;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.mbelling.ws281x.Ws281xLedStrip;
import com.github.mbelling.ws281x.jni.rpi_ws281xConstants;

import zyklone.LEDCloud.mesh.FieldPosition;
import zyklone.LEDCloud.mesh.LedMeshCoordinator;
import zyklone.LEDCloud.patterns.BackgroundPattern;
import zyklone.LEDCloud.patterns.DotPattern;
import zyklone.LEDCloud.patterns.Pattern;
import zyklone.LEDCloud.patterns.RainbowPattern;
import zyklone.LEDCloud.patterns.WallPattern;
import zyklone.LEDCloud.patterns.WavePattern;


/**
 * This class handles the clouds different modes and is in charge of applying changes to
 * the led-matrix depending on the current mode.
 * This class is a runnable and should therefore be run in its own thread.
 * The mode along with other necessary information (e.g. alarm time),
 * may be changed from outside the thread through the use of corresponding methods.
 * Changes to the cloud's LED-strip are mostly applied via the use of so-called pictures,
 * which are nothing more than two-dimensional arrays of Color-objects.
 * These pictures are sent to the classes LedMeshCoordinator, which sends the corresponding
 * commands to the cloud's LED-strip in the correct order.
 * The position of each LED in the matrix can be set via a file (./order.json) which is parsed at startup.
 * 
 * @author Zyklone
 */
public class LEDCloud implements Runnable {
	
	private Ws281xLedStrip lightstrip = null;
	private LedMeshCoordinator coordinator = null;
	private int meshSizeX;
	private int meshSizeY;
	private ArrayList<ArrayList<Color>> comPic;
	private int stripsize = 0;
	private volatile ModeType currentMode;
	private volatile ModeType cacheMode;
	private volatile Color color;
	private volatile Color cacheColor;
	private ArrayList<Pattern> patterns = new ArrayList<>(30);
	private Pattern rainbowPattern = new RainbowPattern(39, 15, 600, 20);
	private boolean addPattern = false;
	private LocalTime alarmTime = null;
	private Random rand = new Random();
	private long lastUIUpdate = 0;
	private boolean idle = false;
	

	/**
	 * Tries to read the file containing the led positioning information (./order.json),
	 * parse it and create a Ws281xLedStrip instance as well as a LedMeshCoordinator instance
	 * from the parsed information. The corresponding class attributes as well as
	 * the maximum values for the cloud's LED-matrix (meshSizeX, meshSizeY) are set accordingly.
	 * Lastly, a BackgroundPattern is added to the list of patterns.
	 */
	public LEDCloud() {
		importLedMeshOrder();
		patterns.add(new BackgroundPattern(meshSizeX, meshSizeY));
	}
	
	/**
	 * Takes a collection of pixels (FieldPosition), extracts the maximum values for rows (y) and columns (x),
	 * and sets the classes mesh-size-attributes accordingly.
	 * @param mesh the collection of pixels (FieldPosition)
	 */
	private void deriveMeshSize(Collection<FieldPosition> mesh) {
		if(mesh == null)
			return;
		meshSizeX = mesh.stream().mapToInt(p -> p.getColumn())
				.max().orElse(0);
		meshSizeX = mesh.stream().mapToInt(p -> p.getRow())
				.max().orElse(0);
	}
	
	/**
	 * Tries to read the file containing the led positioning information (./order.json),
	 * parse it and create a Ws281xLedStrip instance as well as a LedMeshCoordinator instance
	 * from the parsed information. The corresponding class attributes as well as
	 * the maximum values for the cloud's LED-matrix (meshSizeX, meshSizeY) are set accordingly.
	 */
	private void importLedMeshOrder() {
		JSONArray parsedLedMeshOrder = parseFileToJson("./order.json");
		if(parsedLedMeshOrder == null)
			System.exit(-1);
		ArrayList<FieldPosition> order = new ArrayList<>();
		for(Object c : parsedLedMeshOrder) {
			JSONArray field = (JSONArray) c;
			order.add(new FieldPosition(((Long)field.get(0)).intValue(), ((Long)field.get(1)).intValue()));
		}
		System.out.println("LED-Mesh-order successfully imported!");
		this.stripsize = parsedLedMeshOrder.size();
		
		this.lightstrip = new Ws281xLedStrip(stripsize, 21, 800000, 10, 200, 0, false, rpi_ws281xConstants.WS2812_STRIP);
		// current picture-size = row 15, col 39
		this.coordinator = new LedMeshCoordinator(order, lightstrip);
		// initialize mesh-size values
		deriveMeshSize(order);
	}
	
	/**
	 * Tries to read the content of a file and parse it into a json-array.
	 * @param filename the path and name of the file to read
	 * @return the parsed json-array, or null if an error occured
	 */
	private JSONArray parseFileToJson(String filename) {
		String filecontent = null;
		System.out.println("Trying to read content of file " + filename);
		try {
			filecontent = this.readFile(filename);
		} catch(IOException ioe) {
			System.err.println("File could not be read. It probably doesn't exist.");
		}
		if(filecontent == null) {
			System.err.println("Importing the file's content failed.");
			return null;
		}
		System.out.println("File successfully read.");
		JSONParser parser = new JSONParser();
		try {
			return (JSONArray) parser.parse(filecontent);
		} catch (ParseException e) {
			System.err.println("Parsing the file's content failed. Wrong format!");
			System.err.println("Importing the file's content failed.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Tries to read the content of a file.
	 * @param file the path and name of the file to read
	 * @return the content of the file, or null if there was an error
	 * @throws IOException if there was an error finding or reading the file
	 */
	private String readFile(String file) throws IOException {
	    String line = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    String ls = System.getProperty("line.separator");

	    try (BufferedReader reader = new BufferedReader(new FileReader (file))){
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }
	        return stringBuilder.toString();
	    } catch(IOException ioe) {
	    	return null;
	    }
	}
	
	/**
	 * Tells the cloud to display a fixed color.
	 * @param col the Color to be displayed
	 */
	public void setFixedColorMode(Color col) {
		this.currentMode = ModeType.FIXEDCOLOR;
		this.color = col;
		this.idle = false;
	}
	
	/**
	 * Tells the cloud to change the current mode to "Rainbow".
	 */
	public void setRainbowMode() {
		this.currentMode = ModeType.RAINBOW;
		this.idle = false;
	}
	
	/**
	 * Tells the cloud to change the current mode to "Patterns".
	 */
	public void setPatternsMode() {
		this.currentMode = ModeType.PATTERNS;
		this.idle = false;
	}
	
	/**
	 * Tells the cloud to create (and display, if the correct mode is enabled)
	 * an additional pattern (dot, wall or wave).
	 */
	public void addPattern() {
		this.addPattern = true;
	}
	
	/**
	 * Sets the Cloud to flash all LEDs 2 times with the specified color and then restore its prior state.
	 * @param col the color of the notification-flashes
	 */
	public void playNotification(Color col) {
		// save current state
		if(this.currentMode != null && !this.currentMode.equals(ModeType.NOTIFICATION)) {
			this.cacheMode = this.currentMode;
			this.cacheColor = this.color;
		}
		this.currentMode = ModeType.NOTIFICATION;
		this.color = col;
	}
	
	/**
	 * Sets the cloud to wait until the specified time
	 * and then play back an alarm animation (4 minutes fade-in, 2 minutes on, increasingly fast flash).
	 * The alarm will stay on, until the mode is changed.
	 * This means that setting an alarm, while there is already an alarm active, will not have any effect.
	 * Instead, the mode should be changed shortly, and then the new alarm should be applied.
	 * @param time the time, when the alarm animation should be played
	 */
	public void setAlarm(LocalTime time) {
		if(time == null)
			return;
		this.currentMode = ModeType.ALARM;
		this.alarmTime = time;
		this.idle = false;
		System.out.println("Mode successfully set to alarm.");
	}
	
	/**
	 * This feature is not yet implemented.
	 * Sets the Cloud's mode to play patterns along to the recorded audio.
	 */
	public void setMusicMode() {
		this.currentMode = ModeType.MUSIC;
		this.idle = false;
	}
	
	/**
	 * Fades out the currently set color if any,
	 * then flashes the notification-color 2 times and restores the state prior to the notification.
	 */
	private void playNotification() {
		// fade old colors
		System.out.println("Fading old colors.");
		fadeOutCurrentPicture(400);
		
		// play notification
		for(int i = 0; i < 2; i++) {
			fadeInColor(this.color, 600);
			fadeOutColor(this.color, 500);
		}
		
		// restore old state
		this.currentMode = this.cacheMode;
		this.color = this.cacheColor;
		this.idle = false;
	}
	
	/**
	 * Waits until shortly before the alarm-time, slowly fades in a white light,
	 * stays there for 2 minutes and then starts flashing the white light,
	 * starting off slowly and getting more aggressive as time passes.
	 * The cloud will continue to flash the white light until the mode is changed.
	 */
	private void playAlarm() {
		// fade old colors
		System.out.println("Fading old colors.");
		fadeOutCurrentPicture(1000);
		lightstrip.setStrip(0, 0, 0);
		lightstrip.render();
		long timeToAlarmInMs = LocalTime.now().until(this.alarmTime, ChronoUnit.MILLIS);
		long startTime = System.currentTimeMillis();
		long currentTime;
		if(timeToAlarmInMs < 0)
			timeToAlarmInMs += 86400_000;	// add one day, if the time is negative (current time of day is bigger than alarm time)
		if(timeToAlarmInMs > 120_000) {
			timeToAlarmInMs -= 120_000;		// start the animation two minutes before the alarm time
			System.out.println("Waiting " + timeToAlarmInMs + " ms.");
			// wait until alarm time is reached (minus two minutes)
			while(System.currentTimeMillis() - startTime < timeToAlarmInMs && this.currentMode.equals(ModeType.ALARM)) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
			if(!this.currentMode.equals(ModeType.ALARM))
				return;
		}
		startTime = System.currentTimeMillis();
		System.out.println("Alarm time reached. Slowly fading in white light.");
		float fadeInTimeInMs = 240_000f;
		// slowly fade in a white light over a period of 4 minutes
		while((currentTime = System.currentTimeMillis()) - startTime < fadeInTimeInMs && this.currentMode.equals(ModeType.ALARM)) {
			float brightness = (currentTime - startTime)/fadeInTimeInMs;
			System.out.println("Brigthness: " + brightness);
			lightstrip.setStrip((int) (250*brightness), (int) (250*brightness), (int) (250*brightness));
			lightstrip.render();
			try {
				Thread.sleep(1000);	
			} catch (InterruptedException e) {}
		}
		if(!this.currentMode.equals(ModeType.ALARM))
			return;
		System.out.println("White light fully faded in.");
		System.out.println("White light will be on for 2 minutes.");
		startTime = System.currentTimeMillis();
		long onTimeInMs = 120_000;
		// set the cloud to a white light with full brightness for 2 minutes
		while(System.currentTimeMillis() - startTime < onTimeInMs && this.currentMode.equals(ModeType.ALARM)) {
			try {
				Thread.sleep(1000);	
			} catch (InterruptedException e) {}
		}
		if(!this.currentMode.equals(ModeType.ALARM))
			return;
		System.out.println("Soft alarm-playback finished.");
		System.out.println("Getting more aggressive.");
		startTime = System.currentTimeMillis();
		Color white = new Color(255, 255, 255);
		fadeOutColor(white, 10000);
		// start fading in and out a white light, increasing the speed over a period of 4 minutes
		while((currentTime = System.currentTimeMillis()) - startTime < 240_000 && this.currentMode.equals(ModeType.ALARM)) {
			fadeInColor(white, 200 + 24_000*(1-(currentTime - startTime)/240_000));
			fadeOutColor(white, 200 + 24_000*(1-(currentTime - startTime)/240_000));
		}
		// the maximum frequency of the flashing light is 2.5Hz
		while(this.currentMode.equals(ModeType.ALARM) ) {
			fadeInColor(white, 200);
			fadeOutColor(white, 200);
		}
	}
	
	/**
	 * Sets the whole Cloud to one color, according to the last set Color-value.
	 */
	private void setFixedColor() {
		
		lightstrip.setStrip(this.color.getRed(), this.color.getGreen(), this.color.getBlue());
		lightstrip.render();
		this.idle = true;
	}
	
	/**
	 * Fades out the Colors of the last displayed picture over the specified period.
	 * @param timeInMs the time period over which the picture should be faded out
	 */
	private void fadeOutCurrentPicture(long timeInMs) {
		long startTime = System.currentTimeMillis();
		long currentTime;
		while((currentTime = System.currentTimeMillis()) - startTime < timeInMs) {
			float factor = 1 - (currentTime - startTime)/((float)timeInMs);
			if(factor <= 1.0f && factor > 0.0f) {
				for(int i = 0; i < comPic.size(); i++) {
					for(int j = 0; j < comPic.get(i).size(); j++) {
						Color col = comPic.get(i).get(j);
						comPic.get(i).set(j, new Color((int)(col.getRed()*factor), (int)(col.getGreen()*factor), (int)(col.getBlue()*factor)));
					}
				}
				this.coordinator.updateLedMesh(comPic);
			}
			try {
				Thread.sleep(50 + currentTime - System.currentTimeMillis());
			} catch (InterruptedException e) {}
		}
	}
	
	
	/**
	 * Fades in the given Color from minimum to maximum brightness in the specified amount of time
	 * @param col the Color to fade in
	 * @param timeInMs the amount of time in which the Color should be faded from minimum to maximum brightness
	 */
	private void fadeInColor(Color col, long timeInMs) {
		long startTime = System.currentTimeMillis();
		long currentTime;
		while((currentTime = System.currentTimeMillis()) - startTime < timeInMs) {
			float factor = (currentTime - startTime)/((float)timeInMs);
			if(factor <= 1.0f && factor > 0.0f) {
				lightstrip.setStrip((int)(col.getRed()*factor), (int)(col.getGreen()*factor), (int)(col.getBlue()*factor));
				lightstrip.render();
			}
			try {
				Thread.sleep(50 + currentTime - System.currentTimeMillis());
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * Fades out the given Color from maximum to minimum brightness in the specified amount of time
	 * @param col the Color to fade out
	 * @param timeInMs the amount of time in which the Color should be faded from maximum to minimum brightness
	 */
	private void fadeOutColor(Color col, long timeInMs) {
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime < timeInMs) {
			float factor = (System.currentTimeMillis() - startTime)*(-1.0f)/(timeInMs) + 1;
			if(factor <= 1.0f && factor > 0.0f) {
				lightstrip.setStrip((int)(col.getRed()*factor), (int)(col.getGreen()*factor), (int)(col.getBlue()*factor));
				lightstrip.render();
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * Populates the patterns-list with new patterns (by chance, or if there are less than 2),
	 * creates a new "dark" picture (2D-array with black color objects),
	 * adds each pattern to it, one by one
	 * and sends the complete picture (comPic) to the lightstrip.
	 * If a pattern has reached its lifetime, it is removed from the patterns-list.
	 */
	private void processPatterns() {
		if(System.currentTimeMillis() - lastUIUpdate >= 950) {
			System.out.println("Patterns in the list: " + patterns.size());
		}
		 comPic = new ArrayList<>(meshSizeY);
		
		// 2% chance (every frame) to add new pattern, or 100% if less then 2 patterns are being displayed (BasePattern not counted)
		if(rand.nextInt(50) == 0 || patterns.size() <= 2) {
			addPatternToList();
		}
		// initialize dark base picture
		Color black = new Color(0, 0, 0);
		for(int i = 0; i < meshSizeY; i++) {
			ArrayList<Color> row = new ArrayList<Color>(meshSizeX);
			comPic.add(row);
			for(int j = 0; j < meshSizeX; j++) {
				row.add(black);
			}
		}
		// add up all patterns
		for(Iterator<Pattern> it = patterns.iterator(); it.hasNext();) {
			Pattern current = it.next();
			ArrayList<ArrayList<Color>> tempPic = current.getNextPicture();
			// if pattern has reached lifetime, remove it from list
			if(tempPic == null) {
				it.remove();
				System.out.println("Pattern lifetime reached.");
				continue;
			}
			for(int y = 0; y < comPic.size(); y++) {
				for(int x = 0; x < comPic.get(0).size(); x++) {
					Color tempColor = tempPic.get(y).get(x);
					// only add pixel, if it is not dark
					if(tempColor.getRed() != 0 || tempColor.getGreen() != 0 || tempColor.getBlue() != 0) {
						Color comColor = comPic.get(y).get(x);
						int red = comColor.getRed() + tempColor.getRed();
						if(red > 255)
							red = 255;
						int green = comColor.getGreen() + tempColor.getGreen();
						if(green > 255)
							green = 255;
						int blue = comColor.getBlue() + tempColor.getBlue();
						if(blue > 255)
							blue = 255;
						comPic.get(y).set(x, new Color(red, green, blue));
					}
				}
			}
		}
		// display complete picture
		coordinator.updateLedMesh(comPic);
	}
	
	/**
	 * Plays an intro and then continuously calculates and displays "pictures" according to the selected mode.
	 */
	@Override
	public void run() {
		if(this.lightstrip == null || this.coordinator == null
				|| this.meshSizeX == 0 || this.meshSizeY == 0)
			return;
		
		//long patternChangeTime = 0;	// forgot what I wanted to do with this
		
		playIntro();
		
		long currentTime = 0;
		while(true) {
			currentTime = System.currentTimeMillis();
			
			processMode();
			
			if(this.addPattern) {
				addPatternToList();
				this.addPattern = false;
			}
			
			long calculationTime = System.currentTimeMillis() - currentTime;
			// add in a pause, whose length is dependent upon the time needed to calculate and display the next picture,
			// to ensure a stable frame-rate
			if(!this.idle && calculationTime < 50) {
				try {
					Thread.sleep(50-calculationTime);
				} catch (InterruptedException e) {}
			}
			// for monitoring performance. atm only the last calculation-time is printed.
			// Could be improved by adding average, min and max time.
			if(System.currentTimeMillis()-lastUIUpdate > 1000) {
				System.out.println("Calculating one complete picture took " + calculationTime + "ms.");
				lastUIUpdate = System.currentTimeMillis();
			}
		}
	}

	/**
	 * Creates a new pattern and adds it to the patterns-list.<br>
	 * The type of pattern to be added is randomly chosen
	 * and the chance for each type to be selected is weighted differently.
	 */
	private void addPatternToList() {
		int patternGenerator = rand.nextInt(100);
		if(patternGenerator < 45)
			patterns.add(new DotPattern(meshSizeX, meshSizeY));
		else if(patternGenerator < 80)
			patterns.add(new WallPattern(meshSizeX, meshSizeY));
		else
			patterns.add(new WavePattern(meshSizeX, meshSizeY));
		System.out.println("Added new Pattern.");
	}

	/**
	 * Plays the RainbowPattern for 5 seconds and then shuts off all LEDs.
	 */
	private void playIntro() {
		long currentTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - currentTime) < 5000) {
			coordinator.updateLedMesh(this.rainbowPattern.getNextPicture());
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
		System.out.println("Intro finished.");
		this.setFixedColorMode(new Color(0, 0, 0));
	}

	/**
	 * Sets / Advances the LEDs according to the last set status, or just waits 500ms if there is nothing to do.
	 */
	private void processMode() {
		if(currentMode != null && !this.idle) {
			switch(currentMode) {
				case FIXEDCOLOR: setFixedColor();
					break;
				case RAINBOW: coordinator.updateLedMesh(this.rainbowPattern.getNextPicture());
					break;
				case PATTERNS: processPatterns();
					break;
				case MUSIC: ;	// TODO add microphone to the Pi and play patterns to the sampled audio
					break;
				case NOTIFICATION: playNotification();
					break;
				case ALARM: playAlarm();
					break;
			}
		} else {
			// If the Mode has not been set yet (i.e. after startup),
			// or there is a fixed color being displayed, there is nothing to do, so there can be a long pause
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}


	

}
