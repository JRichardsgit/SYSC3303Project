
/**
 * This implementation of the ElevatorSubsystem Class
 * Receives packets from the Scheduler and forwards/routes them to the corresponding elevator
 * RECEIVES ONLY, sending is handled by each respective elevator
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ElevatorSubsystem extends Thread {

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
	public ElevatorSubsystem(int numFloors, int numElevators, boolean measureValues) {
		elevatorList = new Elevator[numElevators];
		//elevatorPending = new boolean[numElevators];
		errorList = new ArrayList<ErrorEvent>();
		
		elevatorGUI = new GUI(numFloors, numElevators);
		//Initialize the elevators
		for (int i = 0; i < numElevators; i ++) {
			elevatorList[i] = (new Elevator(i, numFloors, this, 2000 + i, elevatorGUI, measureValues));
			//elevatorPending[i] = false;
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
			elevatorList[i] = (new Elevator(i, elevatorGUI.getNumFloors(), this, 2000 + i, elevatorGUI, measureValues));
			//elevatorPending[i] = false;
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
		elevatorList[0].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
		elevatorList[1].addError(new ErrorEvent(ErrorEvent.ELEVATOR_STUCK, 100000));
		elevatorList[2].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
		elevatorList[3].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
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
	
	public void run() {
		/**
		 * Elevator subsystem logic
		 */
		loadErrors();
		
		for (Elevator e: elevatorList) {
			e.start();
		}
	}
	
	

	public static void main(String args[]) {
		//Initialize a system with 5 floors and 2 elevators
		ElevatorSubsystem c = new ElevatorSubsystem(22, 4, true);
		c.start();
	}
}
