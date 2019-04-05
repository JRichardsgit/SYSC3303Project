import java.io.Serializable;

/**
 * 
 * The Data Structure for the SchedulerFloorData Class
 *
 */
public class SchedulerFloorData implements Serializable {
	//CONSTANTS FOR REQUEST MODES
	public final static int CONFIRM_MESSAGE = 0;
	public final static int UPDATE_MESSAGE = 1;
	
	private final int mode; //the request mode 
	private int floorLamps[]; 
	/**
	 * Constructor for UPDATES
	 * @param floor
	 * @param mode
	 * @param floorLamps
	 */
	public SchedulerFloorData(int mode, int floorLamps[]) {
		this.mode = mode;
		this.floorLamps = floorLamps;
	}
	
	/**
	 * Constructor for CONFIRM
	 * @param mode
	 */
	public SchedulerFloorData(int mode) {
		this.mode = mode;
	}
	
	/**
	 * The floor lamps array
	 * @return
	 */
	public int[] getFloorLamps() {
		return floorLamps;
	}
	
	/**
	 * Returns the mode of the request (CONFIRM, UPDATE)
	 * @return
	 */
	public int getMode() {
		return mode;
	}

}
