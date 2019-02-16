import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * The Data Structure for the SchedulerData Class
 *
 */
public class SchedulerData implements Serializable {
	private final int elevatorNum;
	private boolean floorLamps[]; //true if an elevatorSubsystem is on that floorSubsystem, false if none; array of floorSubsystem lamp states
	private ArrayList<Integer> reqFloors; //array of destination floors
	private String status;
	
	/**
	 * Creates an new SchedulerData Object
	 * @param elevatorNum the elevator to relay to
	 * @param floorLamps the array of floorLamps
	 * @param floorSubsystem the floorSubsystem destinations to send the elevatorSubsystem
	 */
	public SchedulerData(int elevatorNum, boolean floorLamps[], ArrayList<Integer> reqFloors) {
		this.elevatorNum = elevatorNum;
		this.floorLamps = floorLamps;
		this.reqFloors = reqFloors;
		
		status = "Scheduler: Relaying Information.";
	}
	
	/**
	 * Return the elevator number to relay info to
	 * @return the elevator number to relay info to
	 */
	public int getElevatorNumber() {
		return elevatorNum;
	}
	/**
	 * Returns the array of requested floors
	 * @return the array of requested floors
	 */
	public ArrayList<Integer> getReqFloors() {
		return reqFloors;
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
