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
	private int fields = 4;						//# of fields per instruction
	private int requests = 5;					//# of instructions on the text file
	public String[][] instructions= new String[requests][fields];	//2D array that stores the instructions
	public Queue<String> q1Up = new LinkedList<>();			//Create queues for each floor
	public Queue<String> q2Up = new LinkedList<>();
	public Queue<String> q2Down = new LinkedList<>();
	public Queue<String> q3Up = new LinkedList<>();
	public Queue<String> q3Down = new LinkedList<>();
	public Queue<String> q4Down = new LinkedList<>();

	
			


	public void Parse(String file) throws FileNotFoundException {
		String[] str = new String[fields];
		String filename = file +".txt";
		File events = new File("Assets\\" + filename); 	// FIX ME: change path to accept github 
		Scanner scan = new Scanner(events); 					// create new scanner
		for(int j =0; scan.hasNext();j++) { 					//format instructions int 2D array. j = instruction #, i = instruction type
			for(int i =0; i<fields;i++) {
				str[i] = scan.next();
				instructions[j][i] = str[i];
				}
		}
		scan.close(); 								// close scanner
		
		
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
	
	public boolean getDirection(int instructionNum) {	//return direction of instruction bool(true = up/false=down)
		
		if (instructions[instructionNum][2].equals("up")) 
			ReqDirection = true;
			
		else if (instructions[instructionNum][2].equals("down")) 
				ReqDirection = false;
		else	
			System.out.println("Incorrect Direction");
		return ReqDirection;
	}
	public String getSysTime() { 				//get system time as a string to compare to instruction times
		String timeStamp = new SimpleDateFormat("HH:mm:ss:SS").format(System.currentTimeMillis());
		return timeStamp;
	}
	public void dispatch(int numFloors) {
		
		for(int j=0;j < requests; j++) {     			// iterate through instructions
			//if (instructions[j][0]==this.getSysTime()) {	//Compare instruction time to current time **Not Sure if working correctly**
				if (getFloor(j) == 1) {			//logic to place instructions in appropriate queues
					this.q1Up.add(instructions[j][0]);
					this.q1Up.add(instructions[j][3]);
				}
				else if (getFloor(j) == 2) {
					if(getDirection(j)) {
						this.q2Up.add(instructions[j][0]);
						this.q2Up.add(instructions[j][3]);
					}
					else {
						this.q2Down.add(instructions[j][0]);
						this.q2Down.add(instructions[j][3]);
					}
				}
				else if(getFloor(j) == 3) {
					if(getDirection(j)) {
						this.q3Up.add(instructions[j][0]);
						this.q3Up.add(instructions[j][3]);
					}
					else {
						this.q3Down.add(instructions[j][0]);
						this.q3Down.add(instructions[j][3]);
					}
				}
				else {
					this.q4Down.add(instructions[j][0]);
					this.q4Down.add(instructions[j][3]);
				}
			}
		//}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
	FloorParser FloorData = new FloorParser();
	FloorData.Parse("floordata");
	System.out.println(FloorData.getSysTime());
	}
}
