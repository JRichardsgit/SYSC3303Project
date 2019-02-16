import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Test Case for validating that scheduler data is properly sent from the
 * scheduler to the floorSubsystem and to the elevatorSubsystem
 *
 */
class SchedulerTest {

	Scheduler scheduler;
	FloorSubsystem floorSubsystem;
	ElevatorSubsystem elevatorSubsystem;

	@BeforeEach
	void setUp() throws Exception {
		scheduler = new Scheduler(5);
		floorSubsystem = new FloorSubsystem(5);
		elevatorSubsystem = new ElevatorSubsystem(2);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void schedulerDataTest() {
		// Assert that the datapackets sent were the same ones received

		// Initiate the scheduler to send data to the elevatorSubsystem and floorSubsystem

		// Check the data exchange between elevatorSubsystem and scheduler and verify that the
		// data is the same
		//floorSubsystem.send();
		scheduler.floorReceive();
		//scheduler.elevatorSend();
		elevatorSubsystem.receive();
		assertEquals(scheduler.getSchedulerData().getStatus(), elevatorSubsystem.getSchedulerData().getStatus());

		// Check the exchange between the scheduler and floorSubsystem and verify that the data
		// is the same
		//scheduler.floorSend();
		floorSubsystem.receive();
		assertEquals(scheduler.getSchedulerData().getStatus(), floorSubsystem.getSchedulerData().getStatus());

	}

}
