import java.io.Serializable;

/**
 * Data structure Class for Floor DataPackets
 */
public class FloorData implements Serializable {
	
	private final int floorNum; //The number of the floor
	private final boolean upPressed; //True if up is pressed, false otherwise
	private String status;
	
	/**
	 * Create a new FloorData object with the given floorNum and up/down setting
	 * @param floorNum
	 * @param upPressed
	 */
	public FloorData(int floorNum, boolean upPressed) {
		this.floorNum = floorNum;
		this.upPressed = upPressed;
	}
	
	/**
	 * Return true if up is pressed, false otherwise
	 * @return true if up is pressed, false otherwise
	 */
	public boolean upPressed() {
		if (upPressed) 
			return upPressed;
		return false;
	}
	
	/**
	 * Return true if down is pressed, false otherwise
	 * @return true if down is pressed, false otherwise
	 */
	public boolean downPressed() {
		if (!upPressed)
			return true;
		return false;
	}
	
	/**
	 * Returns the number of the floor
	 * @return the number of the floor
	 */
	public int getFloorNum() {
		return floorNum;
	}
	
	/**
	 * Sets the floor's status
	 * @param status the status of the floor
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Returns the status of the floor
	 * @return the status of the floor
	 */
	public String getStatus() {
		return status;
	}

}
