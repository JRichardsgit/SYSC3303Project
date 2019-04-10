
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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

	// GUI reference
	private GUI elevatorGUI;
	
	// Scheduler Packet Data
	private SchedulerData scheDat;

	// Elevator Number
	private final int elevatorNum;

	// Number of floors
	private final int numFloors;

	// Motor flags
	private boolean movingUp;
	private boolean movingDown;
	private int currDirection;

	// CONSTANTS FOR CURRENT DIRECTION
	public final int UP = 0;
	public final int DOWN = 1;
	public final int IDLE = 2;

	// Door flag
	private boolean doorOpen;

	// Shut down flag, if error occurs
	private boolean shutdown;
	// Door stuck flag
	private boolean doorStuck;
	// Elevator Ready flag
	private boolean actionReady;

	// Timers
	private Timer arrivalSensorsTimer;
	private Timer elevatorButtonsTimer;

	// Reference to Elevator Subsystem
	private ElevatorSubsystem eSystem;

	// Reference to the communicator
	private ElevatorCommunicator communicator;

	// Floor Information
	private ArrayList<Integer> reqFloors;
	private ArrayList<Integer> subReqFloors;
	private ArrayList<Integer> destFloors[];
	private int currFloor;

	// Reply required from scheduler
	private boolean replyRequired;

	// Error List
	private ArrayList<ErrorEvent> errorList;

	// Time check
	private long initializedTime;

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
	public Elevator(int elevatorNum, int numFloors, ElevatorSubsystem eSystem, int port, GUI elevatorGUI, boolean measureValues) {
		this.elevatorGUI = elevatorGUI;
		this.elevatorNum = elevatorNum;
		this.numFloors = numFloors;
		this.eSystem = eSystem;
		movingUp = false;
		movingDown = false;
		currDirection = IDLE;
		doorOpen = false;
		currFloor = 1;
		shutdown = false;
		doorStuck = false;
		reqFloors = new ArrayList<Integer>();
		subReqFloors = new ArrayList<Integer>();
		destFloors = new ArrayList[numFloors];
		errorList = new ArrayList<ErrorEvent>();
		initializedTime = System.currentTimeMillis();
		actionReady = false;

		createAndShowGUI();

		communicator = new ElevatorCommunicator(port, this);
		communicator.start();

		if (measureValues) {
			arrivalSensorsTimer = new Timer("arrival_sensors.txt");
			elevatorButtonsTimer = new Timer("elevator_buttons.txt");
	
			arrivalSensorsTimer.start();
			elevatorButtonsTimer.start();
		}

		for (int i = 0; i < numFloors; i++) {
			destFloors[i] = new ArrayList<Integer>();
		}

	}

	public void createAndShowGUI() {

		// Create the Text Area
		elevatorLog = new JTextArea();
		elevatorLog.setFont(new Font("Arial", Font.ROMAN_BASELINE, 14));
		elevatorLog.setLineWrap(true);
		elevatorLog.setWrapStyleWord(true);
		JScrollPane areaScrollPane = new JScrollPane(elevatorLog);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(800, 500));
		areaScrollPane.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)), areaScrollPane.getBorder()));

		DefaultCaret caret = (DefaultCaret) elevatorLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel schedulerPanel = new JPanel(new BorderLayout());
		schedulerPanel.add(areaScrollPane, BorderLayout.CENTER);

		// Create and set up the window.
		JFrame frame = new JFrame("Elevator " + elevatorNum + " Log");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		Container newContentPane = schedulerPanel;

		frame.setContentPane(newContentPane);
		frame.setPreferredSize(new Dimension(500, 300));
		frame.setLocation(100 + (425 * 3), 50);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Add an error event to the errorList
	 * 
	 * @param err
	 *            the error event
	 */
	public void addError(ErrorEvent err) {
		errorList.add(err);
	}

	/**
	 * Returns true if the elevator is shut down, no longer operational
	 * 
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

		switch (mode) {
		case SchedulerData.CONTINUE_REQUEST:
			if (arrivalSensorsTimer != null)
				if (arrivalSensorsTimer.isTiming()) {
					arrivalSensorsTimer.endTime();
				}
			print("Received CONTINUE request.");
			actionReady = true;
			// Elevator's motor flags and door flags remain unchanged
			break;

		case SchedulerData.FLOOR_REQUEST:
			print("Received FLOOR request.");
			int floor = s.getReqFloor();

			// Add the requested floor to the list
			if (!reqFloors.contains(floor)) {
				switch (currDirection) {
				case UP:
					if (currFloor > floor) {
						subReqFloors.add(floor);
					} else if (currFloor < floor) {
						reqFloors.add(floor);
						Collections.sort(reqFloors);
						Collections.sort(subReqFloors);
					}
					break;
				case DOWN:
					if (currFloor < floor) {
						subReqFloors.add(floor);
					} else if (currFloor > floor) {
						reqFloors.add(floor);
						Collections.sort(reqFloors);
						Collections.reverse(reqFloors);
						Collections.sort(subReqFloors);
						Collections.reverse(subReqFloors);
					}
					break;
				case IDLE:
					reqFloors.add(floor);
					break;
				}

				if (!subReqFloors.isEmpty()) {
					switch (currDirection) {
					case UP:
						if (currFloor < subReqFloors.get(0)) {
							reqFloors.addAll(subReqFloors);
							Collections.sort(reqFloors);
							subReqFloors.clear();
						}
						break;
					case DOWN:
						if (currFloor > subReqFloors.get(0)) {
							reqFloors.addAll(subReqFloors);
							Collections.sort(reqFloors);
							Collections.reverse(subReqFloors);
							subReqFloors.clear();
						}
						break;
					case IDLE:
						reqFloors.addAll(subReqFloors);
						subReqFloors.clear();
						break;
					}
				}

			}

			// Update the destination floors for the requested floor
			// (floor requests that will be added when the elevator reaches that floor)
			destFloors[floor - 1].add(s.getDestFloor());

			print("Current requests: " + reqFloors.toString());
			ArrayList<Integer> allRequests = new ArrayList<Integer>();
			allRequests.addAll(reqFloors);
			allRequests.addAll(subReqFloors);

			elevatorGUI.setRequestsInfo(elevatorNum, allRequests);
			break;

		case SchedulerData.MOVE_REQUEST:
			print("Received MOVE request.");
			if (elevatorButtonsTimer != null)
				if (elevatorButtonsTimer.isTiming()) {
					elevatorButtonsTimer.endTime();
				}
			if (doorOpen) // If door open, close the door before moving
				closeDoor();

			if (s.moveUp()) { // If request was to move up
				moveUp();
				Collections.sort(reqFloors);
			} else {
				moveDown(); // If request was to move down
				Collections.sort(reqFloors);
				Collections.reverse(reqFloors);
			}
			actionReady = true;
			break;

		case SchedulerData.STOP_REQUEST:
			if (arrivalSensorsTimer != null)
				if (arrivalSensorsTimer.isTiming()) {
					arrivalSensorsTimer.endTime();
				}
			print("Received STOP request.");
			// Stop the motor and open the door
			stopMotor();
			openDoor();

			print("Arrived at floor " + currFloor + ".\n");

			if (!reqFloors.isEmpty()) {
				// Remove the floor that we arrived at from the requested floors
				if (reqFloors.contains(currFloor))
					reqFloors.remove(new Integer(currFloor));

				if (reqFloors.isEmpty()) {
					currDirection = IDLE;
					elevatorGUI.setDirectionInfo(elevatorNum, "IDLE");
				} else {
					if (currDirection == UP) {
						elevatorGUI.setDirectionInfo(elevatorNum, "UP");
					} else {
						elevatorGUI.setDirectionInfo(elevatorNum, "DOWN");
					}
				}

				// Add all the floor destinations from people who just boarded to the requested
				// floors
				if (!destFloors[currFloor - 1].isEmpty()) {
					switch (currDirection) {
					case UP:
						if (currFloor < destFloors[currFloor - 1].get(0)) {
							reqFloors.removeAll(destFloors[currFloor - 1]);
							reqFloors.addAll(destFloors[currFloor - 1]);
							Collections.sort(reqFloors);
							destFloors[currFloor - 1].clear();
						}
						break;
					case DOWN:
						if (currFloor > destFloors[currFloor - 1].get(0)) {
							reqFloors.removeAll(destFloors[currFloor - 1]);
							reqFloors.addAll(destFloors[currFloor - 1]);
							Collections.sort(reqFloors);
							Collections.reverse(reqFloors);
							destFloors[currFloor - 1].clear();
						}
						break;
					case IDLE:
						reqFloors.addAll(destFloors[currFloor - 1]);
						Collections.sort(reqFloors);
						destFloors[currFloor - 1].clear();
						if (currFloor < reqFloors.get(0)) {
							currDirection = UP;
							elevatorGUI.setDirectionInfo(elevatorNum, "UP");
						} else {
							currDirection = DOWN;
							elevatorGUI.setDirectionInfo(elevatorNum, "DOWN");
						}
						
						break;
					}
					if (elevatorButtonsTimer != null)
						elevatorButtonsTimer.startTime();
				}

			}
			elevatorGUI.setRequestsInfo(elevatorNum, reqFloors);
			break;
		case SchedulerData.DOOR_REQUEST:
			print("Received DOOR request.");
			// Will proceed to opening/closing it's doors
			break;
		}

		// Update the scheduler with current status if it changed
		if (mode == SchedulerData.STOP_REQUEST) {
			replyRequired = true;
			communicator.send();
			actionReady = true;
			waitForInstruction();
		} else if (mode != SchedulerData.CONTINUE_REQUEST || mode != SchedulerData.DOOR_REQUEST) {
			replyRequired = false;
			communicator.send();
			actionReady = true;
		}
	}

	/**
	 * Wait until the scheduler sends further instruction (not a floor request)
	 */
	public void waitForInstruction() {
		print("Awaiting Instruction.\n");

		do {
			wait(50);
		} while (!actionReady);
	}

	/**
	 * Operates on a one floor basis determined by the set motor flags, if elevator
	 * is idle, does nothing
	 */
	public void moveOneFloor() {
		// If door is open, wait until closed before moving
		while (doorOpen && !isIdle()) {
			wait(50);
		}

		// If elevator is moving
		if (!isIdle()) {
			if (!errorList.isEmpty()) {
				long currentTime = System.currentTimeMillis() - initializedTime;
				Random rand = new Random();
				if ((currentTime / 1000) > (10 + rand.nextInt(10))) {
					ErrorEvent e = errorList.get(0);
					// If the elevator is stuck, shut it down
					if (e.getType() == ErrorEvent.ELEVATOR_STUCK) {
						if (movingUp) {
							print("Stuck between floors " + currFloor + " and " + (currFloor + 1) + ".");
						} else {
							print("Stuck between floors " + currFloor + " and " + (currFloor - 1) + ".");
						}
						shutdown = true;
						communicator.send();
						print("SHUTTING DOWN...");
						elevatorGUI.setShutdown(elevatorNum);
					}
				}
			}

			// If it is working
			if (!shutdown) {
				switch (currDirection) {
				case UP:
					if (currFloor != reqFloors.get(0)) {
						currFloor++;
					}
					if (currFloor > numFloors) {
						currFloor = numFloors;
					}
					print("Currently on floor " + currFloor + ", moving up.");
					elevatorGUI.setDirectionInfo(elevatorNum, "UP");
					break;
				case DOWN:
					if (currFloor != reqFloors.get(0)) {
						currFloor--;
					}
					if (currFloor <= 0) {
						currFloor = 1;
					}
					print("Currently on floor " + currFloor + ", moving down.");
					elevatorGUI.setDirectionInfo(elevatorNum, "DOWN");
					break;
				case IDLE:
					elevatorGUI.setDirectionInfo(elevatorNum, "IDLE");
					break;
				}
				elevatorGUI.setCurrentFloorInfo(elevatorNum, currFloor);
				elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.MOVING);
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
		currDirection = UP;
	}

	/**
	 * Set flags for motor moving the elevator down
	 */
	public void moveDown() {
		print("Now moving down.");
		movingUp = false;
		movingDown = true;
		currDirection = DOWN;
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
		if (currDirection == IDLE)
			return true;
		return false;
	}

	/**
	 * Set the flag for opening the elevator doors
	 */
	public void openDoor() {
		print("Opening doors.");

		actionReady = false;

		if (!errorList.isEmpty()) {

			long currentTime = System.currentTimeMillis() - initializedTime;
			Random rand = new Random();
			if ((currentTime / 1000) > (10 + rand.nextInt(10))) {
				ErrorEvent e = errorList.get(0);

				if (e.getType() == ErrorEvent.DOOR_STUCK) {
					doorStuck = true;
					wait(1000);
					print("Doors STUCK.");
					elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.STUCK);

					replyRequired = true;
					communicator.send();

					do {
						communicator.receive();
						wait(1000);
					} while (scheDat.getMode() != SchedulerData.DOOR_REQUEST);

					errorList.remove(0);
				}
			}
		}

		wait(1000);
		print("Doors opened.");
		elevatorGUI.setDoorsInfo(elevatorNum, GUI.OPEN);
		elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.OPEN);
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
			long currentTime = System.currentTimeMillis() - initializedTime;
			Random rand = new Random();
			if ((currentTime / 1000) > (10 + rand.nextInt(10))) { // After 10-20 s trigger error event
				ErrorEvent e = errorList.get(0);
				if (e.getType() == ErrorEvent.DOOR_STUCK) {
					doorStuck = true;
					wait(1000);
					print("Doors STUCK.");
					elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.STUCK);

					replyRequired = true;
					communicator.send();
					communicator.receive();

					do {
						wait(1000);
					} while (scheDat.getMode() != SchedulerData.DOOR_REQUEST);

					errorList.remove(0);
				}
			}
		}

		wait(1000);
		print("Doors closed.");
		elevatorGUI.setDoorsInfo(elevatorNum, GUI.CLOSED);
		elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.CLOSED);
		doorOpen = false;
		doorStuck = false;
		actionReady = true;
	}

	/**
	 * Return this elevator's data
	 * 
	 * @return this elevator's data
	 */
	public synchronized ElevatorData getElevatorData() {
		int errType;

		if (doorStuck) {
			errType = ElevatorData.DOOR_STUCK_ERROR;
		} else if (shutdown) {
			errType = ElevatorData.ELEVATOR_STUCK_ERROR;
		} else {
			errType = ElevatorData.NO_ERROR;
		}

		return new ElevatorData(elevatorNum, errType, currFloor, reqFloors, movingUp, movingDown, currDirection,
				doorOpen, shutdown, replyRequired);
	}
	
	/**
	 * Closes sockets
	 */
	public void closeSockets() {
		communicator.closeSockets();
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
	 * Print a status message in the console
	 * 
	 * @param message
	 *            the message to be printed
	 */
	public void print(String message) {
		elevatorLog.append(" Elevator " + elevatorNum + ": " + message + "\n");
	}
	
	/**
     * Tell this thread to wait
     */
    public synchronized void pause() {
    	try {
    		this.wait();
    	} catch (Exception e) {}
    }
    
    /**
     * Notify this thread
     */
    public synchronized void wake() {
    	try {
    		this.notify();
    	} catch (Exception e) {}
    }
    

	@Override
	public void run() {

		print("Started.");
		while (true) {
			// If there are requested floors, and the elevator is ready to move
			// (Doors are closed/no pending requests)
			if (isIdle() && reqFloors.isEmpty()) {
				print("ON STANDBY");
				pause();
			}
			if (!reqFloors.isEmpty() && !shutdown && !doorStuck && actionReady) {
				moveOneFloor();
				
				if (!shutdown) {
					// Update the scheduler about current status
					replyRequired = true;
					actionReady = false;
					communicator.send();

					if (arrivalSensorsTimer != null) {
						//Start an arrival sensor measurement
						arrivalSensorsTimer.startTime();
					}

					// Pending for reply from Scheduler
					waitForInstruction();
				}

			}
			// Slow down a bit
			wait(500);
		}
	}

}
