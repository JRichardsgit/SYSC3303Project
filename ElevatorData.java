import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * Data Structure class for ElevatorData
 *
 */
public class ElevatorData implements Serializable {
	private final int elevatorNum;
	private final int currFloor; //the elevatorSubsystem's current floorSubsystem
	private ArrayList<Integer> destFloor; //the floorSubsystem destinations
	private boolean movingUp; //true if elevatorSubsystem is moving up, false otherwise
	private String status;
	
	/**
	 * @param elevatorNum the designated elevator
	 * @param currFloor the floorSubsystem the elevatorSubsystem is currently on
	 * @param destFloor the floorSubsystem destinations
	 * @param movingUp true if the elevatorSubsystem is moving up, false otherwise
	 */
	public ElevatorData(int elevatorNum, int currFloor, ArrayList<Integer> destFloor, boolean movingUp) {
		this.elevatorNum = elevatorNum;
		this.currFloor = currFloor;
		this.destFloor = destFloor;
		this.movingUp = movingUp;
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
	public ArrayList<Integer> getDestinationFloor() {
		return destFloor;
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
