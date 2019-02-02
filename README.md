# SYSC3303Project iteration 1

This project aims to design a real time elevator control system that will quickly and efficiently transport passengers between floors.

Classes:

Floor - Client, sends floor data req. Sends request to the scheduler for an elevator, aswell as the floor number, button pressed and time which it was pressed.

Scheduler - Acts as the system server and manages interactions between the elevator and floor systems. Once the request from floor is recieved, it tells floor that its been received and then contacts elevator system to request an elevator at the floor level. It tells the elevator where to go and how to get there.

Elevator - receieves req. from scheduler, processes the information and sends an elevator. It lets scheduler know that the elevator has been sent.

Setup Instructions:

1 run Elevator.java
2 run Scheduler.java
3 run Floor.java


After, Use the "display selected console" to show each console output, starting from floor, then scheduler output, then elevator output.

The flow is as follows: Floor requests and elevator from scheduler, Scheduler notifies request has been recieved and contacts elevator system, the elevator system processes information and sends an elevator and notifies scheduler.
