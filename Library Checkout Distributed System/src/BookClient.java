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
  
  
  private int udpPort;
  private int tcpPort;
  private String portType;
  private String hostName;
  private InetAddress hostIP;
  private byte[] buffer;
  private byte[] receiveBuffer;
  private DatagramPacket senderUDP;
  private DatagramPacket receiverUDP;
  private DatagramSocket udpSocket;
  private Socket tcpSocket;
  private Scanner dataIn;
  private PrintStream printOut;
  private PrintStream textFile;
  
  public BookClient(String hostName, int tcpPort, int udpPort, int clientID) throws IOException {
	  this.hostName = hostName;
	  this.hostIP = InetAddress.getByName(hostName);
	  this.udpPort = udpPort;
	  this.tcpPort = tcpPort;
	  
	  this.receiveBuffer = new byte [1024];
	  this.portType = UDP;
	  this.tcpSocket = new Socket(this.hostName, this.tcpPort);
	  this.udpSocket = new DatagramSocket();
	  this.dataIn = new Scanner(tcpSocket.getInputStream());
	  this.printOut = new PrintStream(tcpSocket.getOutputStream());
	  this.textFile = new PrintStream(new File("out_"+ clientID + ".txt"));
  }
  
  public void setMode(String connectionType) {
	  if(connectionType.equals("U")) {
		  this.portType = UDP;
	  }
	  else if(connectionType.equals("T")) {
		  this.portType = TCP;
	  }
  }
  
  public void sendCommand(String command) throws IOException{
	  buffer = new byte[command.length()];
	  buffer = command.getBytes();
	  String outputString = "";
	  if(portType.equals(UDP)) {
		  senderUDP = new DatagramPacket(buffer, buffer.length, hostIP, udpPort);
		  udpSocket.send(senderUDP);
		  receiverUDP = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		  udpSocket.receive(receiverUDP);
		  outputString = new String(receiverUDP.getData(), 0, receiverUDP.getLength());
		  
		  if(outputString.equals("")) {
			  textFile.write((outputString).getBytes());
		  }
		  else {
			  textFile.write((outputString+"\n").getBytes());
		  }
		  
	  }
	  else if (portType.equals(TCP)) {
		  printOut.println(command);
		  printOut.flush();
		  while(dataIn.hasNextLine()) {
			  outputString = dataIn.nextLine();
			  if(outputString.equals("finish")) {
				  break;
			  }
			  if(outputString.equals("")) {
				  textFile.write((outputString).getBytes());
			  }
			  else {
				  textFile.write((outputString+"\n").getBytes());
			  }
		  }
	  }
  }
  
  public void close() {
	  textFile.close();
	  try {
		  tcpSocket.close();
	  }
	  catch(IOException e) {
		  e.printStackTrace();
	  }
	  udpSocket.close();
  }
}
