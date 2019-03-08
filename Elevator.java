import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Elevator Node Implementation
 */
public class Elevator extends Thread {
	
	//Elevator Status
	private String status; 
	
	//Elevator Number
	private final int elevatorNum; 
	
	//Number of floors
	private final int numFloors;
	
	//Movement flags
	private boolean movingUp;
	private boolean movingDown;
	
	//Door flag
	private boolean doorOpen;
	
	private boolean requestAvailable;
	
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
	public Elevator(int elevatorNum, int numFloors, ElevatorSubsystem eSystem) {
		this.elevatorNum = elevatorNum;
		this.numFloors = numFloors;
		this.eSystem = eSystem;
		status = "Idle";
		movingUp = false;
		movingDown = false;
		doorOpen = false;
		reqFloors = new ArrayList<Integer>();
		currFloor = 1;
		requestAvailable = false;
	}
	
	@Override
	public void run() {
		
		while (true) {
			if (!reqFloors.isEmpty()) {
				print(reqFloors.toString());
				moveToNextFloor();
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		}
	}
	
	/**
	 * Set flags for motor moving the elevator up
	 */
	public void moveUp() {
		movingUp = true;
		movingDown = false;
	}
	
	/**
	 * Returns true if the elevator is moving up, false otherwise
	 */
	public boolean isMovingUp() {
		return movingUp;
	}
	
	/**
	 * Set flags for motor moving the elevator down
	 */
	public void moveDown() {
		movingUp = false;
		movingDown = true;
	}
	
	/**
	 * Returns true if the elevator is moving up, false otherwise
	 */
	public boolean isMovingDown() {
		return movingDown;
	}
	
	/**
	 * Set flags for idle motor
	 */
	public void moveStop() {
		movingUp = false;
		movingDown = false;
	}
	
	/**
	 * Returns true if idle
	 */
	public boolean isIdle() {
		return (!movingUp && !movingDown);
	}
	
	/**
	 * Move the elevator the requested floor
	 * @param floorNum requested floor
	 */
	public void moveToFloor(int floorNum) {
		print("Elevator " + elevatorNum + " currently on floor " + currFloor + ".");
		print("Elevator " + elevatorNum + " moving to floor " + floorNum + ".");
		
		if (floorNum > currFloor) {
			moveUp();
			simulateWait(Math.abs(floorNum - currFloor) * 3000);
		}
		else if (floorNum < currFloor) {
			moveDown();
			simulateWait(Math.abs(floorNum - currFloor) * 3000);
		}
		else {
			//Do nothing, elevator is already on the floor
		}
		
		currFloor = floorNum;
			
		print("Elevator " + elevatorNum +
				" arrived at floor " + floorNum + ".\n");
		
		openDoor();
		closeDoor();
	}
	
	public void moveToNextFloor() {
		
		moveToFloor(reqFloors.get(0));
		reqFloors.remove(0);
		moveStop();

		eSystem.send(getElevatorData());
		
	}
	
	/**
	 * Request a floor from within the elevator
	 * @param floorNum the requested floor
	 */
	public void chooseFloor(int floorNum) {
		//Only add requested floor if not already requested
		if (!reqFloors.contains((Integer) floorNum))
			reqFloors.add(floorNum);
		Collections.sort(reqFloors);
	}
	
	/**
	 * Set the flag for opening the elevator doors
	 */
	public void openDoor() {
		doorOpen = true;
		print("Elevator " + elevatorNum + " opening doors.");
		simulateWait(2000);
	}
	
	/**
	 * Set the flag for closing the elevator doors
	 */
	public void closeDoor() {
		doorOpen = false;
		print("Elevator " + elevatorNum + " closing doors.\n");
		simulateWait(2000);
	}
	
	/**
	 * Update floor requests with the received request from the scheduler and random requests 
	 * from the elevator user
	 */
	public void receiveRequest(ArrayList<Integer> receivedRequests) {
		reqFloors.removeAll(receivedRequests);
		reqFloors.addAll(receivedRequests);
		Collections.sort(reqFloors);
		
		Random randomFloor = new Random();
	
		//Elevator User floor selection to be further implemented
		//chooseFloor(randomFloor.nextInt(numFloors + 1));
	}
	
	/**
	 * Return this elevator's data
	 * @return this elevator's data
	 */
	public ElevatorData getElevatorData() {
		return new ElevatorData(elevatorNum, currFloor, reqFloors, movingUp, movingDown);
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
