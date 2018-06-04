import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPHandlerThread extends Thread {
    private byte[] returnBuffer;
    private Library inventory;
    private DatagramPacket udpDataPacket;
    private DatagramPacket udpReturnPacket;
    private DatagramSocket udpDataSocket;
    private String commands;
    
    public UDPHandlerThread(Library inventory, DatagramSocket datagramSocket, String command, DatagramPacket datagramPacket) throws SocketException {
    	this.inventory = inventory;
        this.udpDataSocket = datagramSocket;
        this.commands = command;
        this.udpDataPacket = datagramPacket;
    }

    public void run() {
        try {
            String response = inventory.getCommand(commands);
            returnBuffer = response.getBytes();
            udpReturnPacket = new DatagramPacket(returnBuffer,returnBuffer.length,udpDataPacket.getAddress(),udpDataPacket.getPort());
            udpDataSocket.send(udpReturnPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}