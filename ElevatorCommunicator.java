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

public class ElevatorCommunicator extends Thread {
	// Socket and Packet
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;

	private Elevator elevator;
	private SchedulerData scheDat;

	// Scheduler address for sending packets
	private InetAddress schedulerAddress;

	public ElevatorCommunicator(int port, Elevator e) {
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

		elevator = e;
	}

	/**
	 * Set the scheduler address for sending packets
	 * @param address
	 */
	public void setSchedulerAddress(InetAddress address) {
		schedulerAddress = address;
	}

	/**
	 * Send a packet to the scheduler
	 */
	public void send() {

		ElevatorData elevDat = elevator.getElevatorData();

		try {
			// Convert the ElevatorData object into a byte array
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(elevDat);
			ooStream.flush();

			byte msg[] = baoStream.toByteArray();
			sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 3000);

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

		elevator.print("Sent packet to scheduler.\n Containing:\n	" + elevDat.getStatus() + "\n");
	}

	/**
	 * Receive a packet from the scheduler
	 */
	public void receive() {
		// Construct a DatagramPacket for receiving packets up
		// to 5000 bytes long (the length of the byte array).

		if (elevator.getElevatorData().isOperational()) {

			byte data[] = new byte[5000];
			receivePacket = new DatagramPacket(data, data.length);
			// Block until a datagram packet is received from receiveSocket.
			try {
				elevator.print("Waiting for packet...");
				receiveSocket.receive(receivePacket);
				schedulerAddress = receivePacket.getAddress();

			} catch (IOException e) {
				elevator.print("IO Exception: likely:");
				elevator.print("Receive Socket Timed Out.\n" + e);
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

			elevator.processPacket(scheDat);
		}	
		
		else {
			closeSockets();
		}
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
	 * Close sockets
	 */
	public void closeSockets() {
		receiveSocket.close();
		sendSocket.close();
	}

	public void run() {
		while (true) {
			receive();
			wait(2000);
		}
	}



}
