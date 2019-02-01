import java.util.ArrayList;

/**
 * 
 * Data Structure class for ElevatorData
 *
 */
public class ElevatorData {
	private final int currFloor; //the elevator's current floor
	private ArrayList<Integer> destFloor; //the floor destinations
	private boolean movingUp; //true if elevator is moving up, false otherwise
	
	/**
	 * 
	 * @param currFloor the floor the elevator is currently on
	 * @param destFloor the floor destinations
	 * @param movingUp true if the elevator is moving up, false otherwise
	 */
	public ElevatorData(int currFloor, ArrayList<Integer> destFloor, boolean movingUp) {
		this.currFloor = currFloor;
		this.destFloor = destFloor;
		this.movingUp = movingUp;
		
	}
	
	/**
	 * Returns the elevator's current floor
	 * @return the elevator's current floor
	 */
	public int getCurrentFloor() {
		return currFloor;
	}
	
	/**
	 * Returns the list of floor destinations
	 * @return the floor destinations
	 */
	public ArrayList<Integer> getDestinationFloor() {
		return destFloor;
	}
	
	/**
	 * Returns true if the elevator is moving up, false otherwise
	 * @return true if the elevator is moving up, false otherwise
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
}
