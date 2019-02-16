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
	 * @param currFloor the floorSubsystem the elevatorSubsystem is currently on
	 * @param reqFloor the floorSubsystem destinations
	 * @param movingUp true if the elevatorSubsystem is moving up, false otherwise
	 */
	public ElevatorData(int elevatorNum, int currFloor, ArrayList<Integer> reqFloor, boolean movingUp, boolean movingDown) {
		this.elevatorNum = elevatorNum;
		this.currFloor = currFloor;
		this.reqFloor = reqFloor;
		this.movingUp = movingUp;
		this.movingDown = movingDown;
		
		status = "Elevator " + elevatorNum + ": " + reqFloor.size() + " requested floors and ";
		
		if (movingUp) 
			status += "currently moving up.";
		else if (movingDown) 
			status += "currently moving down.";
		else
			status += "currently idle.";
	}
	
	/**
	
	/**
	 * Returns the elevatorSubsystem's current floorSubsystem
	 * @return the elevatorSubsystem's current floorSubsystem
	 */
	public int getCurrentFloor() {
		return currFloor;
	}
	
	/**
	 * Returns the list of floorSubsystem destinations
	 * @return the floorSubsystem destinations
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
	 * Returns true if the elevatorSubsystem is moving down, false otherwise
	 * @return true if the elevatorSubsystem is moving up, false otherwise
	 */
	public boolean isMovingDown() {
		if (!movingUp)
			return true;
		return false;
	}
	
	/**
	 * Sets the elevatorSubsystem's status
	 * @param status the status of the elevatorSubsystem
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Returns the status of the elevatorSubsystem
	 * @return the status of the elevatorSubsystem
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
