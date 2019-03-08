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
		requestAvailable = false;
	}
	
	
	@Override
	public void run() {
		
		while (true) {
			if (!reqFloors.isEmpty()) {
				print("Elevator " + elevatorNum + ": " + reqFloors.toString());
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
	 * Send a packet to the scheduler
	 */
	public void send(ElevatorData elevDat) {
		
		try {
			// Convert the ElevatorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(elevDat);
			ooStream.flush();

			byte msg[] = baoStream.toByteArray();
			sendPacket = new DatagramPacket(msg, msg.length, eSystem.getSchedulerAddress(), 3000);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		processSend();

		// Send the datagram packet to the client via the send socket.
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Elevator: " + elevatorNum + ": Packet sent to scheduler.\n");

	}
	
	/**
	 * Process the sent packet
	 */
	public void processSend() {
		print("Elevator " + elevatorNum + ": Sending packet:");
		print("To host: Scheduler");
		print("Destination host port: " + sendPacket.getPort());
		print("Length: " + sendPacket.getLength());
		print("Containing: \n" + getElevatorData().getStatus() + "\n");
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
		print("Elevator " + elevatorNum + ": currently on floor " + currFloor + ".");
		print("Elevator " + elevatorNum + ": moving to floor " + floorNum + ".\n");
		
		if (floorNum > currFloor) 
			moveUp();
	
		else if (floorNum < currFloor) 
			moveDown();
		
		while (floorNum != currFloor) {
			moveOneFloor();
		}
			
		print("Elevator " + elevatorNum +
				": arrived at floor " + currFloor + ".\n");
		
		openDoor();
		closeDoor();
	}
	
	public void moveOneFloor() {
		if (isMovingUp())
			currFloor ++;
		else if (isMovingDown())
			currFloor --;
		
		simulateWait(3000);
		
		send(getElevatorData());
	}
	
	public void moveToNextFloor() {
		moveToFloor(reqFloors.get(0));
		reqFloors.remove(0);
		moveStop();

		send(getElevatorData());
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
		print("Elevator " + elevatorNum + ": opening doors.\n");
		simulateWait(2000);
	}
	
	/**
	 * Set the flag for closing the elevator doors
	 */
	public void closeDoor() {
		doorOpen = false;
		print("Elevator " + elevatorNum + ": closing doors.\n");
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
		
		send(getElevatorData());
		
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
