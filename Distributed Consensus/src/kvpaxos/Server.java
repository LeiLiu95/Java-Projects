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

    int keyValue;
    int meIndex;
    ConcurrentHashMap <String, Integer> numOfRequestsMap;
    ConcurrentHashMap <String, Integer> keyValueStoreMap;

    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        this.keyValue = 0;
        this.meIndex = 0;
        this.numOfRequestsMap = new ConcurrentHashMap <String, Integer> ();
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
    	Op operationVariable;
    	operationVariable = Op.class.cast(px.Status(seqNum).v);
    	String opKeyValue;
    	opKeyValue = operationVariable.key;
    	for (int i = px.Min(); i < seqNum; i+=1){
    		operationVariable = wait(i);
    		if (operationVariable.op.equals ("Put")) {
    			keyValueStoreMap.put (operationVariable.key, operationVariable.value);
    		}
    		String[] clientIdValue;
    		clientIdValue = operationVariable.clientCallerId.split ("_");
    		String baseIdValue;
    		baseIdValue = clientIdValue[0];
    		int numberOfRequests = Integer.parseInt (clientIdValue[1]);
    		if (!numOfRequestsMap.contains(baseIdValue)){
    			numOfRequestsMap.put (baseIdValue, numberOfRequests);
    		}
    		else if (numOfRequestsMap.contains (baseIdValue) && numberOfRequests > numOfRequestsMap.get (baseIdValue)){
    			numOfRequestsMap.put (baseIdValue, numberOfRequests);
    			px.Done (i);
    		}
    	}
    	return keyValueStoreMap.get(opKeyValue);
    }
    
    //a wait function that returns the op in the map
    public Op wait(int seqVariable) {
    	int threadTimer;
    	threadTimer = 10;
        while (0==0) {
        	Paxos.retStatus returnStatus = this.px.Status (seqVariable);
        	if (State.Decided == returnStatus.state) {
        		return Op.class.cast (returnStatus.v);
        	}
        	try{
        		Thread.sleep (threadTimer);
        	}catch (Exception e){
        		e.printStackTrace ();
        	}
        	if (threadTimer < 1000){
        		threadTimer = threadTimer * 2;
        	}
        }
    }
    
    //function to return the seq value
    public int getDecidedSeqValue (String clientProcessId){
    	for (int seqIndex=px.Min (); seqIndex <= px.Max(); seqIndex+=1){
    		Paxos.retStatus returnStatus;
    		returnStatus = px.Status (seqIndex);
    		State seqState;
    		seqState = returnStatus.state;
    		Op operationVariable;
    		operationVariable = Op.class.cast (returnStatus.v);
    		if (seqState == State.Decided && operationVariable.clientCallerId.equals (clientProcessId)) {
    			return seqIndex;
    		}
    	}
    	return -1;
    }
    
    public Response Get(Request req){
        int seqIdValue;
        seqIdValue = getDecidedSeqValue (req.operation.clientCallerId);
        if (seqIdValue >= 0){
        	Response oldResponse = new Response (getSeqValue(seqIdValue));
        	return oldResponse;
        }
        seqIdValue = px.Max() + 1;
        px.Start (seqIdValue, req.operation);
        Op result;
        result = wait (seqIdValue);
        while (!result.clientCallerId.equals (req.operation.clientCallerId)){
        	seqIdValue = getDecidedSeqValue (req.operation.clientCallerId);
        	if (seqIdValue >= 0){
        		Response returnVar = new Response(seqIdValue);
        		return returnVar;
        	}
        	seqIdValue = px.Max() + 1;
        	px.Start (seqIdValue, req.operation);
        	result = wait (seqIdValue);
        }
        Response tempVar = new Response(getSeqValue(seqIdValue));
        return tempVar;
    }
    
    public Response Put(Request req){
        int seqValue;
        seqValue = getDecidedSeqValue (req.operation.clientCallerId);
        if (seqValue >= 0) {
        	Response tempVar = new Response();
        	return tempVar;
        }
        seqValue = px.Max () + 1;
        px.Start (seqValue, req.operation);

        Op opResult = wait (seqValue);

        while (!opResult.clientCallerId.equals (req.operation.clientCallerId)){
        	seqValue = getDecidedSeqValue (req.operation.clientCallerId);
        	if (seqValue >= 0) {
            	Response tempResponse = new Response();
            	return tempResponse;
        	}
        	seqValue = px.Max () + 1;
        	px.Start (seqValue, req.operation);
        	opResult = wait (seqValue);
        }
    	Response returnVar = new Response();
    	return returnVar;
    }
}
