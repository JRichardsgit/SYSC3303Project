
/**
 * ErrorEvent Class
 *
 */
public class ErrorEvent {
	
	//Constants for error types
	public final static int ELEVATOR_STUCK = 0;
	public final static int DOOR_STUCK = 1;
	
	//Error type
	int type;
	long timeOccur;
	
	/**
	 * Create an error event with the specified type
	 * @param type the error type
	 * @param the time the error occurs
	 */
	public ErrorEvent(int type, long timeOccur) {
		this.type = type;
		this.timeOccur = timeOccur;
	}
	
	/**
	 * Returns the error type
	 * @return the error type
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Returns the time the error occurs
	 * @return the time the error occurs
	 */
	public long getTimeOccur() {
		return timeOccur;
	}
}
