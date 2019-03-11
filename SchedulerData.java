import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * The Data Structure for the SchedulerData Class
 *
 */


public class SchedulerData implements Serializable {
	public final static int FLOOR_REQUEST = 0;
	public final static int MOVE_REQUEST = 1;
	public final static int CONTINUE_REQUEST = 2;
	public final static int STOP_REQUEST = 3;
	

	private final int mode;
	private final int elevatorNum;
	private boolean floorLamps[]; //true if an elevatorSubsystem is on that floorSubsystem, false if none; array of floorSubsystem lamp states
	private ArrayList<Integer> reqFloors; //array of destination floors
	private boolean moveUp, moveDown, doorOpen;
	
	/**
	 * Creates an new SchedulerData Object
	 * @param elevatorNum the elevator to relay to
	 * @param floorLamps the array of floorLamps
	 * @param floorSubsystem the floorSubsystem destinations to send the elevatorSubsystem
	 */
	public SchedulerData(int elevatorNum, int mode, boolean floorLamps[], ArrayList<Integer> reqFloors) {
		this.mode = mode;
		this.elevatorNum = elevatorNum;
		this.floorLamps = floorLamps;
		this.reqFloors = reqFloors;
	}
	
	public SchedulerData(int elevatorNum, int mode, boolean moveUp, boolean moveDown, boolean doorOpen) {
		this.mode = mode;
		this.elevatorNum = elevatorNum;
		this.moveUp = moveUp;
		this.moveDown = moveDown;
		this.doorOpen = doorOpen;
	}
	
	public SchedulerData(int elevatorNum, int mode) {
		this.elevatorNum = elevatorNum;
		this.mode = mode;
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
	 * Returns true if the elevatorSubsystem is moving up, false otherwise
	 * @return true if the elevatorSubsystem is moving up, false otherwise
	 */
	public boolean moveUp() {
		return moveUp;
	}
	
	/**
	 * Returns true if the elevator is moving down, false otherwise
	 * @return true if the elevator is moving up, false otherwise
	 */
	public boolean moveDown() {
		return moveDown;
	}
	
	/**
	 * Returns true if the elevator is idle
	 * @return true if elevator is idle, false otherwise
	 */
	public boolean stop() {
		if (!moveUp && !moveDown)
			return true;
		return false;
	}
	
	/**
	 * 
	 * @return doorOpen
	 */
	public boolean doorOpen() {
		return doorOpen;
	}
	
	public int getMode() {
		return mode;
	}

}
