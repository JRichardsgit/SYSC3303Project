
/**
 * The Implementation of the FloorSubsystem Class
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class FloorSubsystem extends Thread {
	
	//Run Modes
	public final static int TEST_MODE = 2;
	public final static int TIMING_MODE = 1;
	public final static int DEFAULT_MODE = 0;
	
	private int runMode;

	//Sockets and Packets
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;

	//IP address
	InetAddress address;
	
	//Timer
	private Timer floorButtonsTimer;
	
	//Data Structures for relaying data
	private FloorData floorDat;
	private SchedulerData scheDat;
	
	private FloorCommunicator communicator;

	// GUI
	private JTextArea floorSystemLog;

	//List of floors
	private Floor floors[];

	private FloorParser parser;

	/**
	 * Create a new floor subsystem
	 * @param numFloors number of floors
	 */
	public FloorSubsystem(int numFloors, int runMode) {
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
		
		this.runMode = runMode;

		for (int i = 0; i < numFloors; i ++) {
			floors[i] = new Floor(i + 1, this);
		}
		
		communicator = new FloorCommunicator(this);
		communicator.start();
		
		if (runMode == TIMING_MODE) {
			floorButtonsTimer = new Timer("floor_buttons.txt");
			floorButtonsTimer.start();
		}
		
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (runMode == DEFAULT_MODE || runMode == TIMING_MODE) {
			createAndShowGUI();
			requestAddress();
			parser = new FloorParser(this, numFloors, selectFile());
			parser.start();
		}
	}
	
	public String selectFile() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("Assets\\Request Files"));
		fc.setLocation(100 + (425 * 3), 350);

        int returnVal = fc.showDialog(floorSystemLog, "Select File");
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getName();
        } 
        return "default_requests.txt";
	}
	
	public void requestAddress() {
		String[] options = {"Same Computer as Scheduler", "Separate Computer"};
		int popUp = JOptionPane.showOptionDialog(null, "Select Floor Subsystem Run Configuration", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
		switch(popUp) {
		case -1:
			System.exit(0);
		case 0:
			try {
				address = InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case 1:
			try {
				address = InetAddress.getByName(JOptionPane.showInputDialog("Enter the IP address of the scheduler:"));
			} catch (HeadlessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void createAndShowGUI() {

		//Create the Text Area
		floorSystemLog = new JTextArea();
		floorSystemLog.setFont(new Font("Arial", Font.ROMAN_BASELINE, 14));
		floorSystemLog.setLineWrap(true);
		floorSystemLog.setWrapStyleWord(true);
		JScrollPane areaScrollPane = new JScrollPane(floorSystemLog);
		areaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(800, 500));
		areaScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createEmptyBorder(),
								BorderFactory.createEmptyBorder(5,5,5,5)),
						areaScrollPane.getBorder()));

		DefaultCaret caret = (DefaultCaret) floorSystemLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel schedulerPanel = new JPanel(new BorderLayout());
		schedulerPanel.add(areaScrollPane, BorderLayout.CENTER);

		//Create and set up the window.
		JFrame frame = new JFrame("Floor Subsystem Log");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		Container newContentPane = schedulerPanel;
		frame.setContentPane(newContentPane);
		frame.setPreferredSize(new Dimension(500, 300));
		frame.setLocation(100 + (425 * 3), 650);
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Process the scheduler packet
	 * @param sfdata
	 */
	public void processPacket(SchedulerFloorData sfdata) {
		int mode = sfdata.getMode();
		switch(mode) {
		case SchedulerFloorData.CONFIRM_MESSAGE:
			//End the time measurement
			if (runMode == TIMING_MODE)
				if (floorButtonsTimer.isTiming()) {
					floorButtonsTimer.endTime();
				}
			break;
		case SchedulerFloorData.UPDATE_MESSAGE:
			break;
		}
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
		
		if (runMode == TIMING_MODE)
			floorButtonsTimer.startTime();
		
		communicator.send(floor.getFloorData());
	}

	/**
	 * Go down from the specified floor
	 * @param floorNum
	 */
	public void goDown(int currFloor, int destFloor) {
		Floor floor = getFloor(currFloor);
		floor.pressDown();
		floor.setDestination(destFloor);

		if (runMode == TIMING_MODE)
			floorButtonsTimer.startTime();
		
		communicator.send(floor.getFloorData());
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
	
	public void closeSockets() {
		communicator.closeSockets();
	}

	/**
	 * Prints the message on the console
	 * @param message
	 */
	public void print(String message) {
		if (runMode == DEFAULT_MODE || runMode == TIMING_MODE)
			floorSystemLog.append(" " + message + "\n");
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
		int numFloors = 0;
		String[] options = {"Use Defaults", "Use User Inputs"};
		int popUp = JOptionPane.showOptionDialog(null, "Enter Set Up Values For Floor Subsystem", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
		switch(popUp) {
		case -1:
			System.exit(0);
		case 0:
			numFloors = 22; //default floors
			break;
		case 1:
			numFloors = Integer.parseInt(JOptionPane.showInputDialog("How many floors?"));
		}
		
		FloorSubsystem c = new FloorSubsystem(numFloors, FloorSubsystem.DEFAULT_MODE);
	}
}
