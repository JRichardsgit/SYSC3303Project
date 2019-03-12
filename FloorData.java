import java.io.Serializable;

/**
 * Data structure Class for FloorSubsystem DataPackets
 */
public class FloorData implements Serializable {
	
	private final int floorNum; //The number of the floor
	private final boolean upPressed; //True if up is pressed, false otherwise
	private final int destFloor; //The destination floor
	private String status; //Status for console messages
	
	/**
	 * Create a new FloorData object with the given floorNum and up/down setting
	 * @param floorNum
	 * @param upPressed
	 * @param destFloor
	 */
	public FloorData(int floorNum, boolean upPressed, int destFloor) {
		this.floorNum = floorNum;
		this.upPressed = upPressed;
		this.destFloor = destFloor;
		
		if (upPressed)
			status = "Floor " + floorNum + ": request to go up to floor " + destFloor;
		else
			status = "Floor " + floorNum + ": request to go down to floor " + destFloor;
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
	 * Returns the destination floor
	 * @return the destination floor
	 */
	public int getDestFloor() {
		return destFloor;
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
