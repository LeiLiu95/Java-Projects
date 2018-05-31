import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPThread extends Thread {
	//variable for library of books for tcp
    private Library inventory;
    //scanner for data input
    private Scanner dataIn;
    //print writer to output
    private PrintWriter printerOut;
    //socket used for tcp
    private Socket tcpSocket;
    
    public TCPThread(Library library, Socket socket) throws IOException {
        //set inventory of library
    	this.inventory = library;
    	//set the socket for tcp
        this.tcpSocket = socket;
        //set the input and output variables
        this.dataIn = new Scanner(tcpSocket.getInputStream());
        //set the printstream to the proper output
        this.printerOut = new PrintWriter(tcpSocket.getOutputStream());
    }

    public void run() {
    	//create a string for commands
        String command;
        //try tcp
        try {
            //while there are variables or client is opened
            while (dataIn.hasNextLine()) {
                //read input
                command = dataIn.nextLine();
                //set the string to the commands
                String resultString = inventory.getCommand(command);
                //send the response to system
                printerOut.println(resultString);
                //print done 
                printerOut.println("finish");
                //flush the output
                printerOut.flush();
            }
            //close the socket
            tcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}