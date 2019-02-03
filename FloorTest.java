import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Test Case for validating that floor data is properly sent from the floor to
 * the scheduler
 *
 */
class FloorTest {

	Floor floor;
	Scheduler scheduler;

	@BeforeEach
	void setUp() throws Exception {
		floor = new Floor(); // Create a new floor
		scheduler = new Scheduler(5); // Create a new scheduler for a system with 5 floors
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void floorDataTest() {
		// Initiate the interaction between the floor sending data to the scheduler
		floor.send();
		scheduler.floorReceive();

		assertEquals(floor.getFloorData().getStatus(), scheduler.getFloorData().getStatus());
		assertEquals(floor.getFloorData().getFloorNum(), scheduler.getFloorData().getFloorNum());
	}
}
