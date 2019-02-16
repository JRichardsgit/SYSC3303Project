import java.io.Serializable;

/**
 * 
 * The Data Structure for the SchedulerData Class
 *
 */
public class SchedulerData implements Serializable {
	private boolean floorLamps[]; //true if an elevatorSubsystem is on that floorSubsystem, false if none; array of floorSubsystem lamp states
	private int destFloors[]; //array of destination floors
	private String status;
	
	/**
	 * Creates an new SchedulerData Object
	 * @param floorLamps the array of floorLamps
	 * @param floorSubsystem the floorSubsystem destinations to send the elevatorSubsystem
	 */
	public SchedulerData(boolean floorLamps[], int destFloors[]) {
		this.floorLamps = floorLamps;
		this.destFloors = destFloors;
	}
	
	/**
	 * Returns the array of destination floors
	 * @return the array of destination floors
	 */
	public int[] getDestFloors() {
		return destFloors;
	}
	
	/**
	 * Returns the array of floorSubsystem lamp states
	 * @return the array of floorSubsystem lamp states
	 */
	public boolean[] getFloorLamps() {
		return floorLamps;
	}
	
	/**
	 * Sets the scheduler's status
	 * @param status the status of the scheduler
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Returns the status of the scheduler
	 * @return the status of the scheduler
	 */
	public String getStatus() {
		return status;
	}

	

}
