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

	private Scheduler scheduler;
	private FloorSubsystem floorSubsystem;
	private ElevatorSubsystem elevatorSubsystem;

	@BeforeEach
	void setUp() throws Exception {
		//Set up system with 5 floors and 2 elevators
		elevatorSubsystem = new ElevatorSubsystem(5, 2);
		scheduler = new Scheduler(5, 2);
		floorSubsystem = new FloorSubsystem(5);

		//Floor sends floor request to scheduler
		floorSubsystem.send(new FloorData(2, true));

		//Scheduler receives request and relays it to an elevator
		scheduler.floorReceive();
		scheduler.updateRequests();
		scheduler.routeElevator();
		scheduler.elevatorSend(scheduler.getSchedulerData());
		
		//Elevator system receives request and routes it to appropriate elevator
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
	void TestReceiveFloorRequest() {
		//Verify that the scheduler properly received the floor request
		assertEquals(scheduler.getFloorData().getFloorNum(), 2);
		assertTrue(scheduler.getFloorData().upPressed());
	}
	
	@Test
	void TestRelayRequest() {
		//Verify the elevator received the relayed request and has now arrived on that floor
		assertEquals(elevatorSubsystem.getSchedulerData().getElevatorNumber(), scheduler.getSchedulerData().getElevatorNumber());
		assertEquals(elevatorSubsystem.getElevatorData().getCurrentFloor(), 2);
	}

}
