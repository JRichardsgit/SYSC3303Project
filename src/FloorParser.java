import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.time.*;

public class FloorParser extends Thread {
	//CONSTANTS FOR RETRIEVING REQUEST DATA
	public final int REQUEST_TIME = 0;
	public final int SOURCE_FLOOR = 1;
	public final int DIRECTION = 2;
	public final int DEST_FLOOR = 3;
	public final boolean UP = true;
	public final boolean DOWN = false;
	private final int NUM_INSTRUCTION_FIELDS = 4;
	
	//Start time 
	private long startTime;
	
	//Reference to floor subsystem
	private FloorSubsystem fSystem;
	
	//Floors in the system
	private int floors;
	private String filename;
	
	//Request lists and queues
	private ArrayList<String[]> requestList;
	private Queue<String[]>[] upQueue;
	private Queue<String[]>[] downQueue;
	
	public FloorParser(FloorSubsystem fSystem, int floors, String filename) {
		this.fSystem = fSystem;
		this.floors = floors;
		startTime = System.currentTimeMillis();
		
		this.filename = filename;
		
		requestList = new ArrayList<String[]>();
		upQueue = new Queue[floors];
		downQueue = new Queue[floors];
	}

	/**
	 * Parse the given file into an array of requests
	 * @throws FileNotFoundException
	 */
	public void parse() throws FileNotFoundException {
		File events = new File("Assets\\Request Files\\" + filename);
		Scanner scan = new Scanner(events); // create new scanner
		//Scan through the whole file and parse each floor request
		while (scan.hasNext()) {
			String request[] = new String[NUM_INSTRUCTION_FIELDS];
			for (int i = 0; i < NUM_INSTRUCTION_FIELDS; i++) {
				request[i] = scan.next();
			}
			requestList.add(request);
		}
		scan.close(); // close scanner

		/*
		// Print parsed instructions 
		for (String[] request: requestList) {
			for (int i = 0; i < NUM_INSTRUCTION_FIELDS; i++) {
				System.out.print(request[i] + " ");
			}
			System.out.println();
		}
		*/
	}
	
	/**
	 * Determine the time in milliseconds of the given request time
	 * @param requestTime
	 * @return request time in milliseconds
	 */
	public long getRequestTimeInMillis(String requestTime) {
		LocalTime localTime = LocalTime.parse(requestTime);
		return localTime.toSecondOfDay() * 1000;
	}
	
	/**
	 * Determine the elapsed time in milliseconds since start up
	 * @return
	 */
	public long getElapsedTime() {
		return System.currentTimeMillis() - startTime;
	}

	/**
	 * Send all requests to their respective queues
	 */
	public void dispatch() {
		//Instantiate two queues for each floor, one for each direction
		for (int i = 0; i < floors; i++) {
			upQueue[i] = new LinkedList<String[]>();
			downQueue[i] = new LinkedList<String[]>();
		}
		
		for (String[] request: requestList) { 
			int sourceFloor = Integer.parseInt(request[SOURCE_FLOOR]) - 1;
			if (request[DIRECTION].equals("up")) {
				upQueue[sourceFloor].add(request);
			} else {
				downQueue[sourceFloor].add(request);
			}
		}

	}
	
	public void run() {
		//Parse the given file
		try {
			parse();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dispatch();
		
		int count = 0;
		//Send each request at it's respective request time
		while(count < requestList.size()) {
			for(int i = 0; i< floors;i++) {
				if(upQueue[i].peek() != null) {
					String requestTime = upQueue[i].peek()[REQUEST_TIME];
					if (getElapsedTime() >= getRequestTimeInMillis(requestTime)) {
						String currentRequest[] = upQueue[i].poll();
						int source = Integer.parseInt(currentRequest[SOURCE_FLOOR]);
						int destination = Integer.parseInt(currentRequest[DEST_FLOOR]);
						fSystem.goUp(source, destination);
						count ++;
					}
				}
				if(downQueue[i].peek() != null) {
					String requestTime = downQueue[i].peek()[REQUEST_TIME];
					if (getElapsedTime() >= getRequestTimeInMillis(requestTime)) {
						String currentRequest[] = downQueue[i].poll();
						int source = Integer.parseInt(currentRequest[SOURCE_FLOOR]);
						int destination = Integer.parseInt(currentRequest[DEST_FLOOR]);
						fSystem.goDown(source, destination);
						count ++;
					}
				}
			}
		}
		
		//After all the requests have been sent save measurements to the file
		try {
			fSystem.saveToFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
