package kvpaxos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Client {
    String[] servers;
    int[] ports;

    int requestValue = 0;
    String baseIdNum;

    public Client(String[] servers, int[] ports){
        this.servers = servers;
        this.ports = ports;
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
    	int returnValue;
    	returnValue = (serverIdValue + 1) %  servers.length;
      	return returnValue;
    }

    // RMI handlers
    public Integer Get(String key){
        String clientIdNum = baseIdNum + Integer.toString (requestValue++);
        Op operationValue;
        operationValue = new Op ("Get", clientIdNum, key, null);
        Request getRequest;
        getRequest = new Request (operationValue);

        int serverIdValue = 0;
        Response getResponse;
        getResponse = Call ("Get", getRequest, serverIdValue);
        int indexCounter = 0;
        while (getResponse == null){
        	if(indexCounter++ > 1) {
        		return null;
        	}
        	serverIdValue = getNextServerIdValue (serverIdValue);
        	getResponse = Call ("Get", getRequest, serverIdValue);
        }
        return getResponse.intValue;
    }
    
    public boolean Put(String key, Integer value){
        String clientIdValue = baseIdNum + Integer.toString (requestValue++);
        Op operationValue;
        operationValue = new Op ("Put", clientIdValue, key, value);
        Request putRequest;
        putRequest = new Request (operationValue);
        int serverIdValue = 0;
        Response putResult;
        putResult = Call ("Put", putRequest, serverIdValue);
        while (putResult == null){
        	serverIdValue = getNextServerIdValue (serverIdValue);
        	putResult = Call ("Put", putRequest, serverIdValue);
        }
        return true;
    }
}
