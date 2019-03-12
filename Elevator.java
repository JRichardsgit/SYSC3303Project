import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * Elevator Node Implementation
 */
public class Elevator extends Thread {

	// Socket and Packet
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;

	// Port
	private int port;

	// Scheduler address for sending packets
	private InetAddress schedulerAddress;

	// Scheduler Packet Data
	private SchedulerData scheDat;

	// Elevator Number
	private final int elevatorNum;

	// Number of floors
	private final int numFloors;

	// Motor flags
	private boolean movingUp;
	private boolean movingDown;

	// Door flag
	private boolean doorOpen;

	// Shut down flag, if error occurs
	private boolean shutdown;
	// Door stuck flag
	private boolean doorStuck;

	// Reference to Elevator Subsystem
	private ElevatorSubsystem eSystem;

	// Floor Information
	private ArrayList<Integer> reqFloors;
	private ArrayList<Integer> destFloors[];
	private int currFloor;

	//Ready flag
	private boolean actionReady;

	//Error List
	private ArrayList<ErrorEvent> errorList;

	//FloorTimers
	private final long initializedTime;
	private long startTime, endTime, elapsedTime;
	private long expectedTime;

	/**
	 * Creates a new elevator node
	 * 
	 * @param elevatorNum
	 *            this elevator's number
	 * @param eSystem
	 *            reference to the elevator subsystem
	 */
	public Elevator(int elevatorNum, int numFloors, ElevatorSubsystem eSystem, int port) {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendSocket = new DatagramSocket();

			// Construct a datagram socket and bind it to the specified port
			// port on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(port);

		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		this.elevatorNum = elevatorNum;
		this.numFloors = numFloors;
		this.eSystem = eSystem;
		this.port = port;
		movingUp = false;
		movingDown = false;
		doorOpen = false;
		currFloor = 1;
		actionReady = false;
		shutdown = false;
		doorStuck = false;
		reqFloors = new ArrayList<Integer>();
		destFloors = new ArrayList[numFloors];
		errorList = new ArrayList<ErrorEvent>();
		initializedTime = System.nanoTime();
		expectedTime = 10000;

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
	 * Add an error event to the errorList
	 * @param err the error event
	 */
	public void addError(ErrorEvent err) {
		errorList.add(err);
	}

	/**
	 * Returns true if the elevator is shut down, no longer operational
	 * @return true if the elevator is shut down, no longer operational
	 */
	public boolean isShutDown() {
		return shutdown;
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
	 * Receive a packet from the scheduler
	 */
	public void receive() {
		// Construct a DatagramPacket for receiving packets up
		// to 5000 bytes long (the length of the byte array).

		byte data[] = new byte[5000];
		receivePacket = new DatagramPacket(data, data.length);
		// Block until a datagram packet is received from receiveSocket.
		try {
			receiveSocket.receive(receivePacket);
			schedulerAddress = receivePacket.getAddress();

		} catch (IOException e) {
			print("IO Exception: likely:");
			print("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		try {
			//Retrieve the ElevatorData object from the receive packet
			ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
			ObjectInputStream is;
			is = new ObjectInputStream(new BufferedInputStream(byteStream));
			Object o = is.readObject();
			is.close();

			scheDat = (SchedulerData) o;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		processPacket();
	}

	/**
	 * Process the received scheduler packet
	 */
	public void processPacket() {
		SchedulerData s = scheDat;
		int mode = s.getMode();
		switch (mode) {
		case SchedulerData.CONTINUE_REQUEST:
			print("Received CONTINUE request.\n");
			//Elevator's motor flags and door flags remain unchanged
			break;

		case SchedulerData.FLOOR_REQUEST:
			print("Received FLOOR request.\n");
			int floor = s.getReqFloors().get(0);

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
		case SchedulerData.DOOR_REQUEST:
			print("Received DOOR request.\n");
			//Will proceed to opening/closing it's doors
			break;
		}
	}

	/**
	 * Wait until the scheduler sends a request
	 */
	public void waitForInstruction() {
		//Set wait flag true
		//eSystem.setPending(elevatorNum, true);
		print("Awaiting Instruction.\n");
		receive();
		/*
		while (eSystem.isPending(elevatorNum)) {
			wait(1000);
		}
		 */
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
			if (!errorList.isEmpty()) {
				ErrorEvent e = errorList.get(0);
				//If the elevator is stuck, shut it down
				if (e.getType() == ErrorEvent.ELEVATOR_STUCK) {
					if (movingUp) {
						print("Stuck between floors " + currFloor + " and " + (currFloor + 1) + ".");
					} else {
						print("Stuck between floors " + currFloor + " and " + (currFloor - 1) + ".");
					}
					shutdown = true;
					send();
				}
			}

			//If it is working
			if (!shutdown) {
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

		if (!errorList.isEmpty()) {
			ErrorEvent e = errorList.get(0);

			if (e.getType() == ErrorEvent.DOOR_STUCK) {
				doorStuck = true;
				wait(2000);
				send();
				waitForInstruction();
				errorList.remove(0);
			}
		}

		wait(2000);
		print("Doors opened.");
		doorOpen = true;
		doorStuck = false;
		actionReady = true;
	}

	/**
	 * Set the flag for closing the elevator doors
	 */
	public void closeDoor() {
		actionReady = false;
		print("Closing doors.");

		if (!errorList.isEmpty()) {
			ErrorEvent e = errorList.get(0);
			if (e.getType() == ErrorEvent.DOOR_STUCK) {
				doorStuck = true;
				wait(2000);
				send();
				waitForInstruction();
				errorList.remove(0);
			}
		}

		wait(2000);
		print("Doors closed.");
		doorOpen = false;
		doorStuck = false;
		actionReady = true;
	}

	/**
	 * Return this elevator's data
	 * @return this elevator's data
	 */
	public ElevatorData getElevatorData() {
		int errType;

		if (doorStuck) {
			errType = ElevatorData.DOOR_STUCK_ERROR;
		}
		else if (shutdown) {
			errType = ElevatorData.ELEVATOR_STUCK_ERROR;
		}
		else {
			errType = ElevatorData.NO_ERROR;
		}

		return new ElevatorData(elevatorNum, port, errType, currFloor, 
				reqFloors, movingUp, movingDown, doorOpen, shutdown);
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

		print("Started.");
		waitForInstruction();

		while (true) {
			//If there are requested floors, and the elevator is ready to move
			//(Doors are closed/no pending requests)
			if (!reqFloors.isEmpty() && actionReady) {
				startTime = System.currentTimeMillis();
				moveOneFloor();

				if (!shutdown) {
					//Update the scheduler about current status
					send();
					//Pending for further instruction from the scheduler
					waitForInstruction();
				}

				//Measure the time it took for the elevator to receive the instruction
				endTime = System.currentTimeMillis();
				elapsedTime = endTime - startTime;

				//print("Time between floors: " + elapsedTime + " ms.");

				//If the current time for instruction receiving exceeds the expected, 
				//trigger elevator stuck event
				if (elapsedTime > expectedTime) {
					errorList.add(0, new ErrorEvent(ErrorEvent.ELEVATOR_STUCK, 0));
				}
			}
			//Slow down a bit
			wait(1000);
		}
	}

}
