package zyklone.LEDCloud.patterns;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

/**
 * Represents a base class for variable patterns that only affect a part of whole picture.
 * This is done by incorporating attributes like position, size, velocity, direction.
 * The LocalPattern class provides all the base-functionality for calculating pictures depending on the set attributes,
 * a subclass simply has to implement the drawPattern()-method to define the form of the pattern.
 * Subclasses can directly access all of the attributes,
 * but a constructor for initializing the most important attributes is provided nonetheless.
 * 
 * @author Zyklone
 */
public abstract class LocalPattern extends Pattern{
	
	protected Random rand = new Random();
	protected float currentX;
	protected float currentY;
	protected long lifespan;
	protected long startTime;
	protected long currentTime;
	protected long newTime;
	protected int size;
	/** Not necessarily applicable to all LocalPatterns */
	protected int thickness;
	/** The relative amount of brightness a spot should have, after the pattern leaves its position.
	 * This value is applied to the whole picture (which then represents the faded, old picture),
	 * before the next picture is calculated and applied to the old one.*/
	protected float glow;
	protected float velocity;
	protected float maxVelocity;
	protected float acceleration;
	protected float maxAcceleration;
	protected float direction; // in radian
	protected float directionChange; // direction : directionChange = velocity : acceleration
	protected Color color;

	
	/**
	 * This constructor initializes all the attributes to default values which are configured for DotPattern,
	 * but could be used for any other LocalPattern as well.
	 * @param pictureSizeX the length in the x-axis of the led-matrix
	 * @param pictureSizeY the length in the y-axis of the led-matrix
	 */
	public LocalPattern(int pictureSizeX, int pictureSizeY) {
		super(pictureSizeX, pictureSizeY);
		initialize();
		this.lifespan = rand.nextInt(7000) + 6000;			// lifespan of 6 - 13 seconds
		this.size = rand.nextInt(4) + 3;					// default size of 3 to 6 units
		this.velocity = rand.nextInt(50) / 10f + 3f;		// default velocity of 3 to 8 units/s
		this.maxVelocity = 8.0f;	
		this.acceleration = ((rand.nextFloat() * 5) - 2.5f); 	// default acceleration of -2.5 to 2.5 units/s²
		this.maxAcceleration = 2.5f;
		this.glow = rand.nextFloat()/5 + 0.4f;	// default glow of 0.4 to 0.6; the perceived glow is automagically longer, the faster the pattern travels
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
	 */
	public LocalPattern(int pictureSizeX, int pictureSizeY, int maxLifetimeInMs,
			Integer maxSize, Float maxVelocity, Float maxAcceleration, Float averageGlow) {
		super(pictureSizeX, pictureSizeY);
		initialize();
		
		if(maxLifetimeInMs >= 2000)
			lifespan = rand.nextInt(maxLifetimeInMs*2/3) + maxLifetimeInMs/3;	// lifespan of at least a third of maxLifetimeInMs
		else
			this.lifespan = rand.nextInt(7000) + 6000;			// lifespan of 6 - 13 seconds
		
		if(maxSize == null || maxSize < 1) 
			this.size = rand.nextInt(4) + 3;					// default size of 3 to 6 units
		else
			this.size = rand.nextInt(maxSize/2+1) + maxSize/2;	// minimum size = maxSize/2
		
		if(maxVelocity == null) {
			this.maxVelocity = 0.0f;
			this.velocity =  0.0f;
		}
		else if(maxVelocity>=2.0f) {
			this.maxVelocity = maxVelocity;
			this.velocity =  rand.nextInt((int)(maxVelocity*70)) / 100f + maxVelocity*0.3f;	// max speed = lowest starting speed = 0.3*maxVelocity
		}
		else {
			this.velocity = rand.nextInt(50) / 10f + 3f;		// default velocity of 3 to 8 units/s
			this.maxVelocity = 8.0f;	
		}
		if(maxAcceleration == null) {
			this.maxAcceleration = 0.0f;
			this.acceleration = 0.0f;
		}
		else if(maxAcceleration >=0.0f) {
			this.maxAcceleration = maxAcceleration;
			this.acceleration = ((rand.nextFloat() * 2*maxAcceleration) - maxAcceleration);
		} else {
			this.acceleration = ((rand.nextFloat() * 5) - 2.5f); 	// default acceleration of -2.5 to 2.5 units/s²
			this.maxAcceleration = 2.5f;
		}
		if(averageGlow == null)
			this.glow = 0.0f;
		else if(averageGlow >= 0.1f && averageGlow < 0.9f)
			this.glow = rand.nextFloat()/5 + averageGlow-0.1f;	// the perceived glow is automagically longer, the faster the pattern travels
		else
			this.glow = rand.nextFloat()/5 + 0.4f;	// default glow of 0.4 to 0.6; the perceived glow is automagically longer, the faster the pattern travels
	}
	
	/**
	 * Initializes the coordinates with random values inside the picture.
	 */
	private void initialize() {
		this.currentX = rand.nextInt(pictureSizeX);
		this.currentY = rand.nextInt(pictureSizeY);
		this.direction = (float) (rand.nextFloat()*Math.PI*2);
		setColor();
		//updateDirectionChange();
	}
	
	/**
	 * Sets the Pattern's color to a pseudo-random value.
	 */
	private void setColor() {
		int red = 0, green = 0, blue = 0;
		while(red == 0 && green == 0 && blue == 0) {
			if(rand.nextBoolean())
				red = rand.nextInt(250);
			if(rand.nextBoolean())
				green = rand.nextInt(250);
			if(rand.nextBoolean())
				blue = rand.nextInt(250);
		}
		this.color = new Color(red, green, blue);
	}
	
	/**
	 * Sets the Pattern's color to a very low, pseudo-random value with a heavy blue tone,
	 * so it can be used in a BackgroundPattern.
	 */
	protected void setBackgroundPatternColor() {
		int red = rand.nextInt(6);
		int green = rand.nextInt(6);
		int blue = 3 + rand.nextInt(5) + (red+green)/2;	// BasePattern colors should always have a blue tone
		this.color = new Color(red, green, blue);
	}
	
	/**
	 * Defines how to draw the pattern, i.e. the form of the pattern.
	 */
	protected abstract void drawPattern();
	
	/**
	 * Calculates and returns the next picture.
	 * The new position of the pattern is dependent on the time passed since the last calculation.
	 * @return the last calculated picture of this pattern.
	 */
	public ArrayList<ArrayList<Color>> getNextPicture(){
		if(this.startTime == 0) {
			this.currentTime = System.currentTimeMillis();
			this.startTime = currentTime;
		}
		
		this.newTime = System.currentTimeMillis();
		// if the pattern has reached its lifetime, return null
		if(newTime - this.startTime > this.lifespan)
			return null;
		
		this.fadeOutPicture();
		
		// Calculate new position and picture if pattern has more than 1 second left to live
		if(newTime - this.startTime < this.lifespan - 1000) {
			this.advancePatternCoordinates();
			
			// draw the pattern
			drawPattern();
			
			// update velocity and direction
			this.velocity = velocity + (((float)(newTime - currentTime))/1000) * acceleration;
			this.direction = direction + (((float)(newTime - currentTime))/1000) * directionChange;
			
			currentTime = newTime;
			// update acceleration and directionChange with random values every 1.5 seconds
			if((currentTime - startTime)%1551 > 1500) {
				updateAcceleration();
				updateDirectionChange();
			}
		}
		
		// apply a fade in to the calculated picture, if it is younger than 1 second
		if(newTime - startTime < 1000)
			this.fadeInPicture();
		
		return this.picture;
	}

	/**
	 * Randomly sets the value of the directionChange-attribute to a value between -0.3 and 0.3 PI
	 */
	protected void updateDirectionChange() {
		this.directionChange = (float) (((rand.nextFloat() * 6) - 3) / 10 * Math.PI);
	}
	
	/**
	 * Gradually fades in the last calculated picture, depending on the time that passed since the creation of the pattern.
	 * Full brightness is reached after 1 second.
	 */
	protected void fadeInPicture() {
		float fade = (newTime - startTime)/1000f;
		for(int i = 0; i < this.picture.size(); i++) {
			for(int j = 0; j < this.picture.get(i).size(); j++) {
				Color oldCol = picture.get(i).get(j);
				this.picture.get(i).set(j, 
						new Color((int)(oldCol.getRed()*fade),
								(int)(oldCol.getGreen()*fade),
								(int)(oldCol.getBlue()*fade)));
			}
		}
	}
	
	/**
	 * Fades out the last calculated picture.
	 * If the remaining lifetime is longer than 1 second,
	 * the standard glow-setting will be applied.
	 * If the remaining lifetime is shorter than 1 second,
	 * the picture is set to gradually fade out to zero over the remaining lifetime.
	 */
	protected void fadeOutPicture() {
		// fade old colors
		float fade;
		if(newTime - this.startTime < this.lifespan - 1000)
			fade = glow;
		else
			fade = (startTime + this.lifespan - newTime)/1000f;
		for(int i = 0; i < this.picture.size(); i++) {
			for(int j = 0; j < this.picture.get(0).size(); j++) {
				Color oldCol = picture.get(i).get(j);
				if(oldCol.getRed() < 6 && oldCol.getGreen() < 6 && oldCol.getBlue() < 6)
					this.picture.get(i).set(j, new Color(0, 0, 0));
				else {
					//System.out.println("Fading: " + oldCol.getRed()*glow + ", " + oldCol.getGreen()*glow + ", " + oldCol.getBlue()*glow);
					this.picture.get(i).set(j,
							new Color((int)(oldCol.getRed()*fade),
									(int)(oldCol.getGreen()*fade),
									(int)(oldCol.getBlue()*fade)));
				}
			}
		}
	}
	
	/**
	 * Sets exactly one pixel of the picture, based on the objects color-attribute.
	 * Checks if the passed coordinates are within the picture-size and corrects the values if not.
	 * Coordinates crossing the y-boundary, reappear on the opposite side
	 * Coordinates crossing the x-boundary, reappear on the same side, mirrored on the line y=pictureSizeY/2
	 * @param x the x-coordinate of the pixel
	 * @param y the y-coordinate of the pixel
	 */
	protected void drawPixel(int x, int y) {
		int yCoordinate = y;
		if(y < 0)
			//yCoordinate = pictureSizeY - y;
			yCoordinate = pictureSizeY + y%pictureSizeY;
//		else
//			yCoordinate = y%pictureSizeY;
		if(x >= pictureSizeX) {
			this.picture.get((yCoordinate+8)%pictureSizeY).set(pictureSizeX-((x+1)%pictureSizeX), this.color);
		}
		else if(x < 0) {
			this.picture.get((yCoordinate+8)%pictureSizeY).set((x%pictureSizeX)*-1, this.color);
		}
		else
			this.picture.get(yCoordinate%pictureSizeY).set(x, this.color);
	}
	
	/**
	 * Pseudo-randomly generates an acceleration value.
	 * Generally the acceleration will not exceed the maxAcceleration value,
	 * unless the current velocity is above or below a certain absolute value,
	 * in which case the generated acceleration value may exceed the maxAccleration value by up to 50%.
	 */
	protected void updateAcceleration() {
		if(Math.abs(velocity) < maxVelocity*0.3) {	// velocity should not get below 0.3 times the abs(maxVelocity) -> do something if it does
			if(velocity > 0)
				this.acceleration = (rand.nextFloat() * maxAcceleration) + 0.5f*maxAcceleration;	// maxAcceleration value can be exceeded to speed up faster
			else
				this.acceleration = (rand.nextFloat() * maxAcceleration) - 1.5f*maxAcceleration;	// maxAcceleration value can be exceeded to speed up faster
		}
		else if(Math.abs(velocity) > maxVelocity) {	// velocity should not get above abs(maxVelocity) -> do something if it does
			if(velocity > 0)
				this.acceleration = (rand.nextFloat() * maxAcceleration) - 1.5f*maxAcceleration;	// maxAcceleration value can be exceeded to slow down faster
			else
				this.acceleration = (rand.nextFloat() * maxAcceleration) + 0.5f*maxAcceleration;	// maxAcceleration value can be exceeded to slow down faster
		}
		else	// velocity is within the expected range -> apply random acceleration value
			this.acceleration = ((rand.nextFloat() * 2*maxAcceleration) - maxAcceleration);
	}
	
	/**
	 * Calculates and sets the pattern's new position,
	 * depending on the time passed between the last calculation and the pattern's velocity.
	 */
	protected void advancePatternCoordinates() {
		// move dot coordinates
		// y coordinates advance at a relative speed of 0.8
		float tempX = (float) (currentX + (((float)(newTime - currentTime))/1000) * velocity * Math.cos(this.direction));
		if(tempX > pictureSizeX-1) {
			float tt = ((tempX) % (pictureSizeX-1));
			//position traveling around outer edges, direction needs to be corrected
			this.currentX = pictureSizeX - tt - 1;
			this.currentY = (float) ((currentY + (((float)(newTime - currentTime))/1000) * velocity * Math.sin(this.direction) * 0.8f) + 8) % pictureSizeY;
			if(currentY < 0)
				currentY = pictureSizeY - currentY;
			// mirror angle on y axis
			if(this.direction <= Math.PI)
				this.direction = (float) (Math.PI - direction);
			else
				this.direction = (float) (Math.PI*3 - direction);
		}
		else if(tempX < 0) {
			//position traveling around outer edges, direction needs to be corrected
			this.currentX = (tempX%pictureSizeX) * -1;
			this.currentY = (float) ((currentY + 8 + (((float)(newTime - currentTime))/1000) * velocity * Math.sin(this.direction) * 0.8f) + 8) % pictureSizeY;
			if(currentY < 0)
				currentY = pictureSizeY - currentY;
			// mirror angle on y axis
			if(this.direction <= Math.PI)
				this.direction = (float) (Math.PI - direction);
			else
				this.direction = (float) (Math.PI*3 - direction);
		}
		else {
			// x-movement within boundaries
			this.currentX = tempX;
			this.currentY = (float) (currentY + (((float)(newTime - currentTime))/1000) * velocity * Math.sin(this.direction) * 0.8f) % pictureSizeY;
			if(currentY < 0)
				currentY = pictureSizeY - currentY;
		}
	}
		

}
