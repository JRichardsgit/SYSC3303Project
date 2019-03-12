
/**
 * The Implementation of the FloorSubsystem Class
 */

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FloorSubsystem {

	//Sockets and Packets
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;

	//Data Structures for relaying data
	private FloorData floorDat;
	private SchedulerData scheDat;

	//List of floors
	private Floor floors[];

	/**
	 * Create a new floor subsystem
	 * @param numFloors number of floors
	 */
	public FloorSubsystem(int numFloors) {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) { // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}

		floors = new Floor[numFloors];

		for (int i = 0; i < numFloors; i ++) {
			floors[i] = new Floor(i + 1, this);
		}
	}

	/**
	 * Send packet to scheduler
	 * @param floorDat the floor data to be sent
	 */
	public void send(FloorData floorDat) {
		// Prepare a DatagramPacket and send it via sendReceiveSocket
		// to port 4000 on the destination host.
		this.floorDat = floorDat;

		try {
			// Convert the FloorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(floorDat);
			ooStream.flush();
			byte msg[] = baoStream.toByteArray();

			// Construct a datagram packet that is to be sent to a specified port
			sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 3000);

			// Send the datagram packet to the server via the send/receive socket.
			sendReceiveSocket.send(sendPacket);

			print("FloorSubsystem: Request sent to scheduler.\n");
			print("Containing:\n " + floorDat.getStatus() + "\n");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
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

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("Waiting..."); // so we know we're waiting
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		try {
			// Retrieve the SchedulerData object from the receive packet
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

		print("FloorSubsystem: Packet received:");
	}

	/**
	 * Close the data socket
	 */
	public void closeSocket() {
		sendReceiveSocket.close();
	}
	
	/**
	 * Return the last sent floor data packet
	 * @return the floor data
	 */

	public FloorData getFloorData() {
		return floorDat;
	}

	/**
	 * Set the floor data
	 * @param floorDat
	 */
	public void setFloorData(FloorData floorDat) {
		this.floorDat = floorDat;
	}
	
	/**
	 * Go up from the specified floor 
	 * @param floorNum
	 */
	public void goUp(int currFloor, int destFloor) {
		Floor floor = getFloor(currFloor);
		
		floor.pressUp();
		floor.setDestination(destFloor);
		
		send(floor.getFloorData());
	}
	
	/**
	 * Go down from the specified floor
	 * @param floorNum
	 */
	public void goDown(int currFloor, int destFloor) {
		Floor floor = getFloor(currFloor);
		
		floor.pressDown();
		floor.setDestination(destFloor);
		
		send(floor.getFloorData());
	}

	/**
	 * Return the last received scheduler data packet
	 * @return
	 */
	public SchedulerData getSchedulerData() {
		return scheDat;
	}

	/**
	 * Return the floor with the corresponding floor number
	 * @param floorNum the floor number
	 * @return corresponding floor
	 */
	public Floor getFloor(int floorNum) {
		return floors[floorNum - 1];
	}

	/**
	 * Prints the message on the console
	 * @param message
	 */
	public void print(String message) {
		System.out.println(message);
	}
	
	/**
	 * Sleep for the specified time
	 * @param ms
	 */
	public void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void main(String args[]) {

		//Create a floor subsystem with 5 floors
		FloorSubsystem c = new FloorSubsystem(5);

		/**
		 * FLOOR SIMULATION
		 *
		 * Floor simulation data to be later read in by input file.
		 * For now, hard code simulation data.
		 */

		c.goUp(4, 5);
		c.wait(5000);

		c.goUp(2, 4);
		c.wait(5000);

		c.goUp(1, 3);
		c.wait(5000);
		
		c.goDown(5, 1);
		c.wait(5000);
		
		c.closeSocket();
	}
}
