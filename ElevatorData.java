import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * Data Structure class for ElevatorData
 *
 */
public class ElevatorData implements Serializable {
	public final static int NO_ERROR = 0;
	public final static int DOOR_STUCK_ERROR = 1;
	public final static int ELEVATOR_STUCK_ERROR = 2;

	private final int elevatorNum; //the elevator number
	private final int currFloor; //the elevator's current floor
	private ArrayList<Integer> reqFloor; //the requested floors
	private boolean movingUp; //true if elevator is moving up, false otherwise
	private boolean movingDown; //true if elevator is moving down, false otherwise
	private boolean doorOpened; //door flag (open/closed)
	private boolean shutdown; // shutdown flag
	private int errorType; // the error type
	private String status; //status for console messages
	private int port; // port for sending

	/**
	 * Constructor for the ElevatorData object
	 * 
	 * @param elevatorNum
	 * @param currFloor
	 * @param reqFloor list of current outstanding floor requests
	 * @param movingUp 
	 * @param movingDown 
	 * @param doorOpened
	 */
	public ElevatorData(int elevatorNum, int port, int errorType, int currFloor, 
			ArrayList<Integer> reqFloor, boolean movingUp, boolean movingDown, 
			boolean doorOpened, boolean shutdown) {
		this.elevatorNum = elevatorNum;
		this.errorType = errorType;
		this.currFloor = currFloor;
		this.reqFloor = reqFloor;
		this.movingUp = movingUp;
		this.movingDown = movingDown;
		this.doorOpened = doorOpened;
		this.shutdown = shutdown;
		this.port = port;

		switch(errorType) {
		case NO_ERROR:
			status = "Elevator " + elevatorNum + ": Current Floor - " + currFloor + ", requests " + reqFloor.toString() + ", ";

			if (movingUp) 
				status += "moving up";
			else if (movingDown) 
				status += "moving down";
			else
				status += "idle";

			if (doorOpened)
				status += ", door - open.";
			else
				status += ", door - closed.";
			break;

		case DOOR_STUCK_ERROR:
			if (doorOpened)
				status = "Elevator " + elevatorNum + ": Current Floor - " + currFloor 
				+ ", requests " + reqFloor.toString() + ", DOORS STUCK OPEN.";
			else {
				status = "Elevator " + elevatorNum + ": Current Floor - " + currFloor 
						+ ", requests " + reqFloor.toString() + ", DOORS STUCK CLOSED.";
			}
			break;
		case ELEVATOR_STUCK_ERROR:
			if (movingUp)
				status = "Elevator " + elevatorNum + ": STUCK BETWEEN floors " + currFloor 
				+ " and " + (currFloor + 1) + ".";
			else {
				status = "Elevator " + elevatorNum + ": STUCK BETWEEN floors " + currFloor 
						+ " and " + (currFloor - 1) + ".";
			}
		}
	}

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
	 * Returns true if the elevator door is open, false otherwise
	 * @return Returns true if the elevator door is open, false otherwise
	 */
	public boolean doorOpened() {
		return doorOpened;
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
	
	/**
	 * Returns the error type 
	 * @return the error type
	 */
	public int getErrorType() {
		return errorType;
	}
	
	/**
	 * Returns true if the elevator is operational
	 * @return true if the elevator is operational
	 */
	public boolean isOperational() {
		return !shutdown;
	}
	
	/**
	 * Returns the port of the elevator
	 * @return
	 */
	public int getPort() {
		return port;
	}

}
