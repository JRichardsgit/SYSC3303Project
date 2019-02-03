import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Test Case for validating that Elevator data is properly sent from the elevator to the scheduler
 *
 */
public class ElevatorTest {
	
	private Scheduler scheduler;
	private Floor floor;
	private Elevator elevator;

	@BeforeEach
	void setUp() throws Exception {
		scheduler = new Scheduler(5);
		floor = new Floor();
		elevator = new Elevator();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void ElevatorDataTest() {
		//Initiate the floor request to the scheduler and the scheduler request to the elevator
		floor.send();
		scheduler.floorReceive();
		scheduler.elevatorSend();
		elevator.receive();
		
		//Initiate the elevator response to the scheduler 
		elevator.send();
		scheduler.elevatorReceive();
		
		//Verify that the elevator data is the same
		assertEquals(elevator.getElevatorData().getStatus(), scheduler.getElevatorData().getStatus());
		
	}
}
