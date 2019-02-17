import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Test Case for validating that floorSubsystem data is properly sent from the floorSubsystem to
 * the scheduler
 *
 */
class FloorTest {
	
	private Scheduler scheduler;
	private FloorSubsystem floorSubsystem;
	private ElevatorSubsystem elevatorSubsystem;

	@BeforeEach
	void setUp() throws Exception {
		//Initiate system with 5 floors and two elevators
		elevatorSubsystem = new ElevatorSubsystem(5, 2);
		scheduler = new Scheduler(5, 2);
		floorSubsystem = new FloorSubsystem(5);

		//Send the floor request to the scheduler
		floorSubsystem.send(new FloorData(2, true));
		scheduler.floorReceive();
		scheduler.updateRequests();
	}

	@AfterEach
	void tearDown() throws Exception {
		elevatorSubsystem.closeSockets();
		scheduler.closeSockets();
		floorSubsystem.closeSocket();
	}

	@Test
	void TestFloorRequestSent() {
		//Verify that the scheduler received the floor request
		assertEquals(scheduler.getFloorData().getFloorNum(), 2);
		assertTrue(scheduler.getFloorData().upPressed());
	}
}
