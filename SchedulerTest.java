import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Test Case for validating that scheduler data is properly sent from the
 * scheduler to the floor and to the elevator
 *
 */
class SchedulerTest {

	Scheduler scheduler;
	Floor floor;
	Elevator elevator;

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
	void schedulerDataTest() {
		// Assert that the datapackets sent were the same ones received

		// Initiate the scheduler to send data to the elevator and floor

		// Check the data exchange between elevator and scheduler and verify that the
		// data is the same
		floor.send();
		scheduler.floorReceive();
		scheduler.elevatorSend();
		elevator.receive();
		assertEquals(scheduler.getSchedulerData().getStatus(), elevator.getSchedulerData().getStatus());

		// Check the exchange between the scheduler and floor and verify that the data
		// is the same
		scheduler.floorSend();
		floor.receive();
		assertEquals(scheduler.getSchedulerData().getStatus(), floor.getSchedulerData().getStatus());

	}

}
