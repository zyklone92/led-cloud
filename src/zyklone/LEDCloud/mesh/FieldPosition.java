package zyklone.LEDCloud.mesh;

/**
 * Represents a Pixel in the cloud's LED-matrix.
 * 
 * @author Zyklone
 */
public class FieldPosition {

	private int column;
	private int row;
	
	public FieldPosition(int row, int column) {
		this.column = column;
		this.row = row;
	}

	/**
	 * @return the column / x-coordinate of the pixel
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @return the row / y-coordinate of the pixel
	 */
	public int getRow() {
		return row;
	}

}
