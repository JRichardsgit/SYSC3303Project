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

	// Socket and Packet
	DatagramPacket sendPacket;
	DatagramSocket sendSocket;

	// Scheduler address for sending packets
	InetAddress schedulerAddress;

	// Elevator Number
	private final int elevatorNum;

	// Number of floors
	private final int numFloors;

	// Motor flags
	private boolean movingUp;
	private boolean movingDown;

	// Door flag
	private boolean doorOpen;

	// Reference to Elevator Subsystem
	private ElevatorSubsystem eSystem;

	// Floor Information
	private ArrayList<Integer> reqFloors;
	private ArrayList<Integer> destFloors[];
	private int currFloor;

	//Ready flag
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

		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		this.elevatorNum = elevatorNum;
		this.numFloors = numFloors;
		this.eSystem = eSystem;
		movingUp = false;
		movingDown = false;
		doorOpen = false;
		currFloor = 1;
		actionReady = false;
		reqFloors = new ArrayList<Integer>();
		destFloors = new ArrayList[numFloors];
		
		for (int i = 0; i < numFloors; i ++) {
			destFloors[i] = new ArrayList<Integer>();
		}

	}

	/**
	 * Set the scheduler address for sending packets
	 * @param address
	 */
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
	 * Receive the relayed scheduler packet
	 * @param s the scheduler packet relayed by the ElevatorSubsystem
	 */
	public void receiveRequest(SchedulerData s) {

		int mode = s.getMode();
		switch (mode) {
		case SchedulerData.FLOOR_REQUEST:
			print("Received FLOOR request.\n");
			int floor = s.getReqFloor();

			//Add the requested floor to the list
			if (!reqFloors.contains(floor))
				reqFloors.add(floor);
			
			//Update the destination floors for the requested floor
			//(floor requests that will be added when the elevator reaches that floor)
			destFloors[floor - 1].add(s.getDestFloor());
		
			print("Current requests: " + reqFloors.toString());
			break;

		case SchedulerData.MOVE_REQUEST:
			print("Received MOVE request.\n");
			if (doorOpen) //If door open, close the door before moving
				closeDoor();

			if (s.moveUp()) { //If request was to move up
				moveUp();
			} else {
				moveDown(); //If request was to move down
			} 
			break;

		case SchedulerData.CONTINUE_REQUEST:
			print("Received CONTINUE request.\n");
			//Elevator's motor flags and door flags remain unchanged
			break;

		case SchedulerData.STOP_REQUEST:
			print("Received STOP request.\n");
			//Stop the motor and open the door
			stopMotor();
			openDoor();

			print("Arrived at floor " + currFloor + ".\n");
			
			if (!reqFloors.isEmpty()) {
				//Remove the floor that we arrived at from the requested floors
				reqFloors.remove(new Integer(currFloor));
				
				//Add all the floor destinations from people who just boarded to the requested floors
				reqFloors.removeAll(destFloors[currFloor - 1]);
				reqFloors.addAll(destFloors[currFloor - 1]);
				
				//Clear the destination floors from that floor
				destFloors[currFloor - 1].clear();
			} 
			
			send();
			break;
		}
	}

	/**
	 * Wait until the scheduler sends a request
	 */
	public void waitForInstruction() {
		//Set wait flag true
		eSystem.setPending(elevatorNum, true);
		print("Awaiting Instruction.\n");
		while (eSystem.isPending(elevatorNum)) {
			wait(1000);
		}
		//Ready for another action
		actionReady = true;
	}

	/**
	 * Operates on a one floor basis determined by the set motor flags, if elevator is idle, does nothing
	 */
	public void moveOneFloor() {
		//If door is open, wait until closed before moving
		while (doorOpen && !isIdle()) {
			wait(1000);
		}

		//If elevator is moving
		if (!isIdle()) {
			if (movingUp) {
				currFloor++;
				if (currFloor > numFloors) {
					currFloor = numFloors;
				}
				print("Currently on floor " + currFloor + ", moving up.");
			} else if (movingDown) {
				currFloor--;
				if (currFloor <= 0) {
					currFloor = 1;
				}
				print("Currently on floor " + currFloor + ", moving down.");
			} 
			wait(1000);
		} 
		
		//If the elevator has just stopped and there are no more outstanding requests
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
	 * Returns true if the motor idle
	 */
	public boolean isIdle() {
		return (!movingUp && !movingDown);
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
	 * @return this elevator's data
	 */
	public ElevatorData getElevatorData() {
		return new ElevatorData(elevatorNum, currFloor, reqFloors, movingUp, movingDown, doorOpen);
	}

	/**
	 * Simulate waiting time for elevator actions, and for delays
	 * @param ms the time to wait, in milliseconds
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
	 * @param message the message to be printed
	 */
	public void print(String message) {
		System.out.println("Elevator " + elevatorNum + ": " + message);
	}

	@Override
	public void run() {
	
		waitForInstruction();
	
		while (true) {
			//If there are requested floors, and the elevator is ready to move
			//(Doors are closed/no pending requests)
			if (!reqFloors.isEmpty() && actionReady) {
				moveOneFloor();
				//Update the scheduler about current status
				send();
				//Pending for further instruction from the scheduler
				waitForInstruction();
			}
			//Slow down a bit
			wait(1000);
		}
	}

}
