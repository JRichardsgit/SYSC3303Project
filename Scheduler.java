// simple echo scheduler

import java.io.*;
import java.net.*;

public class Scheduler {

   DatagramPacket sendPacket, sendPacket2, receivePacket;
   DatagramSocket sendSocket, receiveSocket;

   public Scheduler()
   {
      try {
         // Construct a datagram socket and bind it to any available 
         // port on the local host machine. This socket will be used to
         // send UDP Datagram packets.
         sendSocket = new DatagramSocket();

         // Construct a datagram socket and bind it to port 5000 
         // on the local host machine. This socket will be used to
         // receive UDP Datagram packets.
         receiveSocket = new DatagramSocket(4000);
        
         
         // to test socket timeout (2 seconds)
         //receiveSocket.setSoTimeout(2000);
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      } 
   }

   public void receiveAndReply()
   {
      // Construct a DatagramPacket for receiving packets up 
      // to 100 bytes long (the length of the byte array).

      byte data[] = new byte[100];
      receivePacket = new DatagramPacket(data, data.length);
      System.out.println("Scheduler: Waiting for Packet.\n");

      // Block until a datagram packet is received from receiveSocket.
      try {        
         System.out.println("Waiting..."); // so we know we're waiting
         receiveSocket.receive(receivePacket);
      } catch (IOException e) {
         System.out.print("IO Exception: likely:");
         System.out.println("Receive Socket Timed Out.\n" + e);
         e.printStackTrace();
         System.exit(1);
      }

      // Process the received datagram.
      System.out.println("scheduler: Packet received:");
      System.out.println("From host: " + receivePacket.getAddress());
      System.out.println("Host port: " + receivePacket.getPort());
      int len = receivePacket.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: " );

      // Form a String from the byte array.
      String received = new String(data,0,len);   
      System.out.println(received + "\n");
      
      // Slow things down (wait 5 seconds)
      try {
          Thread.sleep(8000);
      } catch (InterruptedException e ) {
          e.printStackTrace();
          System.exit(1);
      }
 
      
      
      String s = "i am the scheduler packet letting you know the message request has been recieved. "
      		+ "I will now get an elevator for you.";
      byte msg[] = s.getBytes();
      sendPacket = new DatagramPacket(msg, msg.length,
                               receivePacket.getAddress(), receivePacket.getPort());

      System.out.println( "Server: Sending packet:");
      System.out.println("To host: " + sendPacket.getAddress());
      System.out.println("Destination host port: " + sendPacket.getPort());
      len = sendPacket.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: ");
      System.out.println(new String(sendPacket.getData(),0,len));
      // or (as we should be sending back the same thing)
      // System.out.println(received); 
        
      // Send the datagram packet to the client via the send socket. 
      try {
         sendSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
      
      String s2 = "Scheduler: hi elevator server, i need an elevator.";
      byte msg2[] = s2.getBytes();
      sendPacket2 = new DatagramPacket(msg2, msg2.length,
                               receivePacket.getAddress(), 2000);//elevator server port

      System.out.println( "Server: Sending packet:");
      System.out.println("To host: " + sendPacket2.getAddress());
      System.out.println("Destination host port: " + 2000);
      len = sendPacket2.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: ");
      System.out.println(new String(sendPacket2.getData(),0,len));
      // or (as we should be sending back the same thing)
      // System.out.println(received); 
        
      // Send the datagram packet to the client via the send socket. 
      try {
         sendSocket.send(sendPacket2);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Scheduler: packet sent to elevator");

      // We're finished, so close the sockets.
      sendSocket.close();
      receiveSocket.close();
   }
   
   public static void main( String args[] )
   {
	   Scheduler c = new Scheduler();
      c.receiveAndReply();
    
   }
}
