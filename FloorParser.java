import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class FloorParser{
	public String ReqTime = null;
	public int ReqFloor = 0;
	public boolean ReqDirection = false;
	public int ReqDestFloor = 0;
	private int fields = 4;
	public int requests = 4;
	public int floors = 22;
	public String[][] instructions= new String[requests][fields]; 
	public Queue<String>[] upQ;
	public Queue<String>[] downQ;
	

	public void Parse(String file) throws FileNotFoundException {
		String[] str = new String[fields];
		String filename = file +".txt";
		File events = new File("Assets\\" + filename); // FIX ME: change path to accept github 
		Scanner scan = new Scanner(events); // create new scanner
		for(int j =0; scan.hasNext();j++) { //format instructions int 2D array. j = instruction #, i = instruction type
			for(int i =0; i<fields;i++) {
				str[i] = scan.next();
				instructions[j][i] = str[i];
				}
		}
		scan.close(); // close scanner
		
		
		// print parsed instructions//
		for(int j =0; j<4;j++) {
			System.out.println();
			System.out.print(j+1 + ": ");
			for(int i =0; i<fields;i++) {
		System.out.print(instructions[j][i] +" "); 
			}
		}	
	}
	
	
	public String getTime(int instructionNum) {
		return instructions[instructionNum][0];
	}
	
	public int getFloor(int instructionNum) {
		return ReqFloor = Integer.parseInt(instructions[instructionNum][1]);
	}
	
	public int getDest(int instructionNum ) {
		return ReqDestFloor = Integer.parseInt(instructions[instructionNum][3]);
	}
	
	public boolean getDirection(int instructionNum) {
		
		if (instructions[instructionNum][2].equals("up")) 
			ReqDirection = true;
			
		else if (instructions[instructionNum][2].equals("down")) 
			ReqDirection = false;  
		else
			System.out.println("Invalid direction");
		
		return ReqDirection;
	}
	public String getSysTime() {
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
		return timeStamp;
	}
	public void dispatch() {
		System.out.println("Made it in");
		upQ = new Queue[floors];
		downQ = new Queue[floors];
		for(int i = 0;i<floors;i++) {
			upQ[i] = new LinkedList<>();
			downQ[i] = new LinkedList<>();
		}
		System.out.println("Made the Queues");
		for(int j=0;j < requests;j++) {			//iterate through requests and if direction of req is up, the time, 
												//reqfloor, & destfloor are added to upQ[i]. vice-versa for down
				if (getDirection(j) ) {
					upQ[getFloor(j)].add(instructions[j][0]);
					upQ[getFloor(j)].add(instructions[j][1]);
					upQ[getFloor(j)].add(instructions[j][3]);
					System.out.println("added to upQ" + getFloor(j) + upQ[getFloor(j)]);
				}
				else  { 
					downQ[getFloor(j)].add(instructions[j][0]);
					downQ[getFloor(j)].add(instructions[j][1]);
					downQ[getFloor(j)].add(instructions[j][3]);
					System.out.println("added to downQ");
				}
					
			}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
	FloorParser FloorData = new FloorParser();
	FloorData.Parse("floordata");
	System.out.println(FloorData.getFloor(0));
	System.out.println(FloorData.getDirection(0));
	FloorData.dispatch();
	System.out.println("Made it");
	System.out.println(FloorData.upQ[1].peek());
	}
}
