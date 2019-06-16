package zyklone.LEDCloud;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.mbelling.ws281x.Ws281xLedStrip;
import com.github.mbelling.ws281x.jni.rpi_ws281xConstants;

import zyklone.LEDCloud.mesh.FieldPosition;
import zyklone.LEDCloud.mesh.LedMeshCoordinator;
import zyklone.LEDCloud.patterns.*;


/**
 * This class may be used for testing out new algorithms.
 * 
 * @author Zyklone
 */
public class Tester {

	public Tester() {
	}
	
	public JSONArray parseLedMeshOrder(String filename) {
		String filecontent = null;
		System.out.println("Trying to read content of file " + filename);
		try {
			filecontent = this.readFile(filename);
		} catch(IOException ioe) {
			System.err.println("File could not be read. It probably doesn't exist.");
		}
		if(filecontent == null) {
			System.err.println("Importing coin-infos from file failed.");
			return null;
		}
		System.out.println("File successfully read.");
		JSONParser parser = new JSONParser();
		try {
			return (JSONArray) parser.parse(filecontent);
		} catch (ParseException e) {
			System.err.println("Reading coin-infos from the file failed. Wrong format!");
			System.err.println("Importing coin-infos from file failed.");
			e.printStackTrace();
			return null;
		}
	}
	
	private String readFile(String file) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}

	public static void main(String[] args) {
		
		System.out.println("This is a test");
		Tester test = new Tester();
		JSONArray parsedLedMeshOrder = test.parseLedMeshOrder("./order.json");
		if(parsedLedMeshOrder == null)
			System.exit(-1);
		ArrayList<FieldPosition> order = new ArrayList<>();
		for(Object c : parsedLedMeshOrder) {
			JSONArray field = (JSONArray) c;
			order.add(new FieldPosition(((Long)field.get(0)).intValue(), ((Long)field.get(1)).intValue()));
		}
		System.out.println("LED-Mesh-order successfully imported!");
		int stripsize = parsedLedMeshOrder.size();
		//int stripsize = 208;
		
		Ws281xLedStrip lightstrip = new Ws281xLedStrip(stripsize, 21, 800000, 10, 150, 0, false, rpi_ws281xConstants.WS2812_STRIP);
		
		// current picture-size = row 15, col 39
		LedMeshCoordinator coordinator = new LedMeshCoordinator(order, lightstrip);
		
		lightstrip.setStrip(100, 00, 00);
		lightstrip.render();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {}
		
		lightstrip.setStrip(00, 100, 00);
		lightstrip.render();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {}
		
		lightstrip.setStrip(00, 00, 100);
		lightstrip.render();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {}
		
		lightstrip.setStrip(100, 100, 100);
		lightstrip.render();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {}
		
		
		ArrayList<ArrayList<Color>> picture = new ArrayList<>();
		
		// initialize dark picture
		for(int i = 0; i < 15; i++) {
			ArrayList<Color> row = new ArrayList<Color>();
			picture.add(row);
			for(int j = 0; j < 39; j++) {
				row.add(new Color(0, 0, 0));
			}
		}
		coordinator.updateLedMesh(picture);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		
		//middle blue, ends red
		for(int i = 0; i < 15; i++) {
			for(int j = 0; j < 9; j++) {
				picture.get(i).set(j, new Color(255, 0, 0));
			}
		}
		for(int i = 0; i < 15; i++) {
			for(int j = 9; j < 30; j++) {
				picture.get(i).set(j, new Color(0, 0, 255));
			}
		}
		for(int i = 0; i < 15; i++) {
			for(int j = 30; j < 39; j++) {
				picture.get(i).set(j, new Color(255, 0, 0));
			}
		}
		coordinator.updateLedMesh(picture);
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {}
		
		//moving dot
//		for(int i = 0; i < 10; i++) {
//			System.out.println("Drawing dot " + i);
//			Pattern dot = new Pattern(39, 15, PatternType.DOT);
//			ArrayList<ArrayList<Color>> picture2;
//			while((picture2 = dot.getNext()) != null) {
//				System.out.println("Returned picture is not null");
//				coordinator.updateLedMesh(picture2);
//				try {
//					Thread.sleep(50);
//				} catch (InterruptedException e) {}
//			}
//			System.out.println("Dot animation complete!");
//		}
		
		// random moving dots and walls
		Random rand = new Random();
		ArrayList<Pattern> patterns = new ArrayList<>();
		patterns.add(new BackgroundPattern(39, 15));
		Long time = System.currentTimeMillis();
		boolean calcTime = false;
		Long startTime = System.currentTimeMillis();
		//Long baseUpdateTime = System.currentTimeMillis();
		ArrayList<ArrayList<Color>> basePic = new ArrayList<>();
		// initialize cloud picture
		for(int i = 0; i < 15; i++) {
			ArrayList<Color> row = new ArrayList<Color>();
			basePic.add(row);
			for(int j = 0; j < 39; j++) {
//				row.add(new Color(6, 2, 20));
				row.add(new Color(0, 0, 0));
			}
		}
		//while((System.currentTimeMillis() - time) < 120000) {
		while(time > 1000) {
			startTime = System.currentTimeMillis();
			if(System.currentTimeMillis() - time > 1000) {
				System.out.println("Patterns in the list: " + patterns.size());
				calcTime = true;
				time = System.currentTimeMillis();
			}
			ArrayList<ArrayList<Color>> comPic = new ArrayList<>();
			if(rand.nextInt(60) == 0 || patterns.size() < 1) {
			//if(rand.nextInt(500) == 0) {
				switch(rand.nextInt(3)){
					case 0: patterns.add(new DotPattern(39, 15));
						break;
					case 1: patterns.add(new WallPattern(39, 15));
						break;
					case 2: patterns.add(new WavePattern(39, 15));
						break;
				}
				System.out.println("Added new Pattern.");
			}
			// advance base picture
//			if(System.currentTimeMillis() - baseUpdateTime > 200) {
//				for(int y = 0; y < basePic.size(); y++) {
//					for(int x = 0; x < basePic.get(0).size(); x++) {
//						Color tempColor = basePic.get(y).get(x);
//						int red = tempColor.getRed()+rand.nextInt(3)-1;
//						int green = tempColor.getGreen()+rand.nextInt(3)-1;
//						int blue = tempColor.getBlue()+rand.nextInt(5)-2;
//						red = red > 255 ? 255 : red;
//						blue = blue > 255 ? 255 : blue;
//						green = green > 255 ? 255 : green;
//						basePic.get(y).set(x, new Color(red < 0 ? 0 : red, green < 0 ? 0 : green, blue < 0 ? 0 : blue));
//					}
//				}
//				baseUpdateTime = System.currentTimeMillis();
//			}
			// get base picture to work with
			for(int i = 0; i < basePic.size(); i++) {
				ArrayList<Color> row = new ArrayList<Color>();
				comPic.add(row);
				for(int j = 0; j < basePic.get(0).size(); j++) {
					row.add(basePic.get(i).get(j));
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
			if(calcTime) {
				System.out.println("Calculating one complete picture took " + (System.currentTimeMillis() - time) + "ms.");
				calcTime = false;
			}
			startTime = System.currentTimeMillis();
			while((System.currentTimeMillis() - startTime) < 50) {
				try {
					Thread.sleep(3);
				} catch (InterruptedException e) {}
			}
		}
		
		// progressing bar
		time = System.currentTimeMillis();
		int progress = 0;
		while((System.currentTimeMillis() - time) < 20000) {
			for(int i = 0; i < 15; i++) {
				for(int j = 0; j < 39; j++) {
					picture.get(i).set(j, new Color(j == progress ? 255 : 0, 0, 0));
				}
			}
			coordinator.updateLedMesh(picture);
			progress = progress < 38 ? progress+1 : 0;
			try {
				if(System.currentTimeMillis() - time < 1000)
					Thread.sleep(60);
				else
					Thread.sleep((int)(60/((float)(System.currentTimeMillis() - time)/1000)));
			} catch (InterruptedException e) {}
		}
		
		// white sawtooth
		for(int x = 0; x < 1500; x++) {
			for(int i = 0; i < 15; i++) {
				for(int j = 0; j < 39; j++) {
					picture.get(i).set(j, new Color(picture.get(i).get(j).getRed() < 255 ? picture.get(i).get(j).getRed()+1 : 0, picture.get(i).get(j).getGreen() < 255 ? picture.get(i).get(j).getGreen()+1 : 0, picture.get(i).get(j).getBlue() < 255 ? picture.get(i).get(j).getBlue()+1 : 0));
				}
			}
			coordinator.updateLedMesh(picture);
			try {
				Thread.sleep(6);
			} catch (InterruptedException e) {}
			
			//sawtooth, with random initial values
			if(x == 512) {
				Random rand2 = new Random();
				for(int i = 0; i < 15; i++) {
					for(int j = 0; j < 39; j++) {
						picture.get(i).set(j, new Color(rand2.nextInt(255), rand2.nextInt(255), rand2.nextInt(255)));
					}
				}
				coordinator.updateLedMesh(picture);
			}
		}
		
		System.exit(0);
		
		
//		List<Color> colors = new LinkedList<>();
//		int i=254, j=0, k=0;
//
//		for(j=0; j<254; j++) {
//			i--;
//			colors.add(new Color(i, j, k));
//		}
//		for(k=0; k<254; k++) {
//			j--;
//			colors.add(new Color(i, j, k));
//		}
//		for(; k>0; k--) {
//			i++;
//			colors.add(new Color(i, j, k));
//		}
//		
//		int status = 0;
//		Color col = null;
//		Random rand = new Random();
//		while(true) {
//			for(int x = 0; x<stripsize; x++) { 							// for all LEDs
//				col = colors.get((status + 10*x)%colors.size());	// Color"density"
//				boolean strobe = rand.nextInt(10000)<4;
//				lightstrip.setPixel(x, strobe ? 255 : col.getRed(), strobe ? 255 : col.getGreen(), strobe ? 255 : col.getBlue());
//				//lightstrip.setPixel(x, col.getRed(), col.getGreen(), col.getBlue());
//			}
//			if(status >= colors.size())
//				status = 0;
//			else
//				status= status + 10;				// same as color"density"
//
//			lightstrip.render();
//			try {
//				Thread.sleep(30);					// Animation speed
//			} catch (InterruptedException e) {}
//		}
		
		
	}

}
