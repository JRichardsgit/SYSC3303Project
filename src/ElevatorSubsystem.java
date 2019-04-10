
/**
 * This implementation of the ElevatorSubsystem Class
 * Receives packets from the Scheduler and forwards/routes them to the corresponding elevator
 * RECEIVES ONLY, sending is handled by each respective elevator
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class ElevatorSubsystem extends Thread {
	
	//Run Modes
	public final static int TEST_MODE = 2;
	public final static int TIMING_MODE = 1;
	public final static int DEFAULT_MODE = 0;
		
	private int runMode;

	//Reference to the GUI
	private GUI elevatorGUI;
	
	//List of elevators
	private Elevator elevatorList[];
	
	//Error List
	private ArrayList<ErrorEvent> errorList;
	
	/**
	 * Create a new elevator subsystem with numElevators and numFloors
	 * @param numElevators the number of elevators in the system
	 */
	public ElevatorSubsystem(int numFloors, int numElevators, int runMode) {
		elevatorList = new Elevator[numElevators];
		//elevatorPending = new boolean[numElevators];
		errorList = new ArrayList<ErrorEvent>();
	
		elevatorGUI = new GUI(numFloors, numElevators);
		
		//Initialize the elevators
		for (int i = 0; i < numElevators; i ++) {
			elevatorList[i] = (new Elevator(i, numFloors, this, 2000 + i, elevatorGUI, runMode));
			//elevatorPending[i] = false;
		}
		
		loadErrors();
		
		for (Elevator e: elevatorList) {
			e.start();
		}
	}
	
	/**
	 * Create a new elevator subsystem with elevators and floors to be determined
	 */
	public ElevatorSubsystem(boolean measureValues) {
		//Initialize the GUI and ask user for elevators and floors
		elevatorGUI = new GUI();
		
		elevatorList = new Elevator[elevatorGUI.getNumElevators()];
		//elevatorPending = new boolean[numElevators];
		errorList = new ArrayList<ErrorEvent>();
		
		//Initialize the elevators
		for (int i = 0; i < elevatorGUI.getNumElevators(); i ++) {
			elevatorList[i] = (new Elevator(i, elevatorGUI.getNumFloors(), this, 2000 + i, elevatorGUI, runMode));
			//elevatorPending[i] = false;
		}
		
		loadErrors();
		
		for (Elevator e: elevatorList) {
			e.start();
		}
	}

	/**
	 * Returns the elevator with the corresponding elevator number
	 * @param elevatorNum the elevator number
	 * @return corresponding elevator
	 */
	public Elevator getElevator(int elevatorNum) {
		return elevatorList[elevatorNum];
	}
	
	public void loadErrors() {
		//Hard Coded error events
		if (elevatorList.length >= 4) {
			elevatorList[0].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
			elevatorList[1].addError(new ErrorEvent(ErrorEvent.ELEVATOR_STUCK, 100000));
			elevatorList[2].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
			elevatorList[3].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
		}
	}
	
	/**
	 * Close the sockets of all elevators
	 */
	public void closeSockets() {
		// We're finished, so close the sockets.
		for (Elevator e: elevatorList) {
			e.closeSockets();
		}
		System.exit(0);
	}
	
	/**
	 * Print a status message in the console
	 * @param message the message to be printed
	 */
	public void print(String message) {
		System.out.println("ELEVATOR SUBSYSTEM: " + message);
	}
	
	/**
	 * Wait for the specified amount of time
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
		int numFloors = 0, numElevators = 0;
		String[] options = {"Use Defaults", "Use User Inputs"};
		int popUp = JOptionPane.showOptionDialog(null, "Enter Set Up Values For Elevator Subsystem", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
		switch(popUp) {
		case -1:
			System.exit(0);
		case 0:
			numFloors = 22; //default floors
			numElevators = 4; //default elevators
			break;
		case 1:
			numElevators = Integer.parseInt(JOptionPane.showInputDialog("How many elevators?"));
			numFloors = Integer.parseInt(JOptionPane.showInputDialog("How many floors?"));
		}
		
		ElevatorSubsystem c = new ElevatorSubsystem(numFloors, numElevators, DEFAULT_MODE);
	}
}
