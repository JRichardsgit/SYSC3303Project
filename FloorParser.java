import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class FloorParser extends Thread {
	private final int NUM_INSTRUCTION_FIELDS = 4; // # of fields per instruction
	public ArrayList<String[]> instructions = new ArrayList<String[]>(); // list that stores each instruction

	public void parse(String file) throws FileNotFoundException {
		String filename = file +".txt";
		File events = new File("Assets\\" + filename);
		Scanner scan = new Scanner(events); // create new scanner

		while (scan.hasNext()) { 
			String[] currentInstruction = new String[NUM_INSTRUCTION_FIELDS];
			for(int i = 0; i < NUM_INSTRUCTION_FIELDS;i++) {
				currentInstruction[i] = scan.next();
			}
			instructions.add(currentInstruction);
		}
		scan.close(); // close scanner

		//Print the instructions
		for(int j = 0; j < instructions.size(); j++) {
			System.out.print("\n" + (j + 1) + ": ");
			for(int i = 0; i< NUM_INSTRUCTION_FIELDS;i++) {
				System.out.print(instructions.get(j)[i] + " "); 
			}
		}	
	}

	/**
	 * Returns the list of instructions
	 * @return the list of instructions
	 */
	public ArrayList<String[]> getInstructions() {
		return instructions;
	}
	
	public void run() {
		try {
			parse("floordata");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FloorParser floorInstructions = new FloorParser();
		floorInstructions.start();
	}
}