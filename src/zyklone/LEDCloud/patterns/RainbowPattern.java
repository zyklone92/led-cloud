package zyklone.LEDCloud.patterns;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * The RainbowPattern class represents a pattern across the cloud's whole led-matrix.
 * It uses a static list with gradually changing colors, that imitates the colors of a rainbow,
 * to display a portion of this list across the cloud, parallel to the x-axis.
 * The density of the colors and the velocity the pattern travels across the x-axis,
 * can be set upon instantiation.
 * 
 * @author Zyklone
 */
public class RainbowPattern extends Pattern {
	
	private static List<Color> rainbowList = new ArrayList<>(1540);
	private float velocity = 400;
	private int density = 20;
	private float progress = 0;
	private long lastUpdate;

	/**
	 * This constructor initializes the Pattern with its default values.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 */
	public RainbowPattern(int pictureSizeX, int pictureSizeY) {
		super(pictureSizeX, pictureSizeY);
		if(rainbowList.isEmpty())
			initialize();
	}
	
	/**
	 * This constructor initializes the Pattern with a default density and the passed velocity.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 * @param velocity the velocity the pattern travels across the cloud's x-axis
	 */
	public RainbowPattern(int pictureSizeX, int pictureSizeY, float velocity) {
		super(pictureSizeX, pictureSizeY);
		this.velocity = velocity;
		if(rainbowList.isEmpty())
			initialize();
	}
	
	/**
	 * This constructor initializes the Pattern with the passed velocity and density.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 * @param velocity the velocity the pattern travels across the cloud's x-axis
	 * @param density the density of the rainbow-colors that are displayed on the cloud.
	 */
	public RainbowPattern(int pictureSizeX, int pictureSizeY, float velocity, int density) {
		super(pictureSizeX, pictureSizeY);
		this.velocity = velocity;
		this.density = density;
		if(rainbowList.isEmpty())
			initialize();
	}
	
	/**
	 * Initializes the classes static list with gradually changing colors,
	 * that imitates the colors of a rainbow.
	 */
	public void initialize() {
		// fill the rainbow-list
		int red=255, green=0, blue=0;
		for(green=0; green<255; green++) {
			rainbowList.add(new Color(red, green, blue));
		}
		for(red=255; red>0; red--) {
			rainbowList.add(new Color(red, green, blue));
		}
		for(blue=0; blue<255; blue++) {
			rainbowList.add(new Color(red, green, blue));
		}
		for(green=255; green>0; green--) {
			rainbowList.add(new Color(red, green, blue));
		}
		for(red=0; red<255; red++) {
			rainbowList.add(new Color(red, green, blue));
		}
		for(blue=255; blue>0; blue--) {
			rainbowList.add(new Color(red, green, blue));
		}
		System.out.println("Rainbowlist has " + rainbowList.size() + " elements.");
	}
	
	/**
	 * Calculates and returns the next picture.
	 * The new position of the pattern is dependent on the time passed since the last calculation.
	 * @return the last calculated picture of this pattern.
	 */
	@Override
	public ArrayList<ArrayList<Color>> getNextPicture() {
		if(this.lastUpdate == 0)
			this.lastUpdate = System.currentTimeMillis();
		
		//advance progress depending on passed time since last update
		progress = (progress + ((float)(System.currentTimeMillis() - lastUpdate))/1000 * velocity) % rainbowList.size();
		//System.out.println("Progress " + progress);
		
		// draw picture
		for(int x = 0; x < this.pictureSizeX; x++) {
			Color col = rainbowList.get(((int)progress+x*density) % rainbowList.size());
			for(int y = 0; y < this.pictureSizeY; y++) {
				this.picture.get(y).set(x, col);
			}
		}
		lastUpdate = System.currentTimeMillis();
		
		return picture;
	}

}
