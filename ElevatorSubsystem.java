
/**
 * This implementation of the ElevatorSubsystem Class
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ElevatorSubsystem extends Thread {

	//Sockets and Packets
	DatagramPacket receivePacket;
	DatagramSocket receiveSocket;
	
	InetAddress schedulerAddress;

	//Data Structures for relaying data
	private ElevatorData elevDat;
	private SchedulerData scheDat;
	
	//List of elevators
	private Elevator elevatorList[];
	
	private boolean elevatorPending[];
	/**
	 * Create a new elevator subsystem with numElevators
	 * @param numElevators the number of elevators in the system
	 */
	public ElevatorSubsystem( int numFloors, int numElevators) {
		try {
			// Construct a datagram socket and bind it to port 2000
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(2000);

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		schedulerAddress = null;
		elevatorList = new Elevator[numElevators];
		elevatorPending = new boolean[numElevators];
		
		for (int i = 0; i < numElevators; i ++) {
			elevatorList[i] = (new Elevator(i, numFloors, this));
			elevatorPending[i] = false;
			print("Elevator " + i + " started.");
			elevatorList[i].start();
		}
	}


	/**
	 * Receive a packet from the scheduler
	 */
	public void receive() {
		// Construct a DatagramPacket for receiving packets up
		// to 5000 bytes long (the length of the byte array).

		byte data[] = new byte[5000];
		receivePacket = new DatagramPacket(data, data.length);
		print("Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			//print("Waiting..."); // so we know we're waiting
			receiveSocket.receive(receivePacket);
			
			if (schedulerAddress == null) {
				schedulerAddress = receivePacket.getAddress();
				
				for (Elevator elevator: elevatorList) {
					elevator.setSchedulerAddress(schedulerAddress);
				}
			}
			
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

		print("Packet received.");
	}
	
	public InetAddress getSchedulerAddress() {
		return receivePacket.getAddress();
	}

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
	 * Returns the last sent elevator packet
	 * @return the last sent elevator packet
	 */
	public ElevatorData getElevatorData() {
		return elevDat;
	}
	
	public void setPending(int elevatorNum, boolean pending) {
		elevatorPending[elevatorNum] = pending;
	}
	
	public boolean isPending(int elevatorNum) {
		return elevatorPending[elevatorNum];
	}
	
	/**
	 * Returns the last received scheduler packet
	 * @return the last received scheduler packet
	 */
	public SchedulerData getSchedulerData() {
		return scheDat;
	}
	
	/**
	 * Returns the elevator with the corresponding elevator number
	 * @param elevatorNum the elevator number
	 * @return corresponding elevator
	 */
	public Elevator getElevator(int elevatorNum) {
		return elevatorList[elevatorNum];
	}
	
	public void routePacket() {
		int routedElevatorNumber = scheDat.getElevatorNumber();
		Elevator routedElevator = elevatorList[routedElevatorNumber];
		
		print("Routing to Elevator " + routedElevatorNumber + ".\n");

		elevatorPending[routedElevatorNumber] = false;
		
		wait(1000);
		routedElevator.receiveRequest(scheDat);
	}
	
	/**
	 * Close the sockets
	 */
	public void closeSockets() {
		// We're finished, so close the sockets.
		receiveSocket.close();
	}
	
	/**
	 * Print a status message in the console
	 * @param message the message to be printed
	 */
	public void print(String message) {
		System.out.println("ELEVATOR SUBSYSTEM: " + message);
	}
	
	public void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		/**
		 * Elevator subsystem logic
		 */
		
		while(true) {
			receive();
			routePacket();
		}
	}
	

	public static void main(String args[]) {
		//Initialize a system with 5 floors and 2 elevators
		ElevatorSubsystem c = new ElevatorSubsystem(5, 2);
		c.start();
	}
}
