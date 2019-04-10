

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * 
 * Test Case for validating that ElevatorSubsystem data is properly sent from
 * the elevatorSubsystem to the scheduler
 *
 */
public class TestCases extends TestCase {

	private Scheduler s;
	private FloorSubsystem f;
	private ElevatorSubsystem e;
	private long initialization;
	
	void waitToCheck(long waitTime) {
		boolean checkTest = false;
		
		while(!checkTest) {
			long elapsedTime = System.currentTimeMillis() - initialization;
			if (elapsedTime >= waitTime) {
				checkTest = true;
			}
		}
	}
	
	public void waitFor(long waitTime) {
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setUp() throws Exception {
		
		// Set up system with 5 floors and 2 elevators
		e = new ElevatorSubsystem(5, 2, Main.TEST_MODE);
	    s = new Scheduler(5, 2, Main.TEST_MODE);
		f = new FloorSubsystem(5, Main.TEST_MODE);
		initialization = System.currentTimeMillis();
	}

	public void tearDown() throws Exception {
	
	}

	public void testBasicSystemFunctionality() {
		//Trigger Floor Request From floor 1 going up to floor 5
		f.goUp(1, 5);
		waitToCheck(500);
		ArrayList<Integer> elev0Requests = e.getElevator(0).getElevatorData().getRequestedFloors();
		
		//Check that elevator 0 has received the request
		assertEquals(elev0Requests.size(), 1);
		
		waitFor(2000);
		//Trigger Floor Request From floor 1 going up to floor 5
		f.goUp(1, 4);
		waitFor(500);
		
		ArrayList<Integer> elev1Requests = e.getElevator(1).getElevatorData().getRequestedFloors();
		
		//Check that elevator 1 has received the request
		assertEquals(elev1Requests.size(), 1);
		
		//Wait till elevators reach their destination
		waitToCheck(12000);
		
		//Check that elevator 0 has reached floor 5
		int elev0floor = e.getElevator(0).getElevatorData().getCurrentFloor();
		assertEquals(elev0floor, 5);
		
		//check that elevator 1 has reached floor 4
		int elev1floor = e.getElevator(1).getElevatorData().getCurrentFloor();
		assertEquals(elev1floor, 4);
		
		boolean elev0doorOpen = e.getElevator(0).getElevatorData().doorOpened();
		boolean elev1doorOpen = e.getElevator(1).getElevatorData().doorOpened();
		
		//Check that the elevators have opened their doors
		assertTrue(elev0doorOpen);
		assertTrue(elev1doorOpen);
		
		elev0Requests = e.getElevator(0).getElevatorData().getRequestedFloors();
		elev1Requests = e.getElevator(1).getElevatorData().getRequestedFloors();
		
		//Check that the elevators have no more pending requests
		assertTrue(elev0Requests.isEmpty());
		assertTrue(elev1Requests.isEmpty());
		
	}
	
	
	

	
}
