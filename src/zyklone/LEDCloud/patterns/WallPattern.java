package zyklone.LEDCloud.patterns;


/**
 * This specialization of LocalPattern represents a wall that travels perpendicular to its length
 * (which is represented by the size attribute).
 * The super-classes thickness attribute is used to represent the Wall's width.
 * @author Zyklone
 */
public class WallPattern extends LocalPattern {
	
	/**
	 * This constructor initializes the Pattern with its default values.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 */
	public WallPattern(int pictureSizeX, int pictureSizeY) {
		super(pictureSizeX, pictureSizeY, 13000, 12, 7.0f, 2.0f, 0.3f);
		initialize();
	}
	
	/**
	 * This constructor initializes the Pattern with certain default values,
	 * depending on if it is part of a BackgroundPattern or not.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 * @param backgroundPattern true, if this pattern is part of a BackgroundPattern, otherwise false
	 */
	public WallPattern(int pictureSizeX, int pictureSizeY, boolean backgroundPattern) {
		super(pictureSizeX, pictureSizeY, 0, 
				backgroundPattern ? Integer.valueOf(17) : Integer.valueOf(12),// maxSize
				backgroundPattern ? Float.valueOf(6.0f) : Float.valueOf(7.0f), 	// maxVelocity
				backgroundPattern ? Float.valueOf(1.5f) : Float.valueOf(2.0f), 	// maxAcceleration
				backgroundPattern ? Float.valueOf(0.6f) : Float.valueOf(0.3f)); // glow
		initialize();
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
	public WallPattern(int pictureSizeX, int pictureSizeY, int maxLifetimeInMs, Integer maxSize, Float maxVelocity, Float maxAcceleration, Float averageGlow,
			boolean backgroundPattern) {
		super(pictureSizeX, pictureSizeY, maxLifetimeInMs, maxSize, maxVelocity, maxAcceleration, averageGlow);
		initialize();
		if(backgroundPattern) 
			super.setBackgroundPatternColor();
	}
	
	/**
	 * Initializes the WallPattern's thickness and also its direction,
	 * because a wall should not travel parallel to the x-axis
	 * (because of the high length it might go almost all the way across the y-axis).
	 */
	private void initialize() {
		if(size > 9)
			this.thickness = rand.nextInt(3) + 2;
		else
			this.thickness = rand.nextInt(2) + 2;
		// direction must not be in y axis alone (cos > 0). cases correspond to first and second half of the unit circle
		switch(rand.nextInt(2)) {	
			case 0: this.direction = (float) Math.PI*(rand.nextFloat()*10/12 + (float)1/12);
			break;
			case 1: this.direction = (float) Math.PI*(rand.nextFloat()*10/12 + (float)13/12);
			break;
		}
	}
	
	/**
	 * Overrides the super-classes method in order to ensure,
	 * that if the wall travels close to parallel to the y-axis,
	 * the directionChange attribute is set to a value, that it will not do so for long.
	 */
	@Override
	protected void updateDirectionChange() {
		if((this.direction > 0 && this.direction < Math.PI*3/24) || (this.direction > Math.PI && this.direction < Math.PI*27/24))
			this.directionChange = (float) (0.2f*Math.PI);
		else if((this.direction > Math.PI*21/24 && this.direction < Math.PI) || (this.direction > Math.PI*45/24 && this.direction < 2*Math.PI))
			this.directionChange = (float) (-0.2f*Math.PI);
		else
			this.directionChange = (float) (((rand.nextFloat() * 6) - 3) / 10 * Math.PI); // max directionChange = 0.3*PI*rad/s^2
	}

	/**
	 * Draws the pattern in the form of a rectangle,
	 * with the size-attribute as its length and the thickness as its width.
	 * The length is oriented perpendicular to the Pattern's direction.
	 */
	@Override
	protected void drawPattern() {
		float halfXdist = (float) (Math.abs((float)size/2 * Math.sin(direction)) + Math.abs((float)thickness/2 * Math.cos(direction)));
		float halfYdist = (float) (Math.abs((float)size/2 * Math.cos(direction)) + Math.abs((float)thickness/2 * Math.sin(direction)));
		float wallY = currentY - halfYdist;
		// y coordinates containing complete wall
		for(int y = (int)(wallY < 0 ? wallY-1 : wallY); y <= (currentY + halfYdist); y++) {
			float wallX = currentX - halfXdist;
			// x coordinates containing complete wall
			for(int x = (int)(wallX < 0 ? wallX-1 : wallX); x <= (currentX + halfXdist); x++) {
				float deltaX = x - currentX;
				float deltaY = y - currentY;
				float sinPhi = (float) Math.sin(direction-Math.PI/2);
				float cosPhi = (float) Math.cos(direction-Math.PI/2);
				float cxDist = Math.abs(deltaX * cosPhi + deltaY * sinPhi);
				float cyDist = Math.abs(deltaY * cosPhi - deltaX * sinPhi);
				float xReq = (float)size/2;
				float yReq = (float)thickness/2;
				// check if coordinates are actually within the wall
				if(cxDist <= xReq && cyDist <= yReq) {
					drawPixel(x, y);
				}
			}
		}
	}

}


