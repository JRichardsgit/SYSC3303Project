
/**
 * The Implementation of the FloorSubsystem Class
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class FloorSubsystem extends Thread {

	//Sockets and Packets
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;

	//IP address
	InetAddress address;
	
	private long floorButtons_start;
	private long floorButtons_end;
	private boolean measure_floorButtons = false;
	
	private File file;
	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;
	private ArrayList<String> measurements;

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
		
		communicator = new FloorCommunicator(this);
		communicator.start();
		measurements = new ArrayList<String>();
		
		try {

			//address = InetAddress.getByName("192.168.43.197");
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		parser = new FloorParser(this, numFloors, selectFile());
		parser.start();
		
		createAndShowGUI();
	}
	
	public String selectFile() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("Assets\\Request Files"));
        int returnVal = fc.showDialog(null, "Select an input file");
     
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getName();
        } 
        return "default_requests.txt";
	}

	public void createAndShowGUI() {

		//Create the Text Area
		floorSystemLog = new JTextArea();
		floorSystemLog.setFont(new Font("Arial", Font.ROMAN_BASELINE, 20));
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
		frame.setPreferredSize(new Dimension(800, 500));
		frame.setLocation(100, 550);
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public void processPacket(SchedulerFloorData sfdata) {
		int mode = sfdata.getMode();
		switch(mode) {
		case SchedulerFloorData.CONFIRM_MESSAGE:
			if (measure_floorButtons) {
				measure_floorButtons = false;
				floorButtons_end = System.currentTimeMillis();
				addMeasurement("" + (floorButtons_end - floorButtons_start));
			}
			break;
		case SchedulerFloorData.UPDATE_MESSAGE:
			break;
		}
	}
	
	public void addMeasurement(String measurement) {
		measurements.add(measurement);
	}
	
	public void saveToFile() throws IOException {
		File file = new File("Assets\\Measurements\\floor_buttons.txt");
		if (file.exists()) {
			file.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(file, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (String measurement: measurements) {
			bufferedWriter.write(measurement + " ms\n");
		}
		bufferedWriter.close();
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
		
		measure_floorButtons = true;
		floorButtons_start = System.currentTimeMillis();
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

		measure_floorButtons = true;
		floorButtons_start = System.currentTimeMillis();
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

	/**
	 * Prints the message on the console
	 * @param message
	 */
	public void print(String message) {
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

		//Create a floor subsystem with 5 floors
		FloorSubsystem c = new FloorSubsystem(22);
		
		while(true) {
			c.wait(100);
		}
	}
}
