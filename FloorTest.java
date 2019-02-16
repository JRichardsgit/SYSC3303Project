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

	FloorSubsystem floorSubsystem;
	Scheduler scheduler;

	@BeforeEach
	void setUp() throws Exception {
		floorSubsystem = new FloorSubsystem(5); // Create a new floorSubsystem with 5 floors
		scheduler = new Scheduler(5); // Create a new scheduler for a system with 5 floors
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void floorDataTest() {
		// Initiate the interaction between the floorSubsystem sending data to the scheduler
		//floorSubsystem.send();
		scheduler.floorReceive();

		assertEquals(floorSubsystem.getFloorData().getStatus(), scheduler.getFloorData().getStatus());
		assertEquals(floorSubsystem.getFloorData().getFloorNum(), scheduler.getFloorData().getFloorNum());
	}
}
