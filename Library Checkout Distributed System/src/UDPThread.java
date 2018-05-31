import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPThread extends Thread {
    //byte array for buffer
	private byte[] buffer;
	//library used for udp for books
    private Library inventory;
    //datagram packet to send data
    private DatagramPacket dataPacket;
    //datagram socket for data
    private DatagramSocket dataSocket;
    
    //udp thread will need the library and the port
    public UDPThread(Library library, int udpPort) throws SocketException {
    	//set the inventory to library
        this.inventory = library;
        //set the socket to the udpport
        dataSocket = new DatagramSocket(udpPort);
        //set the buffer to a new array
        this.buffer = new byte[1024];
    }

    public void run() {
        try {
            //loop until the server is closed
            while (true) {
                //continuously acceptt packets
                this.buffer = new byte[1024];
                //set packet to a new datagrampacket
                dataPacket = new DatagramPacket(buffer, buffer.length);
                //set the data soccket ot the receive of packett
                dataSocket.receive(dataPacket);
                //create a new thread to handle the packet
                Thread tempThread = new UDPHandlerThread(inventory,dataSocket,new String(buffer).trim(),dataPacket);
                //start the thread
                tempThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
