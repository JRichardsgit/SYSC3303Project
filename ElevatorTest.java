import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Test Case for validating that ElevatorSubsystem data is properly sent from
 * the elevatorSubsystem to the scheduler
 *
 */
public class ElevatorTest {

	private Scheduler scheduler;
	private FloorSubsystem floorSubsystem;
	private ElevatorSubsystem elevatorSubsystem;

	@BeforeEach
	void setUp() throws Exception {
		// Set up system with 5 floors and 2 elevators
		elevatorSubsystem = new ElevatorSubsystem(5, 2);
		scheduler = new Scheduler(5, 2);
		floorSubsystem = new FloorSubsystem(5);

		// Floor sends floor request to scheduler
		floorSubsystem.send(new FloorData(2, true));

		// Scheduler receives request and relays it to an elevator
		scheduler.floorReceive();
		scheduler.updateRequests();
		scheduler.routeElevator();
		scheduler.elevatorSend(scheduler.getSchedulerData());

		// Elevator system receives request and routes it to appropriate elevator
		elevatorSubsystem.receive();
		elevatorSubsystem.routePacket();
	}

	@AfterEach
	void tearDown() throws Exception {
		elevatorSubsystem.closeSockets();
		scheduler.closeSockets();
		floorSubsystem.closeSocket();
	}

	@Test
	void TestElevatorReceiveFloorRequest() {
		// Verify floor request was sent to elevator
		assertEquals(new Integer(elevatorSubsystem.getSchedulerData().getReqFloors().get(0)), new Integer(2));
	}

	@Test
	void TestElevatorReceiveRoutedFloorRequest() {
		// Verify elevator received and fulfilled the floor request, now elevator is on
		// floor 2
		assertEquals(new Integer(elevatorSubsystem.getElevatorData().getCurrentFloor()), new Integer(2));
	}
}
