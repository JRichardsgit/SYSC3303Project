import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.time.*;

public class FloorParser extends Thread {
	private long startTime = System.currentTimeMillis();
	private FloorSubsystem fSystem;
	private final int instructionFields = 4;
	private int requests = 4;
	private int floors;
	private String[][] instructions = new String[requests][instructionFields];
	private Queue<String[]>[] upQueue;
	private Queue<String[]>[] downQueue;
	
	public FloorParser(FloorSubsystem fSystem, int floors) {
		this.fSystem = fSystem;
		this.floors = floors;
	}

	public void parse(String file) throws FileNotFoundException {
		String[] str = new String[instructionFields];
		String filename = file + ".txt";
		File events = new File("Assets\\" + filename); // FIX ME: change path to accept github
		Scanner scan = new Scanner(events); // create new scanner
		for (int j = 0; scan.hasNext(); j++) { // format instructions int 2D array. j = instruction #, i = instruction
												// type
			for (int i = 0; i < instructionFields; i++) {
				str[i] = scan.next();
				instructions[j][i] = str[i];
			}
		}
		scan.close(); // close scanner

		// Print parsed instructions
		for (int j = 0; j < 4; j++) {
			System.out.print((j + 1) + ": ");
			for (int i = 0; i < instructionFields; i++) {
				System.out.print(instructions[j][i] + " ");
			}
			System.out.println();
		}
	}

	public String getTime(int instructionNum) {
		return instructions[instructionNum][0];
	}

	public int getFloor(int instructionNum) {
		return Integer.parseInt(instructions[instructionNum][1]);
	}

	public int getDest(int instructionNum) {
		return Integer.parseInt(instructions[instructionNum][3]);
	}

	public boolean getDirection(int instructionNum) {
		if (instructions[instructionNum][2].equals("up"))
			return true;
		return false;
	}

	/**
	 * Returns the current system time
	 * @return
	 */
	public String getSysTime() {
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis() - startTime);
		return timeStamp;
	}
	
	public long getRequestTimeInMillis(String requestTime) {
		LocalTime localTime = LocalTime.parse(requestTime);
		return localTime.toSecondOfDay() * 1000;
	}
	
	public long getElapsedTime() {
		return System.currentTimeMillis() - startTime;
	}

	public void dispatch() {
		upQueue = new Queue[floors];
		downQueue = new Queue[floors];
		for (int i = 0; i < floors; i++) {
			upQueue[i] = new LinkedList<String[]>();
			downQueue[i] = new LinkedList<String[]>();
		}
		for (int j = 0; j < requests; j++) { // iterate through requests and if direction of req is up, the time,
			// reqfloor, & destfloor are added to upQueue[i]. vice-versa for down
			if (getDirection(j)) {
				upQueue[getFloor(j)].add(instructions[j]);
			} else {
				downQueue[getFloor(j)].add(instructions[j]);
			}

		}

	}
	
	public void run() {
		try {
			parse("floordata");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dispatch();
		
		int count = 0;
		while(count < requests) {
			for(int i = 0; i< floors;i++) {
				if(upQueue[i].peek() != null) {
					String requestTime = upQueue[i].peek()[0];
					if (getElapsedTime() >= getRequestTimeInMillis(requestTime)) {
						String currentRequest[] = upQueue[i].poll();
						int source = Integer.parseInt(currentRequest[1]);
						int destination = Integer.parseInt(currentRequest[3]);
						fSystem.goUp(source, destination);
						count ++;
					}
				}
				if(downQueue[i].peek() != null) {
					String requestTime = downQueue[i].peek()[0];
					if (getElapsedTime() >= getRequestTimeInMillis(requestTime)) {
						String currentRequest[] = downQueue[i].poll();
						int source = Integer.parseInt(currentRequest[1]);
						int destination = Integer.parseInt(currentRequest[3]);
						fSystem.goDown(source, destination);
						count ++;
					}
				}
			}
		}
		
		try {
			fSystem.saveToFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
