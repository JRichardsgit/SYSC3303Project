
/**
 * The Implementation of the Scheduler Class
 */

import java.io.*;
import java.net.*;

public class Scheduler {

	DatagramPacket floorSendPacket, elevatorSendPacket, floorReceivePacket, elevatorReceivePacket;
	DatagramSocket sendSocket, receiveSocket;

	private boolean floorLamps[];
	private int destFloors[];
	private final int numFloors;
	
	private SchedulerData scheDat;
	private FloorData floorDat;
	private ElevatorData elevDat;
	
	private String elevatorStatus;

	public Scheduler(int numFloors) {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendSocket = new DatagramSocket();

			// Construct a datagram socket and bind it to port 5000
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(4000);

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		this.numFloors = numFloors;
		floorLamps = new boolean[numFloors];
	}

	public void receiveAndReply() {
		floorReceive();
		elevatorSend();
		wait5s();
		elevatorReceive();
		floorSend();

		// We're finished, so close the sockets.
		sendSocket.close();
		receiveSocket.close();
	}

	public void floorSend() {
		String s = "i am the scheduler packet letting you know the message request has been recieved. "
				+ "I will now get an elevator for you.";

		try {
			scheDat = new SchedulerData(floorLamps, destFloors);
			scheDat.setStatus("Scheduler received reply from Elevator. Elevator is arriving shortly.");
			
			//Convert the FloorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream;
			ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(scheDat);
			ooStream.flush();
			byte msg[] = baoStream.toByteArray();
		
			floorSendPacket = new DatagramPacket(msg, msg.length, floorReceivePacket.getAddress(), floorReceivePacket.getPort());
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
		System.out.println("Scheduler: Packet sent to floor.\n");

	}

	public void floorReceive() {
		// Construct a DatagramPacket for receiving packets up
		// to 100 bytes long (the length of the byte array).

		byte data[] = new byte[5000];
		floorReceivePacket = new DatagramPacket(data, data.length);
		System.out.println("Scheduler: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("Waiting..."); // so we know we're waiting
			receiveSocket.receive(floorReceivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
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
			
			floorDat = (FloorData) o;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		processFloorReceived();
		System.out.println("Scheduler: Packet received from floor.\n");
	}

	public void elevatorSend() {
	
		try {
			scheDat = new SchedulerData(floorLamps, destFloors);
			scheDat.setStatus("Scheduler received the floor request. Relaying request to elevator.");
			
			//Convert the FloorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream;
			ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(scheDat);
			ooStream.flush();
			byte msg[] = baoStream.toByteArray();
		
			elevatorSendPacket = new DatagramPacket(msg, msg.length, floorReceivePacket.getAddress(), 2000);// elevator server
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

		System.out.println("Scheduler: Packet sent to elevator.");
	}

	public void elevatorReceive() {
		// Construct a DatagramPacket for receiving packets up
		// to 100 bytes long (the length of the byte array).

		byte data[] = new byte[5000];
		elevatorReceivePacket = new DatagramPacket(data, data.length);
		System.out.println("Scheduler: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("Waiting..."); // so we know we're waiting
			receiveSocket.receive(elevatorReceivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
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
			
			elevDat = (ElevatorData) o;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		processElevatorReceived();

		System.out.println("Scheduler: Packet received from elevator.");
	}

	public void processFloorReceived() {
		System.out.println("Scheduler: Packet received.");
		System.out.println("From host: " + floorReceivePacket.getAddress());
		System.out.println("Host port: " + floorReceivePacket.getPort());
		int len = floorReceivePacket.getLength();
		System.out.println("Length: " + len);
		//System.out.print("Containing: ");
	}
	
	public void processElevatorReceived() {
		System.out.println("Scheduler: Packet received.");
		System.out.println("From host: " + elevatorReceivePacket.getAddress());
		System.out.println("Host port: " + elevatorReceivePacket.getPort());
		int len = elevatorReceivePacket.getLength();
		System.out.println("Length: " + len);
		//System.out.print("Containing: ");
	}

	public void processElevatorSend() {
		System.out.println("Scheduler: Sending packet to elevator.");
		System.out.println("To host: " + elevatorSendPacket.getAddress());
		System.out.println("Destination host port: " + 2000);
		int len = elevatorSendPacket.getLength();
		System.out.println("Length: " + len);
		//System.out.print("Containing: ");

	}

	public void processFloorSend() {
		System.out.println("Scheduler: Sending packet to floor.");
		System.out.println("To host: " + floorSendPacket.getAddress());
		System.out.println("Destination host port: " + floorSendPacket.getPort());
		int len = floorSendPacket.getLength();
		System.out.println("Length: " + len);
		//System.out.print("Containing: ");

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

	public static void main(String args[]) {
		Scheduler c = new Scheduler(5);
		c.receiveAndReply();

	}
}
