
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

	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;

	private FloorData floorDat;
	private SchedulerData scheDat;
	
	private Floor floors[];

	private String message;

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

	public void send() {
		// Prepare a DatagramPacket and send it via sendReceiveSocket
		// to port 4000 on the destination host.

		Date date = new Date();
		String strDateFormat = "hh:mm:ss a";
		DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
		String formattedDate = dateFormat.format(date);

		String s = "Scheduler i need an elevatorSubsystem. The floorSubsystem number is 4, button pressed is up, elevatorSubsystem number is 1, and the time is "
				+ formattedDate;

		try {
			floorDat = new FloorData(1, true);
			floorDat.setStatus("Requesting ElevatorSubsystem.");

			// Convert the FloorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(floorDat);
			ooStream.flush();
			byte msg[] = baoStream.toByteArray();

			// Construct a datagram packet that is to be sent to a specified port
			sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 4000);

			// Send the datagram packet to the server via the send/receive socket.
			sendReceiveSocket.send(sendPacket);

			processSend();

			System.out.println("FloorSubsystem: Packet sent to scheduler.\n");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}

	}

	public void receive() {

		// Construct a DatagramPacket for receiving packets up
		// to 100 bytes long (the length of the byte array).

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

		processReceived();

		System.out.println("FloorSubsystem: Packet received from scheduler.\n");

		// We're finished, so close the socket.
		sendReceiveSocket.close();

	}

	public void processReceived() {
		// Process the received datagram.
		System.out.println("FloorSubsystem: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: " + scheDat.getStatus() + "\n");

	}

	public void processSend() {
		// Process Sent Datagram
		System.out.println("FloorSubsystem: Sending packet to scheduler:");
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: " + floorDat.getStatus() + "\n");
 
	}
	
	public FloorData getFloorData() {
		return floorDat;
	}
	
	public SchedulerData getSchedulerData() {
		return scheDat;
	}

	public static void main(String args[]) {
		
		//Create a floor subsystem with 5 floors
		FloorSubsystem c = new FloorSubsystem(5);
		
		c.send();
		c.receive();

	}
}
