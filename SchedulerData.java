import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * The Data Structure for the SchedulerData Class
 *
 */
public class SchedulerData implements Serializable {
	//CONSTANTS FOR REQUEST MODES
	public final static int FLOOR_REQUEST = 0;
	public final static int MOVE_REQUEST = 1;
	public final static int CONTINUE_REQUEST = 2;
	public final static int STOP_REQUEST = 3;
	

	private final int mode; //the request mode 
	private final int elevatorNum; //the elevator this data is being sent to
	private boolean floorLamps[]; //true if an elevatorSubsystem is on that floorSubsystem, false if none; array of floorSubsystem lamp states
	private int floor; //array of destination floors
	private int destFloor; //the destination floor
	private boolean moveUp, moveDown, doorOpen; //motor, door flags
	
	/**
	 * Constructor signature for FLOOR requests
	 * @param elevatorNum
	 * @param mode
	 * @param floorLamps
	 * @param floor
	 * @param destFloor
	 */
	public SchedulerData(int elevatorNum, int mode, boolean floorLamps[], int floor, int destFloor) {
		this.mode = mode;
		this.elevatorNum = elevatorNum;
		this.floorLamps = floorLamps;
		this.floor = floor;
		this.destFloor = destFloor;
	}
	
	/**
	 * Constructor signature for MOVE/STOP requests
	 * @param elevatorNum
	 * @param mode
	 * @param moveUp
	 * @param moveDown
	 * @param doorOpen
	 */
	public SchedulerData(int elevatorNum, int mode, boolean moveUp, boolean moveDown, boolean doorOpen) {
		this.mode = mode;
		this.elevatorNum = elevatorNum;
		this.moveUp = moveUp;
		this.moveDown = moveDown;
		this.doorOpen = doorOpen;
	}
	
	/**
	 * Constructor signature for CONTINUE requests
	 * @param elevatorNum
	 * @param mode
	 */
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
	public int getReqFloor() {
		return floor;
	}
	
	/**
	 * Returns the destination floor
	 * @return the destination floor
	 */
	public int getDestFloor() {
		return destFloor;
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
	 * Returns true if the door is open, false if closed
	 * @return doorOpen
	 */
	public boolean doorOpen() {
		return doorOpen;
	}
	
	/**
	 * Returns the mode of the request (FLOOR, MOVE, CONTINUE, STOP)
	 * @return
	 */
	public int getMode() {
		return mode;
	}

}
