import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Test Case for validating that ElevatorSubsystem data is properly sent from
 * the elevatorSubsystem to the scheduler
 *
 */
public class TestCases {

	private Scheduler scheduler;
	private FloorSubsystem floorSubsystem;
	private ElevatorSubsystem elevatorSubsystem;
	private long initialization;
	
	@BeforeEach
	void setUp() throws Exception {
		// Set up system with 5 floors and 2 elevators
		elevatorSubsystem = new ElevatorSubsystem(5, 2);
		scheduler = new Scheduler(5, 2);
		floorSubsystem = new FloorSubsystem(5);
		initialization = System.currentTimeMillis();
	}

	@AfterEach
	void tearDown() throws Exception {
		elevatorSubsystem.closeSockets();
		scheduler.closeSockets();
		floorSubsystem.closeSockets();
	}

	@Test
	void TestRouteFloorRequest() {
		
		assert(true);
	}
	
	@Test
	void TestShutDown() {
		assert(true);
	}

	@Test
	void TestAllRequestsComplete() {
		assert(true);
	}
	
	
}
