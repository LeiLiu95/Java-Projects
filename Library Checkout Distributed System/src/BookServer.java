import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BookServer {
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    Library inventory = new Library(fileName);
    // TODO: handle request from clients
    try {
    	Thread udpThread = new UDPThread(inventory, udpPort);
    	udpThread.start();
    	ServerSocket tcpServer = new ServerSocket(tcpPort);
    	Socket tcpSocket;
    	while((tcpSocket = tcpServer.accept()) != null) {
    		Thread tcpThread = new TCPThread(inventory, tcpSocket);
    		tcpThread.start();
    	}
    }
    catch(IOException e) {
    	e.printStackTrace();
    }
  }
}
