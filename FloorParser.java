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
	public String[][] instructions= new String[5][4];
	private int fields = 4;
			


	public void Parse(String file) throws FileNotFoundException {
		String[] str = new String[fields];
		String filename = file +".txt";
		File events = new File("C:\\Users\\JRich\\Documents\\" + filename); // FIX ME: change path to accept github 
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
		
		if (instructions[instructionNum][2] == "up") 
			ReqDirection = true;
			
		else  
				ReqDirection = false;
		
		return ReqDirection;
	}
	public String getSysTime() {
		String timeStamp = new SimpleDateFormat("HH:mm:ss:SS").format(System.currentTimeMillis());
		return timeStamp;
	}
	public void dispatch(int numFloors) {
		
		Queue<String> q1Up = new LinkedList<>();
		Queue<String> q2Up = new LinkedList<>();
		Queue<String> q2Down = new LinkedList<>();
		Queue<String> q3Up = new LinkedList<>();
		Queue<String> q3Down = new LinkedList<>();
		Queue<String> q4Down = new LinkedList<>();

		
		for(int j=0;j < instructions.length;j++) {
			if (instructions[j][0]==this.getSysTime()) {
				if (getFloor(j) == 1) {
					q1Up.add(instructions[j][0]);
					q1Up.add(instructions[j][3]);
				}
				else if (getFloor(j) == 2) {
					if(getDirection(j)) {
						q2Up.add(instructions[j][0]);
						q2Up.add(instructions[j][3]);
					}
					else {
						q2Down.add(instructions[j][0]);
						q2Down.add(instructions[j][3]);
					}
				}
				else if(getFloor(j) == 3) {
					if(getDirection(j)) {
						q3Up.add(instructions[j][0]);
						q3Up.add(instructions[j][3]);
					}
					else {
						q3Down.add(instructions[j][0]);
						q3Down.add(instructions[j][3]);
					}
				}
				else {
					q4Down.add(instructions[j][0]);
					q4Down.add(instructions[j][3]);
				}
			}
		}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
	FloorParser FloorData = new FloorParser();
	FloorData.Parse("floordata");
	System.out.println(FloorData.getSysTime());
	}
}
