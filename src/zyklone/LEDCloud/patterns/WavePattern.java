package zyklone.LEDCloud.patterns;

import java.awt.Color;
import java.util.ArrayList;


/**
 * This specialization of LocalPattern represents a circular wave like it would be created
 * by a spherical object, dropping into a pool of liquid.
 * The super-classes thickness attribute is used to represent the traveling waves thickness.
 * 
 * @author Zyklone
 */
public class WavePattern extends LocalPattern {
	
	private float radius = 2;	// starting (outer) radius of the circular wave

	/**
	 * This constructor initializes the Pattern with its default values.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 */
	public WavePattern(int pictureSizeX, int pictureSizeY) {
		super(pictureSizeX, pictureSizeY, 8000, null, null, null, Float.valueOf(0.6f));
		initialize();
	}

	/**
	 * This constructor initializes the Pattern with certain default values,
	 * depending on if it is part of a BackgroundPattern or not.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 * @param backgroundPattern true, if this pattern is part of a BackgroundPattern, otherwise false
	 */
	public WavePattern(int pictureSizeX, int pictureSizeY, boolean backgroundPattern) {
		this(pictureSizeX, pictureSizeY);
		if(backgroundPattern) {
			super.setBackgroundPatternColor();
			this.glow += 0.15f;
			this.velocity -= 0.6f;
		}
	}
	
	/**
	 * Initializes the WavePattern's thickness, velocity and also the x-coordinate of its origin,
	 * to not be in the vicinity of either end.
	 * The velocity is set to a value dependent on the Patterns lifetime.
	 * The longer the lifetime, the lower the velocity.
	 */
	private void initialize() {
		this.currentX = rand.nextInt(pictureSizeX-20) + 10;
		this.thickness = (rand.nextInt(3) + 2);								// default thickness of 2 to 4 units
		this.velocity = ((float) rand.nextInt(30) * 350/lifespan) + 1.5f;	// max velocity = 4.125 units / s
	}
	

	/**
	 * Draws the pattern in the form of a circular wave like it would be created
	 * by a spherical object, dropping into a pool of liquid.
	 * The thickness attribute is used to represent
	 * the thickness of the outwards (from the origin) traveling wave.
	 */
	@Override
	protected void drawPattern() {
		// y coordinates containing complete dot
		int yBorderLow = (int) (currentY - radius);
		int yBorderHigh = (int) (currentY + radius);
		for(int y = yBorderLow; y <= yBorderHigh; y++) {
			// x coordinates containing complete dot
			for(int x = (int)(currentX - radius); x <= (currentX + radius); x++) {
				// check if coordinates are actually within the dot
				float yDistance = (float)Math.pow((y*1.2f - currentY), 2);
				float centerDistance = (float)Math.sqrt(Math.pow((x - currentX), 2) + yDistance); // y-axis has to travel slower
				if(centerDistance < radius && centerDistance > radius - thickness && yDistance <= pictureSizeY) {
					drawPixel(x, y);
				}
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<ArrayList<Color>> getNextPicture(){
		if(this.startTime == 0) {
			this.currentTime = System.currentTimeMillis();
			this.startTime = currentTime;
		}
		
		newTime = System.currentTimeMillis();
		// if the pattern has reached its lifetime, return null
		if(newTime - this.startTime > this.lifespan)
			return null;
	
		super.fadeOutPicture();
		
		if(newTime - this.startTime < this.lifespan - 1000) {
			radius += (((float)(newTime - currentTime))/1000) * velocity;
			
			// draw the wave
			drawPattern();
			currentTime = newTime;
		}
		
		// apply a fade in to the calculated picture, if it is younger than 1 second
		if(newTime - startTime < 1000)
			super.fadeInPicture();
		
		return this.picture;
	}

	

}
