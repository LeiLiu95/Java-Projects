package kvpaxos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Client {
    String[] servers;
    int[] ports;

    // Your data here
    //int variable to hold the request number
    int requestValue = 0;
    //string that holds the id number
    String baseIdNum;

    public Client(String[] servers, int[] ports){
        this.servers = servers;
        this.ports = ports;
        // Your initialization code here
        //the id number will be set to the time of system
        this.baseIdNum = Long.toString (System.nanoTime ()) + '_';
    }

    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;
        KVPaxosRMI stub;
        try{
            Registry registry= LocateRegistry.getRegistry(this.ports[id]);
            stub=(KVPaxosRMI) registry.lookup("KVPaxos");
            if(rmi.equals("Get"))
                callReply = stub.Get(req);
            else if(rmi.equals("Put")){
                callReply = stub.Put(req);}
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }
    
    
    //function to grab the next server id
    int getNextServerIdValue (int serverIdValue){
    	//create an int value that is the server id value modded by the length of the servers list
    	int returnValue;
    	//mode server id by length
    	returnValue = (serverIdValue + 1) %  servers.length;
      	return returnValue;
    }

    // RMI handlers
    public Integer Get(String key){
        // Your code here
    	//return 0;
    	//create a string to hold the id value and time
        String clientIdNum = baseIdNum + Integer.toString (requestValue++);
        //create a new operation and pass the necessary values for a get operation
        Op operationValue;
        operationValue = new Op ("Get", clientIdNum, key, null);
        //create a new request variable with the created op variable
        Request getRequest;
        getRequest = new Request (operationValue);

        //set the initial value of server to 0
        int serverIdValue = 0;
        //get a response from the call function
        Response getResponse;
        getResponse = Call ("Get", getRequest, serverIdValue);
        //set a counter for 0 for iteration
        int indexCounter = 0;
        while (getResponse == null){
        	//check if the counter is reater than 1
        	if(indexCounter++ > 1) {
        		//if it is then return null for get function
        		return null;
        	}
        	//set the server id to the next id value
        	serverIdValue = getNextServerIdValue (serverIdValue);
        	//set the response to the return of the call function with get passed
        	getResponse = Call ("Get", getRequest, serverIdValue);
        }
        //return the value from the final get response
        return getResponse.intValue;
    }
    
    public boolean Put(String key, Integer value){
        // Your code here
    	//return false;
    	
    	//create a string that holds the value of the client 
        String clientIdValue = baseIdNum + Integer.toString (requestValue++);
        //get the op value with the put parameter passed into Op constructor
        Op operationValue;
        operationValue = new Op ("Put", clientIdValue, key, value);
        //create a request with the operation passed
        Request putRequest;
        putRequest = new Request (operationValue);
        //create a server id value holder
        int serverIdValue = 0;
        //create response from the call type to iterate through
        Response putResult;
        putResult = Call ("Put", putRequest, serverIdValue);
        //iterate through until there are no more responses for call put
        while (putResult == null){
        	//set the server id value holder to the next server id value
        	serverIdValue = getNextServerIdValue (serverIdValue);
        	//call put with the put parameter and an incremented server id value
        	putResult = Call ("Put", putRequest, serverIdValue);
        }
        //after completing the put, return true
        return true;
    }
}
