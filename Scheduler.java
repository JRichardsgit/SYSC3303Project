
/**
 * The Implementation of the Scheduler Class
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Scheduler {

	//Packets and Sockets
	DatagramPacket floorSendPacket, elevatorSendPacket, floorReceivePacket, elevatorReceivePacket;
	DatagramSocket sendSocket, floorReceiveSocket, elevatorReceiveSocket;

	//Lamps and Sensors
	private boolean floorLamps[];
	private ArrayList<Integer> reqFloors;
	private boolean arrivalSensors[];

	//Total number of floors
	private final int numFloors;
	
	//Elevator Data List
	private ElevatorData elevDataList[];

	//Data Structures for relaying Data
	private SchedulerData scheDat;
	private FloorData floorDat;
	private ElevatorData elevDat;

	/**
	 * Create a new Scheduler with the corresponding number of floors
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
			floorReceiveSocket = new DatagramSocket(3000);
			
			// Construct a datagram socket and bind it to port 4000
			// on the local host machine. This socket will be used to
		    // receive UDP Datagram packets.
		    elevatorReceiveSocket = new DatagramSocket(4000);

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		this.numFloors = numFloors;
		floorLamps = new boolean[numFloors];
		arrivalSensors = new boolean[numFloors];
		reqFloors = new ArrayList<Integer>();
		
		elevDataList = new ElevatorData[numElevators];

	}

	/*
	public void receiveAndReply() {
		floorReceive();
		elevatorSend();
		wait5s();
		elevatorReceive();
		floorSend();
	}
	*/

	/**
	 * Close the sockets
	 */
	public void closeSockets() {
		// We're finished, so close the sockets.
		sendSocket.close();
		floorReceiveSocket.close();
		elevatorReceiveSocket.close();
	}

	/**
	 * Send the Floor subsystem a data packet
	 * @param scheDat the scheduler data
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

			floorSendPacket = new DatagramPacket(msg, msg.length, floorReceivePacket.getAddress(),
					floorReceivePacket.getPort());
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
		print("Scheduler: Packet sent to FloorSubsystem.\n");

	}

	/**
	 * Receive a packet from the Floor subsystem
	 */
	public void floorReceive() {
		// Construct a DatagramPacket for receiving packets up
		// to 100 bytes long (the length of the byte array).

		byte data[] = new byte[5000];
		floorReceivePacket = new DatagramPacket(data, data.length);
		print("Scheduler: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			print("Waiting..."); // so we know we're waiting
			floorReceiveSocket.receive(floorReceivePacket);
		} catch (IOException e) {
			print("IO Exception: likely:");
			print("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		try {
			// Retrieve the ElevatorData object from the receive packet
			ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
			ObjectInputStream is;
			is = new ObjectInputStream(new BufferedInputStream(byteStream));
			Object o = is.readObject();
			is.close();

			floorDat = (FloorData) o;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		processFloorReceived();
	}

	/**
	 * Send the Elevator subsystem a data packet
	 * @param scheDat the scheduler data
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

			elevatorSendPacket = new DatagramPacket(msg, msg.length, floorReceivePacket.getAddress(), 2000);// elevatorSubsystem
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

		print("Scheduler: Packet sent to ElevatorSubsystem.\n");
	}

	/**
	 * Receive a packet from the Elevator subsystem
	 */
	public void elevatorReceive() {
		// Construct a DatagramPacket for receiving packets up
		// to 100 bytes long (the length of the byte array).

		byte data[] = new byte[5000];
		elevatorReceivePacket = new DatagramPacket(data, data.length);
		print("Scheduler: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			print("Waiting..."); // so we know we're waiting
			elevatorReceiveSocket.receive(elevatorReceivePacket);
		} catch (IOException e) {
			print("IO Exception: likely:");
			print("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		try {
			// Retrieve the ElevatorData object from the receive packet
			ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
			ObjectInputStream is;
			is = new ObjectInputStream(new BufferedInputStream(byteStream));
			Object o = is.readObject();
			is.close();

			elevDat = (ElevatorData) o;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		processElevatorReceived();
	}

	/**
	 * Process the received floor packet
	 */
	public void processFloorReceived() {
		print("Scheduler: Packet received.");
		print("From FloorSubsystem: " + floorReceivePacket.getAddress());
		print("Host port: " + floorReceivePacket.getPort());
		print("Packet Length: " + floorReceivePacket.getLength());
		print("Containing: \n" + floorDat.getStatus() + "\n");
	}

	/**
	 * Process the received elevator packet
	 */
	public void processElevatorReceived() {
		print("Scheduler: Packet received.");
		print("From ElevatorSubsystem: " + elevatorReceivePacket.getAddress());
		print("Host port: " + elevatorReceivePacket.getPort());
		print("Packet length: " + elevatorReceivePacket.getLength());
		print("Containing: \n" + elevDat.getStatus() + "\n");
		
		elevDataList[elevDat.getElevatorNumber()] = elevDat;
	}

	/**
	 * Process the scheduler packet sent to the Elevator subsystem
	 */
	public void processElevatorSend() {
		print("Scheduler: Sending packet to ElevatorSubsystem.");
		print("To host: " + elevatorSendPacket.getAddress());
		print("Destination host port: " + 2000);
		print("Length: " + elevatorSendPacket.getLength());
		print("Containing: \n" + scheDat.getStatus() + "\n");

	}

	/**
	 * Process the scheduler packet sent to the Floor subsystem
	 */
	public void processFloorSend() {
		print("Scheduler: Sending packet to FloorSubsystem.");
		print("To host: " + floorSendPacket.getAddress());
		print("Destination host port: " + floorSendPacket.getPort());
		print("Length: " + floorSendPacket.getLength());
		print("Containing: " + scheDat.getStatus() + "\n");

	}

	/**
	 * Wait 5 seconds
	 */
	public void wait5s() {
		// Slow things down (wait 5 seconds)
		try {
			Thread.sleep(8000);
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
	
	/**
	 * Update the scheduler's lamps
	 */
	public void updateLamps() {
		//Update the floor lamps
		floorLamps[elevDat.getCurrentFloor()] = true;
	}
	
	/**
	 * Determine which elevator should get the floor request
	 */
	public void routeElevator() {
		/**
		 * LOGIC for scheduling goes HERE
		 */
		//Hard coded to send to elevator 1 -- for testing purposes
		scheDat = new SchedulerData(1, floorLamps, reqFloors);
	}

	/**
	 * Return the last received elevator data
	 * @return
	 */
	public ElevatorData getElevatorData() {
		return elevDat;
	}

	/**
	 * Return the scheduler's current data
	 * @return the current scheduler data
	 */
	public SchedulerData getSchedulerData() {
		return scheDat;
	}

	/**
	 * Return the last received floor data
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
		Scheduler c = new Scheduler(5, 2);

		/**
		 * Scheduler Logic
		 */
		while (true) {
			//Receive a request from a floor
			c.floorReceive();
			//Update current data
			c.updateRequests();
			
			//Route appropriate elevator
			c.routeElevator();
			//Relay request to appropriate elevator
			c.elevatorSend(c.getSchedulerData());
			
			//Receive input data from elevator to light appropriate lamps
			c.elevatorReceive();
			//Light appropriate lamps
			c.updateLamps();
		}

	}
}
