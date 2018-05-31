import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
public class BookClient {
	//string used for udp or tcp connection type
  final String UDP = "UDP";
  final String TCP = "TCP";
  
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    
    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    hostAddress = "localhost";
    tcpPort = 7000;// hardcoded -- must match the server's tcp port
    udpPort = 8000;// hardcoded -- must match the server's udp port
    //added line
    BookClient client = null;
    try {
    	//set client
    	client = new BookClient(hostAddress, tcpPort, udpPort, clientId);
        Scanner sc = new Scanner(new FileReader(commandFile));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          //System.out.println(cmd);
          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
        	  client.setMode(tokens[1]);
          }
          //if any other command the call client send command function
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  client.sendCommand(cmd);
          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  client.sendCommand(cmd);
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  client.sendCommand(cmd);
          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  client.sendCommand(cmd);
          } else if (tokens[0].equals("exit")) {
            // TODO: send appropriate command to the server
        	  client.sendCommand(cmd);
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (FileNotFoundException e) {
	e.printStackTrace();
    }
    catch(IOException e) {
    	System.err.println(e);
    }
    //when everything is finished then close the client
    finally {
    	if(client!=null) {
    		client.close();
    	}
    }
  }
  
  //added variables
  
  //ports needed for connections
  private int udpPort;
  //tcp port variable
  private int tcpPort;
  //string to determine the type of connection, udp or tcp
  private String portType;
  //variable to hold the host
  private String hostName;
  //address of the host
  private InetAddress hostIP;
  //byte array for buffer data
  private byte[] buffer;
  //byte array for the data to be received
  private byte[] receiveBuffer;
  //datagram packet to send udp
  private DatagramPacket senderUDP;
  //datagram packet to receive data for udp
  private DatagramPacket receiverUDP;
  //datagram socket for udp
  private DatagramSocket udpSocket;
  //socket to be used for tcp
  private Socket tcpSocket;
  //scanner for data input
  private Scanner dataIn;
  //printstream for print line
  private PrintStream printOut;
  //printstream used for outputting to textfile
  private PrintStream textFile;
  
  public BookClient(String hostName, int tcpPort, int udpPort, int clientID) throws IOException {
	  //set the proper hostname and host address
	  this.hostName = hostName;
	  //set the ip address to the proper inet address
	  this.hostIP = InetAddress.getByName(hostName);
	  //set the ports to the proper port number
	  this.udpPort = udpPort;
	  //set tcp port to passed tcp port
	  this.tcpPort = tcpPort;
	  
	  //set the buffer size to one byte
	  this.receiveBuffer = new byte [1024];
	  //set default to UDP
	  this.portType = UDP;
	  //set sockets for tcp and udp
	  this.tcpSocket = new Socket(this.hostName, this.tcpPort);
	  this.udpSocket = new DatagramSocket();
	  //initialize scanner to the proper socket input
	  this.dataIn = new Scanner(tcpSocket.getInputStream());
	  //set a printstream to output
	  this.printOut = new PrintStream(tcpSocket.getOutputStream());
	  //create a printstream to the output file
	  this.textFile = new PrintStream(new File("out_"+ clientID + ".txt"));
  }
  
  public void setMode(String connectionType) {
	  //if input is U then set it to UDP
	  if(connectionType.equals("U")) {
		  //set port to udp default
		  this.portType = UDP;
	  }
	  //if input is T then set to TCP
	  else if(connectionType.equals("T")) {
		  //set port to tcp default
		  this.portType = TCP;
	  }
	  //System.out.println("Using " + connectionType + " connection.");
  }
  
  public void sendCommand(String command) throws IOException{
	  //set buffer values
	  buffer = new byte[command.length()];
	  buffer = command.getBytes();
	  //create string variable to hold output to print
	  String outputString = "";
	  //sed the command through udp if connection mode is udp
	  if(portType.equals(UDP)) {
		  //set the send udp to a new packet with the buffer and size
		  senderUDP = new DatagramPacket(buffer, buffer.length, hostIP, udpPort);
		  //send the udp data through the socket
		  udpSocket.send(senderUDP);
		  //set up the receive udp
		  receiverUDP = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		  //set the udp socket to the receive value
		  udpSocket.receive(receiverUDP);
		  //set the output string to the returned data
		  outputString = new String(receiverUDP.getData(), 0, receiverUDP.getLength());
		  
		  //testing writing output
		  if(outputString.equals("")) {
			  textFile.write((outputString).getBytes());
		  }
		//ese write the result to textfile
		  else {
			  textFile.write((outputString+"\n").getBytes());
		  }
		  
		  //System.out.println(outputString);
	  }
	  //else check if the connection type is TCP
	  else if (portType.equals(TCP)) {
		  
		  //if it is tcp then print out the command
		  printOut.println(command);
		  //call the flush method
		  printOut.flush();
		  //iterate through to check if there are any commands left
		  while(dataIn.hasNextLine()) {
			  //set the outupt string to the next line from input
			  outputString = dataIn.nextLine();
			  //if it is done then exit
			  if(outputString.equals("finish")) {
				  break;
			  }
			  //ese write the result to textfile
			  if(outputString.equals("")) {
				  textFile.write((outputString).getBytes());
			  }
			  else {
				  textFile.write((outputString+"\n").getBytes());
			  }
			  //textFile.write((outputString+"\n").getBytes());
			  //System.out.println(outputString);
		  }
	  }
  }
  
  public void close() {
	  //close all sockets when done
	  textFile.close();
	  try {
		  tcpSocket.close();
	  }
	  catch(IOException e) {
		  e.printStackTrace();
	  }
	  //close udp socket
	  udpSocket.close();
  }
}
