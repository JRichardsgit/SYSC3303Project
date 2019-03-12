import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * Elevator Node Implementation
 */
public class Elevator extends Thread {

	DatagramPacket sendPacket;
	DatagramSocket sendSocket;

	InetAddress schedulerAddress;

	// Elevator Status
	private String status;

	// Elevator Number
	private final int elevatorNum;

	// Number of floors
	private final int numFloors;

	// Movement flags
	private boolean movingUp;
	private boolean movingDown;

	// Door flag
	private boolean doorOpen;

	// Reference to Elevator Subsystem
	private ElevatorSubsystem eSystem;

	// Floor Information
	private ArrayList<Integer> reqFloors;
	private int currFloor;
	
	private boolean actionReady;

	/**
	 * Creates a new elevator node
	 * 
	 * @param elevatorNum
	 *            this elevator's number
	 * @param eSystem
	 *            reference to the elevator subsystem
	 */
	public Elevator(int elevatorNum, int numFloors, ElevatorSubsystem eSystem) {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendSocket = new DatagramSocket();

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		this.elevatorNum = elevatorNum;
		this.numFloors = numFloors;
		this.eSystem = eSystem;
		status = "Idle";
		movingUp = false;
		movingDown = false;
		doorOpen = false;
		reqFloors = new ArrayList<Integer>();
		currFloor = 1;
		actionReady = false;
	}

	@Override
	public void run() {

		waitForInstruction();
		
		while (true) {
			if (!reqFloors.isEmpty() && actionReady) {
				moveOneFloor();
				send();
				waitForInstruction();
			}
			wait(1000);
		}
	}

	public void setSchedulerAddress(InetAddress address) {
		schedulerAddress = address;
	}

	/**
	 * Send a packet to the scheduler
	 */
	public void send() {

		ElevatorData elevDat = getElevatorData();
		try {
			// Convert the ElevatorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(elevDat);
			ooStream.flush();

			byte msg[] = baoStream.toByteArray();
			sendPacket = new DatagramPacket(msg, msg.length, schedulerAddress, 3000);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Send the datagram packet to the client via the send socket.
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Sent packet to scheduler.\nContaining:\n	" + elevDat.getStatus() + "\n");
	}

	/**
	 * Update floor requests with the received request from the scheduler and random
	 * requests from the elevator user
	 */
	public void receiveRequest(SchedulerData s) {
	
		int mode = s.getMode();
		switch (mode) {
			case SchedulerData.FLOOR_REQUEST:
				print("Received FLOOR request.\n");
				reqFloors.removeAll(s.getReqFloors());
				reqFloors.addAll(s.getReqFloors());
				//Collections.sort(reqFloors);
				print("Current requests: " + reqFloors.toString());
				break;
			
			case SchedulerData.MOVE_REQUEST:
				print("Received MOVE request.\n");
				if (doorOpen) //If door open, close the door before moving
					closeDoor();
				
				if (s.moveUp()) {
					moveUp();
				} else {
					moveDown();
				} 
				break;
				
			case SchedulerData.CONTINUE_REQUEST:
				print("Received CONTINUE request.\n");
				break;
				
			case SchedulerData.STOP_REQUEST:
				print("Received STOP request.\n");
				stopMotor();
				openDoor();
				if (!reqFloors.isEmpty()) {
					print("Arrived at floor " + currFloor + ".\n");
					reqFloors.remove(new Integer(currFloor));
				} 
				break;
		}
	}

	public void waitForInstruction() {
		eSystem.setPending(elevatorNum, true);
		print("Awaiting Instruction.\n");
		while (eSystem.isPending(elevatorNum)) {
			wait(1000);
		}
		actionReady = true;
	}

	public void moveOneFloor() {
		//If door is open, wait until closed
		while (doorOpen && !isIdle()) {
			wait(1000);
		}
		
		//If train is moving or has just stopped
		if (!isIdle()) {
			if (movingUp) {
				currFloor++;
				print("Currently on floor " + currFloor + ", moving up.");
			} else if (movingDown) {
				currFloor--;
				print("Currently on floor " + currFloor + ", moving down.");
			} 
			wait(1000);
		} 
		
		if (reqFloors.isEmpty())
			actionReady = false;
		
	}

	/**
	 * Set flags for motor moving the elevator up
	 */
	public void moveUp() {
		print("Now moving up.");
		movingUp = true;
		movingDown = false;
	}
	/**
	 * Set flags for motor moving the elevator down
	 */
	public void moveDown() {
		print("Now moving down.");
		movingUp = false;
		movingDown = true;
	}

	/**
	 * Set flags for idle motor
	 */
	public void stopMotor() {
		print("Now stopping.");
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
	 * Request a floor from within the elevator
	 * 
	 * @param floorNum
	 *            the requested floor
	 */
	public void chooseFloor(int floorNum) {
		// Only add requested floor if not already requested
		if (!reqFloors.contains((Integer) floorNum))
			reqFloors.add(floorNum);
		// Collections.sort(reqFloors);
	}

	/**
	 * Set the flag for opening the elevator doors
	 */
	public void openDoor() {
		actionReady = false;
		print("Opening doors.");
		wait(2000);
		print("Doors opened.");
		doorOpen = true;
		actionReady = true;
	}

	/**
	 * Set the flag for closing the elevator doors
	 */
	public void closeDoor() {
		actionReady = false;
		print("Closing doors.");
		wait(2000);
		print("Doors closed.");
		doorOpen = false;
		actionReady = true;
	}

	/**
	 * Return this elevator's data
	 * 
	 * @return this elevator's data
	 */
	public ElevatorData getElevatorData() {
		return new ElevatorData(elevatorNum, currFloor, reqFloors, movingUp, movingDown, doorOpen);
	}

	/**
	 * Simulate waiting time for elevator actions
	 * 
	 * @param ms
	 *            the time to wait, in milliseconds
	 */
	public void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Print a status message in the console
	 * 
	 * @param message
	 *            the message to be printed
	 */
	public void print(String message) {
		System.out.println("Elevator " + elevatorNum + ": " + message);
	}

}
