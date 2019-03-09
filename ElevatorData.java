import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * Data Structure class for ElevatorData
 *
 */
public class ElevatorData implements Serializable {
	private final int elevatorNum; //the elevator number
	private final int currFloor; //the elevator's current floor
	private ArrayList<Integer> reqFloor; //the requested floors
	private boolean movingUp; //true if elevator is moving up, false otherwise
	private boolean movingDown; //true if elevator is moving down, false otherwise
	private String status; 
	
	/**
	 * @param elevatorNum the designated elevator
	 * @param currFloor the floor the elevator is currently on
	 * @param reqFloor the floor destinations
	 * @param movingUp true if the elevator is moving up, false otherwise
	 */
	public ElevatorData(int elevatorNum, int currFloor, ArrayList<Integer> reqFloor, boolean movingUp, boolean movingDown) {
		this.elevatorNum = elevatorNum;
		this.currFloor = currFloor;
		this.reqFloor = reqFloor;
		this.movingUp = movingUp;
		this.movingDown = movingDown;
		
		status = "Elevator " + elevatorNum + ": Currently on floor " + currFloor + " with requests " + reqFloor.toString() + ", ";
		
		if (movingUp) 
			status += "moving up.";
		else if (movingDown) 
			status += "moving down.";
		else
			status += "idle.";
	}
	
	/**
	
	/**
	 * Returns the elevator's current floorSubsystem
	 * @return the elevator's current floorSubsystem
	 */
	public int getCurrentFloor() {
		return currFloor;
	}
	
	/**
	 * Returns the list of floor destinations
	 * @return the floor destinations
	 */
	public ArrayList<Integer> getRequestedFloors() {
		return reqFloor;
	}
	
	/**
	 * Returns true if the elevatorSubsystem is moving up, false otherwise
	 * @return true if the elevatorSubsystem is moving up, false otherwise
	 */
	public boolean isMovingUp() {
		if (movingUp)
			return movingUp;
		return false;
	}
	
	/**
	 * Returns true if the elevator is moving down, false otherwise
	 * @return true if the elevator is moving up, false otherwise
	 */
	public boolean isMovingDown() {
		if (!movingUp)
			return true;
		return false;
	}
	
	/**
	 * Returns true if the elevator is idle
	 * @return true if elevator is idle, false otherwise
	 */
	public boolean isIdle() {
		if (!movingUp && !movingDown)
			return true;
		return false;
	}
	
	/**
	 * Sets the elevator's status
	 * @param status the status of the elevator
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Returns the status of the elevator
	 * @return the status of the elevator
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Returns the elevator number this data to which this data is designated
	 * @return the elevator number corresponding to this data
	 */
	public int getElevatorNumber() {
		return elevatorNum;
	}

}
