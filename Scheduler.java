
/**
 * The Implementation of the Scheduler Class
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Scheduler {

	// Packets and Sockets
	DatagramPacket floorSendPacket, elevatorSendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;

	ArrayList<DatagramPacket> receiveQueue;

	// Send switch
	boolean sendElevator;
	boolean sendFloor;

	// Lamps and Sensors
	private boolean floorLamps[];
	private ArrayList<Integer> reqFloors;
	private boolean arrivalSensors[];

	// Total number of floors
	private final int numFloors;

	// Elevator Data List
	private ElevatorData elevData[];
	private ArrayList<ElevatorData> potentialRoutes;
	private int routedElevator;

	// Data Structures for relaying Data
	private SchedulerData scheDat;
	private FloorData floorDat;
	private ElevatorData elevDat;

	/**
	 * Create a new Scheduler with the corresponding number of floors
	 *
	 * @param numFloors
	 */
	public Scheduler(int numFloors, int numElevators) {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendSocket = new DatagramSocket();

			// Construct a datagram socket and bind it to port 4000
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.

			receiveSocket = new DatagramSocket(3000);

			// to test socket timeout (2 seconds)
			//receiveSocket.setSoTimeout(25000);

		} catch (SocketException se) {

		}

		receiveQueue = new ArrayList<DatagramPacket>();

		this.numFloors = numFloors;
		floorLamps = new boolean[numFloors];
		arrivalSensors = new boolean[numFloors];
		reqFloors = new ArrayList<Integer>();

		sendElevator = false;
		sendFloor = false;

		elevData = new ElevatorData[numElevators];
		for (int i = 0; i < numElevators; i++) {
			// Assume same starting position as set in elevator subsystem
			elevData[i] = new ElevatorData(i, 1, new ArrayList<Integer>(), false, false, false);
		}

	}

	/**
	 * Close the sockets
	 */
	public void closeSockets() {
		// We're finished, so close the sockets.
		sendSocket.close();
		receiveSocket.close();
	}

	/**
	 * Send the Floor subsystem a data packet
	 *
	 * @param scheDat
	 *            the scheduler data
	 */
	public void floorSend(SchedulerData scheDat) {

		this.scheDat = scheDat;

		try {
			// Convert the FloorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream;
			ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(scheDat);
			ooStream.flush();
			byte msg[] = baoStream.toByteArray();

			floorSendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(),
					receivePacket.getPort());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Send the datagram packet to the client via the send socket.
		try {
			sendSocket.send(floorSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		processFloorSend();

	}

	/**
	 * Receive a packet
	 */
	public void receive() {
		byte data[] = new byte[5000];
		receivePacket = new DatagramPacket(data, data.length);
		print("Scheduler: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			// print("Waiting..."); // so we know we're waiting
			receiveSocket.receive(receivePacket);
			receiveQueue.add(receivePacket);
		} catch (IOException e) {
			print("IO Exception: likely:");
			print("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void processAndSend() {

		try {
			// Retrieve the ElevatorData object from the receive packet
			if (!receiveQueue.isEmpty()) {
				for (DatagramPacket dPacket : receiveQueue) {
					ByteArrayInputStream byteStream = new ByteArrayInputStream(dPacket.getData());
					ObjectInputStream is;
					is = new ObjectInputStream(new BufferedInputStream(byteStream));
					Object o = is.readObject();
					is.close();

					if (o instanceof FloorData) {
						floorDat = (FloorData) o;
						processFloorReceived();

						updateRequests();
						routeElevator();
						elevatorSend(getSchedulerData());
						clearRequest();
					} else {
						elevDat = (ElevatorData) o;
						processElevatorReceived();

						elevData[elevDat.getElevatorNumber()] = elevDat;
						displayElevatorStates();
						manageElevators();
					}
				}
			}

			receiveQueue.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	/**
	 * Send the Elevator subsystem a data packet
	 *
	 * @param scheDat
	 *            the scheduler data
	 */
	public void elevatorSend(SchedulerData scheDat) {

		this.scheDat = scheDat;
		try {
			// Convert the FloorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream;
			ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(scheDat);
			ooStream.flush();
			byte msg[] = baoStream.toByteArray();

			elevatorSendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), 2000);// elevatorSubsystem
			// server
			// port
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Send the datagram packet to the client via the send socket.
		try {
			sendSocket.send(elevatorSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		processElevatorSend();
	}

	/**
	 * Process the received floor packet
	 */
	public void processFloorReceived() {
		print("Scheduler: Packet received.");
		print("Containing:\n	" + floorDat.getStatus() + "\n");
	}

	/**
	 * Process the received elevator packet
	 */
	public void processElevatorReceived() {
		print("Scheduler: Packet received.");
		print("Containing:\n	" + elevDat.getStatus() + "\n");
	}

	/**
	 * Process the scheduler packet sent to the Elevator subsystem
	 */
	public void processElevatorSend() {
		print("Scheduler: Sent packet to ElevatorSubsystem.");
	}

	/**
	 * Process the scheduler packet sent to the Floor subsystem
	 */
	public void processFloorSend() {
		print("Scheduler: Sent packet to FloorSubsystem.");
	}

	/**
	 * Wait 5 seconds
	 */
	public void waitTime(int ms) {
		// Slow things down (wait 5 seconds)
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Update the scheduler's floor requests
	 */
	public void updateRequests() {
		if (!reqFloors.contains(floorDat.getFloorNum()))
			reqFloors.add(floorDat.getFloorNum());
	}

	public void displayElevatorStates() {
		print("ELEVATOR STATUS:");
		for (ElevatorData e : elevData) {
			print("	" + e.getStatus());
		}
		print("\n");
	}

	/**
	 * Manage the elevator that last updated it's status
	 */
	public void manageElevators() {
		ElevatorData e = elevDat;
		SchedulerData s = null;
		int currentFloor = e.getCurrentFloor();
		
		// If elevator is on the current requested floor, stop and open doors
		if (e.getRequestedFloors().contains(currentFloor)) {
			print("SIGNAL STOP to Elevator: " + e.getElevatorNumber() + ".");
			s = new SchedulerData(e.getElevatorNumber(), SchedulerData.STOP_REQUEST, false, false, true);
		}

		//If elevator has not reached it's current destination
		else {
			// If elevator is above floor, move down, close doors
			if (currentFloor > e.getRequestedFloors().get(0) && e.isIdle()) {
				print("SIGNAL MOVE DOWN to elevator: " + e.getElevatorNumber());
				s = new SchedulerData(e.getElevatorNumber(), SchedulerData.MOVE_REQUEST, false, true,
						false);
			}
			// If elevator is below floor, move up, close doors
			else if (currentFloor < e.getRequestedFloors().get(0) && e.isIdle()) {
				print("SIGNAL MOVE UP to elevator: " + e.getElevatorNumber());
				s = new SchedulerData(e.getElevatorNumber(), SchedulerData.MOVE_REQUEST, true, false,
						false);
			}
			//If already moving towards destination floor, just tell it to continue
			else {
				print("SIGNAL CONTINUE to elevator: " + e.getElevatorNumber());
				s = new SchedulerData(e.getElevatorNumber(), SchedulerData.CONTINUE_REQUEST);
			}

		}
		if (s != null)
			elevatorSend(s);
	}

	/**
	 * Clear the scheduler's floor requests
	 */
	public void clearRequest() {
		reqFloors.clear();
	}

	/**
	 * Update the scheduler's lamps
	 */
	public void updateLamps() {
		// Update the floor lamps
		floorLamps[elevDat.getCurrentFloor() - 1] = true;
	}

	/**
	 * Returns true if there is an elevator on the same floor
	 *
	 * @return
	 */
	public boolean elevatorSameFloor() {
		potentialRoutes.clear();
		boolean caseTrue = false;
		for (int i = 0; i < elevData.length; i++) {
			if (floorDat.getFloorNum() == elevData[i].getCurrentFloor()) {
				caseTrue = true;
				potentialRoutes.add(elevData[i]);
			}
		}

		return caseTrue;
	}

	/**
	 * Returns true if there is an elevator above the requested floor
	 *
	 * @return
	 */
	public boolean elevatorAboveFloor() {
		potentialRoutes.clear();
		boolean caseTrue = false;
		for (int i = 0; i < elevData.length; i++) {
			if (elevData[i].getCurrentFloor() > floorDat.getFloorNum()) {
				caseTrue = true;
			}
		}
		return caseTrue;
	}

	/**
	 * Returns true if there is an elevator below the requested floor
	 *
	 * @return
	 */
	public boolean elevatorBelowFloor() {
		potentialRoutes.clear();
		boolean caseTrue = false;
		for (int i = 0; i < elevData.length; i++) {
			if (elevData[i].getCurrentFloor() < floorDat.getFloorNum()) {
				caseTrue = true;
			}
		}
		return caseTrue;
	}

	public boolean allElevatorsAboveFloor() {
		potentialRoutes.clear();
		for (int i = 0; i < elevData.length; i++) {
			potentialRoutes.add(elevData[i]);
			if (elevData[i].getCurrentFloor() < floorDat.getFloorNum()) {
				return false;
			}
		}
		return true;
	}


	public boolean allElevatorsBelowFloor() {
		potentialRoutes.clear();
		for (int i = 0; i < elevData.length; i++) {
			potentialRoutes.add(elevData[i]);
			if (elevData[i].getCurrentFloor() > floorDat.getFloorNum()) {
				return false;
			}
		}
		return true;
	}
	
	public int closestElevator() {
		//Assume closest is elevator 0
		ElevatorData closest = potentialRoutes.get(0);
		
		for (ElevatorData e: potentialRoutes) {
			//If elevator is closer that the current closest, it becomes the new closest
			if (Math.abs((e.getCurrentFloor() - floorDat.getFloorNum())) < Math
					.abs((closest.getCurrentFloor() - floorDat.getFloorNum()))) {
				closest = e;
			} 
		}

		return closest.getElevatorNumber();

	}

	/**
	 * Determine which elevator should get the floor request
	 */
	public void routeElevator() {

		potentialRoutes = new ArrayList<ElevatorData>();
		ArrayList<ElevatorData> optimalRoutes = new ArrayList<ElevatorData>();
		
		//If an elevator is on the same floor
		if (elevatorSameFloor()) {
			routedElevator = potentialRoutes.get(0).getElevatorNumber();
		}
		
		//If all are above or below
		else if (allElevatorsAboveFloor() || allElevatorsBelowFloor()) {
			routedElevator = closestElevator();
		}
		
		//Else, just send the closest one out of all the elevators
		else if (elevatorAboveFloor() || elevatorBelowFloor()) {
			for (ElevatorData e: elevData) {
				potentialRoutes.add(e);
			}
			
			routedElevator = closestElevator();
		}

		print("Sending request to Elevator " + routedElevator + ".\n");
		scheDat = new SchedulerData(routedElevator, SchedulerData.FLOOR_REQUEST, floorLamps, reqFloors);
	}

	/**
	 * Return the last received elevator data
	 *
	 * @return
	 */
	public ElevatorData getElevatorData() {
		return elevDat;
	}

	/**
	 * Return the scheduler's current data
	 *
	 * @return the current scheduler data
	 */
	public SchedulerData getSchedulerData() {
		return scheDat;
	}

	/**
	 * Return the last received floor data
	 *
	 * @return
	 */
	public FloorData getFloorData() {
		return floorDat;
	}

	/**
	 * Prints the message on the console
	 *
	 * @param message
	 */
	public void print(String message) {
		System.out.println(message);
	}

	public static void main(String args[]) {
		//Initialize a scheduler for a system with 5 floors and 2 elevators
		Scheduler c = new Scheduler(5, 2);

		/**
		 * Scheduler Logic
		 */
		while (true) {
			c.receive();
			c.processAndSend();
			c.waitTime(1000);
		}

	}
}
