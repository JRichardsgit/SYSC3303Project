import java.util.ArrayList;

/**
 * Floor Node for nth Floor
 */
public class Floor {
	
	private final int floorNum; //this floor's number
	private final FloorSubsystem fSystem; //the reference to the floor subsystem
	
	private boolean upPressed; //true if up is pressed/false otherwise
	private boolean downPressed; //true if down is pressed/false otherwise
	private ArrayList<Integer> floorLamps; //list of floors the elevators are currently on
	
	/**
	 * Create a new floor with designated floor number
	 * @param floorNum designated floor number
	 * @param fSystem reference to the floor subsystem
	 */
	public Floor(int floorNum, FloorSubsystem fSystem) {
		this.floorNum = floorNum; 
		this.fSystem = fSystem;
		
		floorLamps = new ArrayList<Integer>();
	}
	
	/**
	 * Presses the up button
	 */
	public void pressUp() {
		upPressed = true;
	}
	
	/**
	 * Presses the down button
	 */
	public void pressDown() {
		downPressed = true;
	}
	
	/**
	 * Return true if up button is pressed, false otherwise
	 * @return true if up button is pressed, false otherwise
	 */
	public boolean upPressed() {
		return upPressed;
	}
	
	/**
	 * Return true if down button is pressed, false otherwise
	 * @return true if down button is pressed, false otherwise
	 */
	public boolean downPressed() {
		return downPressed;
	}
	
	/**
	 * Turn on the corresponding floor lamps
	 * @param floorLamps the lamps to turn on
	 */
	public void setLamps(ArrayList<Integer> floorLamps) {
		this.floorLamps = floorLamps;
	}

}
