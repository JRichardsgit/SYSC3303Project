import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Test Case for validating that ElevatorSubsystem data is properly sent from the elevatorSubsystem to the scheduler
 *
 */
public class ElevatorTest {
	
	private Scheduler scheduler;
	private FloorSubsystem floorSubsystem;
	private ElevatorSubsystem elevatorSubsystem;

	@BeforeEach
	void setUp() throws Exception {
		scheduler = new Scheduler(5, 2);
		floorSubsystem = new FloorSubsystem(5);
		elevatorSubsystem = new ElevatorSubsystem(5, 2);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void ElevatorDataTest() {
		//Initiate the floorSubsystem request to the scheduler and the scheduler request to the elevatorSubsystem
		//floorSubsystem.send();
		scheduler.floorReceive();
		//scheduler.elevatorSend();
		elevatorSubsystem.receive();
		
		//Initiate the elevatorSubsystem response to the scheduler 
		//elevatorSubsystem.send();
		scheduler.elevatorReceive();
		
		//Verify that the elevatorSubsystem data is the same
		assertEquals(elevatorSubsystem.getElevatorData().getStatus(), scheduler.getElevatorData().getStatus());
		
	}
}
