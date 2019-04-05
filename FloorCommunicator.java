import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class FloorCommunicator extends Thread {
	// Socket and Packet
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;

	private SchedulerFloorData sfdata;
	private FloorSubsystem system;

	// Scheduler address for sending packets
	private InetAddress schedulerAddress;

	public FloorCommunicator(FloorSubsystem system) {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		try {
			//schedulerAddress = InetAddress.getByName("192.168.43.69");
			schedulerAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.system = system;
	}

	/**
	 * Send a packet to the scheduler
	 */
	public void send(FloorData floorDat) {
		try {
			// Convert the ElevatorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(floorDat);
			ooStream.flush();

			byte msg[] = baoStream.toByteArray();

			sendPacket = new DatagramPacket(msg, msg.length, schedulerAddress, 3000);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Send the datagram packet to the client via the send socket.
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		system.print("Sending to address: " + schedulerAddress);
		system.print("Sent packet to scheduler.\n Containing:\n	" + floorDat.getStatus() + "\n");
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
			// elevator.print("Waiting for packet...");
			sendReceiveSocket.receive(receivePacket);
			schedulerAddress = receivePacket.getAddress();

		} catch (IOException e) {
			system.print("IO Exception: likely:");
			system.print("Receive Socket Timed Out.\n" + e);
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

			sfdata = (SchedulerFloorData) o;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		system.print("Received packet from address: " + schedulerAddress);
		system.processPacket(sfdata);

	}

	/**
	 * Simulate waiting time for elevator actions, and for delays
	 * 
	 * @param ms
	 *            the time to wait, in milliseconds
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
	 * Close sockets
	 */
	public void closeSockets() {
		sendReceiveSocket.close();
	}

	public void run() {
		while (true) {
			receive();
			wait(100);
		}
	}

}
