import java.util.ArrayList;

/**
 * Elevator Node Implementation
 */
public class Elevator extends Thread {
	
	//Elevator Status
	private String status; 
	
	//Elevator Number
	private final int elevatorNum; 
	
	//Movement flags
	private boolean movingUp;
	private boolean movingDown;
	
	//Door flag
	private boolean doorOpen;
	
	//Reference to Elevator Subsystem
	private ElevatorSubsystem eSystem;
	
	//Floor Information
	private ArrayList<Integer> reqFloors;
	private int currFloor;
	
	/**
	 * Creates a new elevator node
	 * @param elevatorNum this elevator's number
	 * @param eSystem reference to the elevator subsystem
	 */
	public Elevator(int elevatorNum, ElevatorSubsystem eSystem) {
		this.elevatorNum = elevatorNum;
		this.eSystem = eSystem;
		status = "Idle";
		movingUp = false;
		movingDown = false;
		doorOpen = false;
		reqFloors = new ArrayList<Integer>();
		currFloor = 0;
	}
	
	/**
	 * Set flags for motor moving the elevator up
	 */
	public void moveUp() {
		movingUp = true;
		movingDown = false;
	}
	
	/**
	 * Set flags for motor moving the elevator down
	 */
	public void moveDown() {
		movingUp = false;
		movingDown = true;
	}
	
	/**
	 * Set flags for idle motor
	 */
	public void moveStop() {
		movingUp = false;
		movingDown = false;
	}
	
	/**
	 * Move the elevator the requested floor
	 * @param floorNum requested floor
	 */
	public void moveToFloor(int floorNum) {
		print("Elevator " + elevatorNum + " currently on floor " + currFloor + " .");
		print("Elevator " + elevatorNum + " moving to floor" + currFloor + " .");
		
		if (floorNum > currFloor) {
			moveUp();
		}
		else if (floorNum < currFloor) {
			moveDown();
			simulateWait(Math.abs(floorNum - currFloor) * 3000);
		}
		else {
			//Do nothing, elevator is already on the floor
		}
			
		print("Arrived at floor " + floorNum + ".");
		reqFloors.remove((Integer) floorNum);
		
		print("Opening doors.");
		simulateWait(2000);
		openDoor();
		
		print("Closing doors.");
		simulateWait(2000);
		closeDoor();
	}
	
	/**
	 * Request a floor from within the elevator
	 * @param floorNum the requested floor
	 */
	public void chooseFloor(int floorNum) {
		//Only add requested floor if not already requested
		if (!reqFloors.contains((Integer) floorNum))
			reqFloors.add(floorNum);
	}
	
	/**
	 * Set the flag for opening the elevator doors
	 */
	public void openDoor() {
		doorOpen = true;
	}
	
	/**
	 * Set the flag for closing the elevator doors
	 */
	public void closeDoor() {
		doorOpen = false;
	}
	
	/**
	 * Simulate waiting time for elevator actions
	 * @param ms the time to wait, in milliseconds
	 */
	public void simulateWait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Print a status message in the console
	 * @param message the message to be printed
	 */
	public void print(String message) {
		System.out.println(message);
	}

}
