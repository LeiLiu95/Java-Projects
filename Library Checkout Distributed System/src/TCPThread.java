import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPThread extends Thread {
    private Library inventory;
    private Scanner dataIn;
    private PrintWriter printerOut;
    private Socket tcpSocket;
    
    public TCPThread(Library library, Socket socket) throws IOException {
    	this.inventory = library;
        this.tcpSocket = socket;
        this.dataIn = new Scanner(tcpSocket.getInputStream());
        this.printerOut = new PrintWriter(tcpSocket.getOutputStream());
    }

    public void run() {
        String command;
        try {
            while (dataIn.hasNextLine()) {
                command = dataIn.nextLine();
                String resultString = inventory.getCommand(command);
                printerOut.println(resultString);
                printerOut.println("finish");
                printerOut.flush();
            }
            tcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}