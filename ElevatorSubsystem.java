
/**
 * This implementation of the ElevatorSubsystem Class
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ElevatorSubsystem {

	//Sockets and Packets
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;

	//Data Structures for relaying data
	private ElevatorData elevDat;
	private SchedulerData scheDat;
	
	//List of elevators
	private Elevator elevatorList[];

	/**
	 * Create a new elevator subsystem with numElevators
	 * @param numElevators the number of elevators in the system
	 */
	public ElevatorSubsystem( int numFloors, int numElevators) {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendSocket = new DatagramSocket();

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
		
		elevatorList = new Elevator[numElevators];
		
		for (int i = 0; i < numElevators; i ++) {
			elevatorList[i] = (new Elevator(i, numFloors, this));
		}
	}

	/**
	public void receiveAndReply() {
		receive();
		wait5s();
		send();
		// We're finished, so close the sockets.
		sendSocket.close();
		receiveSocket.close();
	}
	*/

	/**
	 * Send a packet to the scheduler
	 */
	public void send(ElevatorData elevDat) {
		
		this.elevDat = elevDat;
		try {
			// Convert the ElevatorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(elevDat);
			ooStream.flush();

			byte msg[] = baoStream.toByteArray();
			sendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), 4000);

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

		print("ElevatorSubsystem: Packet sent to scheduler.\n");

	}

	/**
	 * Receive a packet from the scheduler
	 */
	public void receive() {
		// Construct a DatagramPacket for receiving packets up
		// to 5000 bytes long (the length of the byte array).

		byte data[] = new byte[5000];
		receivePacket = new DatagramPacket(data, data.length);
		print("ElevatorSubsystem: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			print("Waiting..."); // so we know we're waiting
			receiveSocket.receive(receivePacket);
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

		processReceive();
		
		print("ElevatorSubsystem: Packet received from scheduler.\n");
	}

	/**
	 * Process the sent packet
	 */
	public void processSend() {
		print("ElevatorSubsystem: Sending packet:");
		print("To host: Scheduler");
		print("Destination host port: " + sendPacket.getPort());
		print("Length: " + sendPacket.getLength());
		print("Containing: \n" + elevDat.getStatus() + "\n");
	}

	/**
	 * Process the received packet
	 */
	public void processReceive() {
		// Process the received datagram.
		print("ElevatorSubsystem: Packet received:");
		print("From host: " + receivePacket.getAddress());
		print("Host port: " + receivePacket.getPort());
		print("Length: " + receivePacket.getLength());
		print("Containing: \n" + scheDat.getStatus() + "\n");
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
		getElevator(scheDat.getElevatorNumber()).receiveRequest(scheDat.getReqFloors());
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
	 * Print a status message in the console
	 * @param message the message to be printed
	 */
	public void print(String message) {
		System.out.println(message);
	}

	public static void main(String args[]) {
		ElevatorSubsystem c = new ElevatorSubsystem(5, 2);
		
		/**
		 * Elevator subsystem logic
		 */
		
		while(true) {
			c.receive();
			c.routePacket();
		}

	}
}
