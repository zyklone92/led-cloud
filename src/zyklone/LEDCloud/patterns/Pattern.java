package zyklone.LEDCloud.patterns;

import java.awt.Color;
import java.util.ArrayList;

/**
 * This class represents the interface between the LEDCloud and all possible patterns,
 * for the LEDCloud to have a way of accessing the patterns' current pictures and sending them to the led-strip.
 * 
 * @author Zyklone
 */
public abstract class Pattern{
	
	protected ArrayList<ArrayList<Color>> picture;
	protected int pictureSizeX;
	protected int pictureSizeY;
	protected static Color black = new Color(0, 0, 0);
	
	public Pattern(int pictureSizeX, int pictureSizeY) {
		this.pictureSizeX = pictureSizeX;
		this.pictureSizeY = pictureSizeY;
		initializePicture();
	}

	/**
	 * @return the patterns latest picture, or null if the picture is unable to produce any more pictures
	 * (e.g. if pattern-lifetime is reached)
	 */
	public abstract ArrayList<ArrayList<Color>> getNextPicture();
	
	/**
	 * Initializes the picture by creating the required lists
	 * and setting all the picture's pixels to black (i.e. dark).
	 */
	protected void initializePicture() {
		this.picture = new ArrayList<>(pictureSizeY);
		for(int i = 0; i < this.pictureSizeY; i++) {
			ArrayList<Color> row = new ArrayList<Color>(pictureSizeX);
			picture.add(row);
			for(int j = 0; j < pictureSizeX; j++) {
				row.add(black);
			}
		}
	}
	
}
