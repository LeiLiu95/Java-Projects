import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPThread extends Thread {
	private byte[] buffer;
    private Library inventory;
    private DatagramPacket dataPacket;
    private DatagramSocket dataSocket;
    
    //udp thread will need the library and the port
    public UDPThread(Library library, int udpPort) throws SocketException {
        this.inventory = library;
        dataSocket = new DatagramSocket(udpPort);
        this.buffer = new byte[1024];
    }

    public void run() {
        try {
            while (true) {
                this.buffer = new byte[1024];
                dataPacket = new DatagramPacket(buffer, buffer.length);
                dataSocket.receive(dataPacket);
                Thread tempThread = new UDPHandlerThread(inventory,dataSocket,new String(buffer).trim(),dataPacket);
                tempThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
