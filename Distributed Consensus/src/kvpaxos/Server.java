package kvpaxos;
import paxos.Paxos;
import paxos.State;
// You are allowed to call Paxos.Status to check if agreement was made.

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements KVPaxosRMI {

    ReentrantLock mutex;
    Registry registry;
    Paxos px;
    int me;

    String[] servers;
    int[] ports;
    KVPaxosRMI stub;

    // Your definitions here
    int keyValue;
    //me index for server
    int meIndex;
    //map used to hold the number of reqeusts
    ConcurrentHashMap <String, Integer> numOfRequestsMap;
    //map used to hold keys anad value
    ConcurrentHashMap <String, Integer> keyValueStoreMap;

    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        // Your initialization code here
        //initialize int variables to 0
        this.keyValue = 0;
        //set me index
        this.meIndex = 0;
        //initialize the number of requests map
        this.numOfRequestsMap = new ConcurrentHashMap <String, Integer> ();
        //initialize the key and value store map
        this.keyValueStoreMap = new ConcurrentHashMap <String, Integer> ();

        try{
            System.setProperty("java.rmi.server.hostname", this.servers[this.me]);
            registry = LocateRegistry.getRegistry(this.ports[this.me]);
            stub = (KVPaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("KVPaxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    //function that gets tthe seq value 
    private int getSeqValue(int seqNum){
    	//create an op with the seqnum
    	Op operationVariable;
    	operationVariable = Op.class.cast(px.Status(seqNum).v);
    	//create a key for the operation variable
    	String opKeyValue;
    	opKeyValue = operationVariable.key;
    	//iterate through the seq with the min value of paxos
    	for (int i = px.Min(); i < seqNum; i+=1){
    		//set the operation variable to the wait of i
    		operationVariable = wait(i);
    		//if the op is equal to put then put the value into the key value map
    		if (operationVariable.op.equals ("Put")) {
    			//set the value into the key value hash map
    			keyValueStoreMap.put (operationVariable.key, operationVariable.value);
    		}
    		//create a clientid list
    		String[] clientIdValue;
    		clientIdValue = operationVariable.clientCallerId.split ("_");
    		//set the id to the client id index 0
    		String baseIdValue;
    		baseIdValue = clientIdValue[0];
    		//create an int that holds the number of requests
    		int numberOfRequests = Integer.parseInt (clientIdValue[1]);
    		//if not then check if the map contains the id value
    		if (!numOfRequestsMap.contains(baseIdValue)){
    			//then put the number of requests to the id value key
    			numOfRequestsMap.put (baseIdValue, numberOfRequests);
    		}
    		//if the map has the id and if the number of requests is bigger than the id value  from map then
    		else if (numOfRequestsMap.contains (baseIdValue) && numberOfRequests > numOfRequestsMap.get (baseIdValue)){
    			//put the number of requests to the id value key
    			numOfRequestsMap.put (baseIdValue, numberOfRequests);
    			//set the paxos index i to done
    			px.Done (i);
    		}
    	}
    	//return the value from the map with the op key index
    	return keyValueStoreMap.get(opKeyValue);
    }
    
    //a wait function that returns the op in the map
    public Op wait(int seqVariable) {
    	//create a trhead timer that is set to 10 initially
    	int threadTimer;
    	threadTimer = 10;
    	//while loop to wait until the conditions are met
        while (0==0) {
        	//create a return status that is the status of this seq
        	Paxos.retStatus returnStatus = this.px.Status (seqVariable);
        	//if the state of the status is decided then return the v variable
        	if (State.Decided == returnStatus.state) {
        		return Op.class.cast (returnStatus.v);
        	}
        	//if not then sleep the thread for a set amound of time
        	try{
        		Thread.sleep (threadTimer);
        	}catch (Exception e){
        		e.printStackTrace ();
        	}
        	//if the thread timer is to little then increase the timer of the thread
        	if (threadTimer < 1000){
        		//double the timer value
        		threadTimer = threadTimer * 2;
        	}
        }
    }
    
    //function to return the seq value
    public int getDecidedSeqValue (String clientProcessId){
    	// Loop through seen seq's looking for if the request was already fulfilled
    	//loop through the seq to check if a request was fulfilled until the max of paxos instance
    	for (int seqIndex=px.Min (); seqIndex <= px.Max(); seqIndex+=1){
    		//create a retstatus of the thread id seq
    		Paxos.retStatus returnStatus;
    		returnStatus = px.Status (seqIndex);
    		//set the state equal to the state of the return status
    		State seqState;
    		seqState = returnStatus.state;
    		//create an op variable of the return status
    		Op operationVariable;
    		operationVariable = Op.class.cast (returnStatus.v);
    		//if the seq state is decided and the client id value is correct then return the correct seq value
    		if (seqState == State.Decided && operationVariable.clientCallerId.equals (clientProcessId)) {
    			return seqIndex;
    		}
    	}
    	//if the seq is not found then return -1
    	return -1;
    }
    
    // RMI handlers
    public Response Get(Request req){
        // Your code here
//    	Response temp = null;
//    	return null;
    	
        //if the request is done then return the old result
        int seqIdValue;
        seqIdValue = getDecidedSeqValue (req.operation.clientCallerId);
        //if the seq value is non zero then create the old response and return it
        if (seqIdValue >= 0){
        	//create a response variable that will be returning the value from getSeqValue function call
        	Response oldResponse = new Response (getSeqValue(seqIdValue));
        	//return the resposne
        	return oldResponse;
        }
        //the new seq is needed to be found and a paxos instance will be needed
        //set the id value to the max +1
        seqIdValue = px.Max() + 1;
        //start the thread
        px.Start (seqIdValue, req.operation);
        //call the wait function to wait for a result to continue
        Op result;
        result = wait (seqIdValue);
        //loop until a paxos instance is accepted
        while (!result.clientCallerId.equals (req.operation.clientCallerId)){
        	//if the request is done then return the old value
        	//check the id value of the next decided value
        	seqIdValue = getDecidedSeqValue (req.operation.clientCallerId);
        	//if the value is non zero then return the old response
        	if (seqIdValue >= 0){
        		//return the seq id response value
        		Response returnVar = new Response(seqIdValue);
        		return returnVar;
        	}
        	//the new seq will need to be found
        	//set the seq id to the max added by 1
        	seqIdValue = px.Max() + 1;
        	//start the paxos instance
        	px.Start (seqIdValue, req.operation);
        	//call wait function to wait for result to continue
        	result = wait (seqIdValue);
        }
        //once done return the response value from the seqid
        Response tempVar = new Response(getSeqValue(seqIdValue));
        return tempVar;
    }
    
    public Response Put(Request req){
        // Your code here
    	//Response temp = null;
    	//return null;

    	//create an seq value to hold the decided seq value
        int seqValue;
        //set the seq value to the return value from the getDecidedSeqValue
        seqValue = getDecidedSeqValue (req.operation.clientCallerId);
        //if the value is non zero then return an empty response
        if (seqValue >= 0) {
        	//create and return an empty response
        	//create an empty response and return it from the method call
        	Response tempVar = new Response();
        	return tempVar;
        }
        //a new valid seq is needed and paxos needs to be started
        //set a new seq value
        //set the seq value to the paxos instance max incremented by 1
        seqValue = px.Max () + 1;
        //start the paxos 
        px.Start (seqValue, req.operation);

        //create an op from the wait function call with seq value
        Op opResult = wait (seqValue);

        //loop until the paxos decides on the current proposal
        while (!opResult.clientCallerId.equals (req.operation.clientCallerId)){
        	//if the request is done then return the previous result
        	//set seq value to the decided seq value
        	seqValue = getDecidedSeqValue (req.operation.clientCallerId);
        	//if the seq value is non zero return an empty resposne
        	if (seqValue >= 0) {
        		//create and return an empty response
        		//create an empty response variable and call the constructor to return from method call
            	Response tempResponse = new Response();
            	return tempResponse;
        	}
        	//a new seq is needed and paxos needs to be started
        	//set the new seq value to the paxos max incremented by 1
        	seqValue = px.Max () + 1;
        	//start the paxos instance
        	px.Start (seqValue, req.operation);
        	//set the opresult to the return of the wait function with the set seq value
        	opResult = wait (seqValue);
        }
        //if complete then return new response
        //create an empty temp response variable and return it
    	Response returnVar = new Response();
    	return returnVar;
    }
}
