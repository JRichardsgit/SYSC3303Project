
/**
 * This implementation of the ElevatorSubsystem Class
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ElevatorSubsystem {

	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;

	private ElevatorData elevDat;
	private SchedulerData scheDat;
	
	private ArrayList<Elevator> elevatorList;

	public ElevatorSubsystem() {
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
		
		elevatorList = new ArrayList<Elevator>();
	}

	public void receiveAndReply() {
		receive();
		wait5s();
		send();
		// We're finished, so close the sockets.
		sendSocket.close();
		receiveSocket.close();
	}

	public void send() {

		try {
			elevDat.setStatus("ElevatorSubsystem received request. Heading to destination floorSubsystem.");
			// Convert the FloorData object into a byte array
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

		System.out.println("ElevatorSubsystem: Packet sent to scheduler.\n");

	}

	public void receive() {
		// Construct a DatagramPacket for receiving packets up
		// to 100 bytes long (the length of the byte array).

		byte data[] = new byte[5000];
		receivePacket = new DatagramPacket(data, data.length);
		System.out.println("ElevatorSubsystem: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("Waiting..."); // so we know we're waiting
			receiveSocket.receive(receivePacket);
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
			
			scheDat = (SchedulerData) o;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		processReceive();
		
		System.out.println("ElevatorSubsystem: Packet received from scheduler.\n");
	}

	public void processSend() {
		System.out.println("ElevatorSubsystem: Sending packet:");
		System.out.println("To host: elevatorSubsystem system");
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: " + elevDat.getStatus() + "\n");
	}

	public void processReceive() {
		// Process the received datagram.
		System.out.println("ElevatorSubsystem: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: " + scheDat.getStatus() + "\n");
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
	
	public ElevatorData getElevatorData() {
		return elevDat;
	}
	
	public SchedulerData getSchedulerData() {
		return scheDat;
	}
	
	public void addElevator(int elevatorNum) {
		elevatorList.add(new Elevator(elevatorNum, this));
	}
	
	public Elevator getElevator(int elevatorNum) {
		return elevatorList.get(elevatorNum - 1);
	}

	public static void main(String args[]) {
		ElevatorSubsystem c = new ElevatorSubsystem();
		
		c.addElevator(1);
		c.addElevator(2);
	
		c.receiveAndReply();
	}
}
