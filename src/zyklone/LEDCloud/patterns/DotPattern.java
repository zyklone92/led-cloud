package zyklone.LEDCloud.patterns;

/**
 * This specialization of LocalPattern represents a fully lit circle.
 * @author Zyklone
 */
public class DotPattern extends LocalPattern {

	/**
	 * This constructor initializes the Pattern with its default values.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 */
	public DotPattern(int pictureSizeX, int pictureSizeY) {
		super(pictureSizeX, pictureSizeY);
	}
	
	/**
	 * This constructor initializes the Pattern with certain default values,
	 * depending on if it is part of a BackgroundPattern or not.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 * @param backgroundPattern true, if this pattern is part of a BackgroundPattern, otherwise false
	 */
	public DotPattern(int pictureSizeX, int pictureSizeY, boolean backgroundPattern) {
		super(pictureSizeX, pictureSizeY, 0, 
				backgroundPattern ? Integer.valueOf(11) : null,				// maxSize
				backgroundPattern ? Float.valueOf(6.0f) : Float.valueOf(-1), 	// maxVelocity
				backgroundPattern ? Float.valueOf(1.0f) : Float.valueOf(-1), 	// maxAcceleration
				backgroundPattern ? Float.valueOf(0.7f) : Float.valueOf(-1)); // glow
		if(backgroundPattern) 
			super.setBackgroundPatternColor();
	}
	
	/**
	 * This constructor allows the passed parameters to set most of the object's attributes.
	 * If a null value is passed, the corresponding attributes are set to null
	 * (except for the size, which would be initialized with a default value).
	 * If an invalid value is passed, the corresponding attributes are initialized with default values.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 * @param maxLifetimeInMs the maximum lifetime the instance of this class should have
	 * @param maxSize the maximum size this pattern should have
	 * @param maxVelocity the the maximum velocity this pattern should have
	 * @param maxAcceleration the the maximum acceleration this pattern should have
	 * @param averageGlow a rough value of the amount of glow this pattern should have
	 * @param backgroundPattern true, if this pattern is part of a BackgroundPattern, otherwise false
	 */
	public DotPattern(int pictureSizeX, int pictureSizeY, int maxLifetimeInMs, Integer maxSize, Float maxVelocity, Float maxAcceleration, Float averageGlow,
			boolean backgroundPattern) {
		super(pictureSizeX, pictureSizeY, maxLifetimeInMs, maxSize, maxVelocity, maxAcceleration, averageGlow);
		if(backgroundPattern) 
			super.setBackgroundPatternColor();
	}
	
	/**
	 * Draws the pattern in the form of a dot / fully filled circle with the size-attribute as its diameter.
	 */
	@Override
	protected void drawPattern() {
		// y coordinates containing complete dot
		for(int y = (int)(currentY - ((float)size/2)); y <= (currentY + ((float)size/2)); y++) {
			// x coordinates containing complete dot
			for(int x = (int)(currentX - ((float)size/2)); x <= (currentX + ((float)size/2)); x++) {
				// check if coordinates are actually within the dot
				if(Math.sqrt(Math.pow((x - currentX), 2) + Math.pow((y - currentY), 2)) < ((float)size)/2) {
					drawPixel(x, y);
				}
			}
		}
	}
	

}
