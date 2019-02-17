# SYSC 3303 Project: L2_G10_Iteration2
## Group Members: Gabriele Sarwar, Jolar Tabungar, James Richards, Akash Joe, Ryan Tordesillas
This project aims to design a real time elevator control system that will quickly and efficiently transport passengers between floors.

## Classes:

### FloorSubsystem
* Acts as the client, sends floor requests to the scheduler for relaying to an elevator, manages the sending and receiving requests for all the floors

#### Floor
* Node of the FloorSubsystem; handles its respective floor requests, has its own floor number and up/down buttons, and floor lamps

### Scheduler
* Acts as the system server and manages interactions between the elevator and floor systems. Once a floor request is received, it is routed to the appropriate elevator and relays the request to the elevator subsystem.

### ElevatorSubsystem 
* Receives relayed floor requests from the scheduler and sends it to the routed elevator node

#### Elevator
* Node of the ElevatorSubsystem; handles it's own movement, opening/closing of doors, keeps track of all pending floor requests, lamps, and updates the scheduler about its status

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

