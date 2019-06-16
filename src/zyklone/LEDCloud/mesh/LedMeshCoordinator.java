package zyklone.LEDCloud.mesh;


import java.util.List;
import java.awt.Color;

import com.github.mbelling.ws281x.Ws281xLedStrip;

/**
 * The LedMeshCoordinator is responsible for mapping every pixel in a picture (2 dimensional array)
 * to an led on an LED-strip and send the color-information to the strip in the correct order,
 * so that the desired picture is displayed.
 * In order to achieve this, the LedMeshCoordinator needs information about the position of each pixel
 * in the 2-dimensional array, i.e. its coordinates.
 * This information, in the form of a list of FieldPosition objects, is passed along upon instantiation.
 * The list needs to be ordered, so that the n-th element may represent the n-th LED on the LED-strip.
 * 
 * @author Zyklone
 */
public class LedMeshCoordinator {

	private List<FieldPosition> order;
	private Ws281xLedStrip ledstrip;
	
	public LedMeshCoordinator(List<FieldPosition> order, Ws281xLedStrip ledstrip) {
		if(order == null || ledstrip == null) {
			System.err.println("MeshCoordinator will not provide meaningful output, if the order-Array is null!");
			throw new IllegalArgumentException("MeshCoordinator will not provide meaningful output, if the order-Array is null!");
		}
		this.order = order;
		this.ledstrip = ledstrip;
	}
	
	/**
	 * Updates the LED-strip according to the passed 2-dimensional color-array (referred to as picture)
	 * and the order-list of the instance of this class.
	 * @param picture the passed 2-dimensional color-array
	 * @return the number of updated LEDs / set pixels, which should correspond to the number of elements in the order-list.
	 */
	public long updateLedMesh(List<? extends List<Color>> picture) {
		long updatedLeds = 0;
		for(int i = 0; i < order.size(); i++) {
			FieldPosition fieldPos = order.get(i);
			if(picture.size() > fieldPos.getRow() && picture.get(fieldPos.getRow()).size() > fieldPos.getColumn()) {
				Color col = picture.get(fieldPos.getRow()).get(fieldPos.getColumn());
				ledstrip.setPixel(i, col.getRed(), col.getGreen(), col.getBlue());
				updatedLeds++;
			}
		}
		ledstrip.render();
		return updatedLeds;
	}
	

}
