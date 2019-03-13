import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/**
 * Elevator Node Implementation
 */
public class Elevator extends Thread {

	// Scheduler Packet Data
	private SchedulerData scheDat;

	// Elevator Number
	private final int elevatorNum;

	// Number of floors
	private final int numFloors;

	// Motor flags
	private boolean movingUp;
	private boolean movingDown;

	// Door flag
	private boolean doorOpen;

	// Shut down flag, if error occurs
	private boolean shutdown;
	// Door stuck flag
	private boolean doorStuck;
	// Elevator Ready flag
	private boolean actionReady;

	// Reference to Elevator Subsystem
	private ElevatorSubsystem eSystem;

	// Reference to the communicator
	private ElevatorCommunicator communicator;

	// Floor Information
	private ArrayList<Integer> reqFloors;
	private ArrayList<Integer> destFloors[];
	private int currFloor;

	// Reply required from scheduler
	private boolean replyRequired;

	// Error List
	private ArrayList<ErrorEvent> errorList;

	// FloorTimers
	private final long initializedTime;
	private long startTime, endTime, elapsedTime;
	private long expectedTime;

	// GUI
	private JTextArea elevatorLog;

	/**
	 * Creates a new elevator node
	 * 
	 * @param elevatorNum
	 *            this elevator's number
	 * @param eSystem
	 *            reference to the elevator subsystem
	 */
	public Elevator(int elevatorNum, int numFloors, ElevatorSubsystem eSystem, int port) {


		this.elevatorNum = elevatorNum;
		this.numFloors = numFloors;
		this.eSystem = eSystem;
		movingUp = false;
		movingDown = false;
		doorOpen = false;
		currFloor = 1;
		shutdown = false;
		doorStuck = false;
		reqFloors = new ArrayList<Integer>();
		destFloors = new ArrayList[numFloors];
		errorList = new ArrayList<ErrorEvent>();
		initializedTime = System.nanoTime();
		expectedTime = 10000;

		createAndShowGUI();

		communicator = new ElevatorCommunicator(port, this);
		communicator.start();

		for (int i = 0; i < numFloors; i ++) {
			destFloors[i] = new ArrayList<Integer>();
		}

	}

	public void createAndShowGUI() {

		//Create the Text Area
		elevatorLog = new JTextArea();
		elevatorLog.setFont(new Font("Arial", Font.ROMAN_BASELINE, 20));
		elevatorLog.setLineWrap(true);
		elevatorLog.setWrapStyleWord(true);
		JScrollPane areaScrollPane = new JScrollPane(elevatorLog);
		areaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(800, 500));
		areaScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createEmptyBorder(),
								BorderFactory.createEmptyBorder(5,5,5,5)),
						areaScrollPane.getBorder()));

		DefaultCaret caret = (DefaultCaret) elevatorLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel schedulerPanel = new JPanel(new BorderLayout());
		schedulerPanel.add(areaScrollPane, BorderLayout.CENTER);

		//Create and set up the window.
		JFrame frame = new JFrame("Elevator " + elevatorNum + " Log");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		Container newContentPane = schedulerPanel;

		frame.setContentPane(newContentPane);
		frame.setPreferredSize(new Dimension(800, 500));
		frame.setLocation(900, 50 + (500 * elevatorNum));
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}


	/**
	 * Add an error event to the errorList
	 * @param err the error event
	 */
	public void addError(ErrorEvent err) {
		errorList.add(err);
	}

	/**
	 * Returns true if the elevator is shut down, no longer operational
	 * @return true if the elevator is shut down, no longer operational
	 */
	public boolean isShutDown() {
		return shutdown;
	}


	/**
	 * Process the received scheduler packet
	 */
	public void processPacket(SchedulerData s) {
		scheDat = s;
		int mode = s.getMode();
		actionReady = false;

		switch (mode) {
		case SchedulerData.CONTINUE_REQUEST:
			print("Received CONTINUE request.");
			//Elevator's motor flags and door flags remain unchanged
			break;

		case SchedulerData.FLOOR_REQUEST:
			print("Received FLOOR request.");
			int floor = s.getReqFloor();

			//Add the requested floor to the list
			if (!reqFloors.contains(floor)) {
				reqFloors.add(floor);
				Collections.sort(reqFloors);
				if (movingDown)
					Collections.reverse(reqFloors);
			}


			//Update the destination floors for the requested floor
			//(floor requests that will be added when the elevator reaches that floor)
			destFloors[floor - 1].add(s.getDestFloor());

			print("Current requests: " + reqFloors.toString());
			break;

		case SchedulerData.MOVE_REQUEST:
			print("Received MOVE request.");
			if (doorOpen) //If door open, close the door before moving
				closeDoor();

			if (s.moveUp()) { //If request was to move up
				moveUp();
			} else {
				moveDown(); //If request was to move down
			} 
			break;

		case SchedulerData.STOP_REQUEST:
			print("Received STOP request.");
			//Stop the motor and open the door
			stopMotor();
			openDoor();

			print("Arrived at floor " + currFloor + ".\n");

			if (!reqFloors.isEmpty()) {
				//Remove the floor that we arrived at from the requested floors
				reqFloors.remove(new Integer(currFloor));

				//Add all the floor destinations from people who just boarded to the requested floors
				reqFloors.removeAll(destFloors[currFloor - 1]);
				reqFloors.addAll(destFloors[currFloor - 1]);

				//Clear the destination floors from that floor
				destFloors[currFloor - 1].clear();
			}
			replyRequired = true;
			communicator.send();
			break;
		case SchedulerData.DOOR_REQUEST:
			print("Received DOOR request.");
			//Will proceed to opening/closing it's doors
			break;
		}

		//Update the scheduler with current status if it changed
		if (mode != SchedulerData.CONTINUE_REQUEST && mode != SchedulerData.DOOR_REQUEST) {
			replyRequired = false;
			communicator.send();
		}

		actionReady = true;
	}

	/**
	 * Wait until the scheduler sends further instruction (not a floor request)
	 */
	public void waitForInstruction() {
		print("Awaiting Instruction.\n");

		do {
			wait(2000);
		} while (scheDat.getMode() == SchedulerData.FLOOR_REQUEST && !actionReady);
	}

	/**
	 * Operates on a one floor basis determined by the set motor flags, if elevator is idle, does nothing
	 */
	public void moveOneFloor() {
		//If door is open, wait until closed before moving
		while (doorOpen && !isIdle()) {
			wait(1000);
		}

		//If elevator is moving
		if (!isIdle()) {
			if (!errorList.isEmpty()) {
				ErrorEvent e = errorList.get(0);
				//If the elevator is stuck, shut it down
				if (e.getType() == ErrorEvent.ELEVATOR_STUCK) {
					if (movingUp) {
						print("Stuck between floors " + currFloor + " and " + (currFloor + 1) + ".");
					} else {
						print("Stuck between floors " + currFloor + " and " + (currFloor - 1) + ".");
					}
					shutdown = true;
					communicator.send();
					print("SHUTTING DOWN...");
				}
			}

			//If it is working
			if (!shutdown) {
				if (movingUp) {
					currFloor++;
					if (currFloor > numFloors) {
						currFloor = numFloors;
					}
					print("Currently on floor " + currFloor + ", moving up.");
				} else if (movingDown) {
					currFloor--;
					if (currFloor <= 0) {
						currFloor = 1;
					}
					print("Currently on floor " + currFloor + ", moving down.");
				} 
				wait(1000);
			}
		} 
	}

	/**
	 * Set flags for motor moving the elevator up
	 */
	public void moveUp() {
		print("Now moving up.");
		movingUp = true;
		movingDown = false;
	}
	/**
	 * Set flags for motor moving the elevator down
	 */
	public void moveDown() {
		print("Now moving down.");
		movingUp = false;
		movingDown = true;
	}

	/**
	 * Set flags for idle motor
	 */
	public void stopMotor() {
		print("Now stopping.");
		movingUp = false;
		movingDown = false;
	}

	/**
	 * Returns true if the motor idle
	 */
	public boolean isIdle() {
		return (!movingUp && !movingDown);
	}

	/**
	 * Set the flag for opening the elevator doors
	 */
	public void openDoor() {
		print("Opening doors.");
		actionReady = false;

		if (!errorList.isEmpty()) {
			ErrorEvent e = errorList.get(0);

			if (e.getType() == ErrorEvent.DOOR_STUCK) {
				doorStuck = true;
				wait(2000);
				print("Doors STUCK.");

				replyRequired = true;
				communicator.send();

				do {
					communicator.receive();
					wait(2000);
				} while (scheDat.getMode() != SchedulerData.DOOR_REQUEST);

				errorList.remove(0);
			}
		}

		wait(2000);
		print("Doors opened.");
		doorOpen = true;
		doorStuck = false;
		actionReady = true;
	}

	/**
	 * Set the flag for closing the elevator doors
	 */
	public void closeDoor() {
		print("Closing doors.");
		actionReady = false;

		if (!errorList.isEmpty()) {
			ErrorEvent e = errorList.get(0);
			if (e.getType() == ErrorEvent.DOOR_STUCK) {
				doorStuck = true;
				wait(2000);
				print("Doors STUCK.");

				replyRequired = true;
				communicator.send();
				communicator.receive();

				do {
					wait(2000);
				} while (scheDat.getMode() != SchedulerData.DOOR_REQUEST);

				errorList.remove(0);
			}
		}

		wait(2000);
		print("Doors closed.");
		doorOpen = false;
		doorStuck = false;
		actionReady = true;
	}

	/**
	 * Return this elevator's data
	 * @return this elevator's data
	 */
	public ElevatorData getElevatorData() {
		int errType;

		if (doorStuck) {
			errType = ElevatorData.DOOR_STUCK_ERROR;
		}
		else if (shutdown) {
			errType = ElevatorData.ELEVATOR_STUCK_ERROR;
		}
		else {
			errType = ElevatorData.NO_ERROR;
		}

		return new ElevatorData(elevatorNum, errType, currFloor, 
				reqFloors, movingUp, movingDown, doorOpen, shutdown, replyRequired);
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
	 * Print a status message in the console
	 * @param message the message to be printed
	 */
	public void print(String message) {
		elevatorLog.append(" Elevator " + elevatorNum + ": " + message + "\n");
	}

	@Override
	public void run() {

		print("Started.");

		while (true) {
			//If there are requested floors, and the elevator is ready to move
			//(Doors are closed/no pending requests)
			if (!reqFloors.isEmpty() && !shutdown && !doorStuck && actionReady) {
				startTime = System.currentTimeMillis();
				moveOneFloor();
				//Measure the time it took for the elevator to receive the instruction
				endTime = System.currentTimeMillis();
				elapsedTime = endTime - startTime;

				//print("Time between floors: " + elapsedTime + " ms.");

				//If the current time for instruction receiving exceeds the expected, 
				//trigger elevator stuck event
				if (elapsedTime > expectedTime) {
					errorList.add(0, new ErrorEvent(ErrorEvent.ELEVATOR_STUCK, 0));
				}

				if (!shutdown) {
					//Update the scheduler about current status
					replyRequired = true;
					communicator.send();

					//Pending for reply from Scheduler
					waitForInstruction();
				}
			}
			//Slow down a bit
			wait(1000);
		}
	}

}
