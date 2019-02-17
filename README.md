# SYSC 3303 Project: L2G2_Iteration2_
## Group Members: Gabriele Sarwar, Jolar Tabungar, James Richards, Akash Joe, Ryan Tordesillas
This project aims to design a real time elevator control system that will quickly and efficiently transport passengers between floors.

## Classes:

### Floor
* Acts as the client, sends floor data requests to the scheduler for an elevator, as well as the floor number, button pressed and time which it was pressed.

### Scheduler
* Acts as the system server and manages interactions between the elevator and floor systems. Once the request from floor is recieved, it tells floor that it has been received and then contacts elevator system to request an elevator at the floor level. It tells the elevator where to go and how to get there.

### Elevator 
* Receives requests from scheduler, processes the information and sends an elevator. It lets scheduler know that the elevator has been sent.

## Setup Instructions:

#### 1. Run ElevatorSubsystem.java
#### 2. Run Scheduler.java
#### 3. Run FloorSubsystem.java


* After, Use the "display selected console" to show each console output, starting from floor, then scheduler output, then elevator output.

#### System Flow: 
* Floor requests an elevator and sends it to the scheduler
* Scheduler decides which elevator to send the request
* Scheduler relays request to the elevator subsystem
* Elevator subsystem relays it to the appropriate elevator
* Elevator that receives the request responds to the scheduler and updates it about it's status

#### Testing
* Testing and error handling are incorporated in the respective test cases (ElevatorTest, FloorTest, SchedulerTest)
* Run each test case as a java application, separately

