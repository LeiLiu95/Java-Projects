import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPHandlerThread extends Thread {
	//create a buffer foor the return data
    private byte[] returnBuffer;
    //create the library that will be used for the udp thread
    private Library inventory;
    //create datagram packet for udp send and returns
    private DatagramPacket udpDataPacket;
    private DatagramPacket udpReturnPacket;
    //create a datagram socket for data to be transferred
    private DatagramSocket udpDataSocket;
    //string for the input of commands
    private String commands;
    
    public UDPHandlerThread(Library inventory, DatagramSocket datagramSocket, String command, DatagramPacket datagramPacket) throws SocketException {
        //set the library inventory
    	this.inventory = inventory;
    	//set the data socket
        this.udpDataSocket = datagramSocket;
        //set the commands for the thread to the passed parameter
        this.commands = command;
        //set the datapack to the proper datagram from the parameter passed
        this.udpDataPacket = datagramPacket;
    }

    public void run() {
        try {
            //receive correct respone for command
            String response = inventory.getCommand(commands);
            //send to client
            returnBuffer = response.getBytes();
            //set the return packet to the datapacket
            udpReturnPacket = new DatagramPacket(returnBuffer,returnBuffer.length,udpDataPacket.getAddress(),udpDataPacket.getPort());
            //send the data socket
            udpDataSocket.send(udpReturnPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}