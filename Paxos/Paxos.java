package paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
//    int maxSeqSeen;
//    ArrayList<Integer> doneSeqs;
//    HashMap<Integer, Object> values;
//    HashMap<Integer, StateP> accpState;
    //create a done counter at -1 initially
    int doneCounter = -1;
    //create ann int array of the done seqs
    int[] doneSeqs;
    //create a thread pool executor service
    final ExecutorService threadPool;
    //create a deque for seq
    ConcurrentLinkedDeque<Integer> seqDeque;
    //create a hashmap for instaces
    ConcurrentSkipListMap<Integer, PaxosInstanceAgreement> instanceMap;
    
    //class that is used for agreement instances
    class PaxosInstanceAgreement{
    	//shared value id
    	int uniqueId = 0;
    	//set the last accepted id to be -1
    	int lastAcceptReqId = -1;
    	//learner variabled that shows if the instance is decided or not
    	boolean decided = false;
    	//acceptor variables that is needed 

    	//initialize the last value to be null
    	Object lastAcceptValue = null;
    	//variable for proposer that is initialized as null
    	Object valueObject = null;
    	
    	//constructors for agreement instance class
    	//empty constructor method
    	PaxosInstanceAgreement(){
    	}
    	
    	//constructor with a passed object
    	PaxosInstanceAgreement (Object value){
    		//set the object to the passed parameter
    		this.valueObject = value;
    	}	
    }
    
    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
//        doneSeqs = new ArrayList<Integer>();
//        values = new HashMap<Integer , Object>();
//        accpState = new HashMap<Integer, StateP>();
        doneSeqs = new int[peers.length];
        //iterate through the new array and set all indexes to -1
        for (int i=0; i<doneSeqs.length; i+=1) {
        	//set the value in doneseq to -1
        	doneSeqs[i] = -1;
        }
        //create a new thread pool
        threadPool = Executors.newWorkStealingPool();
        //initialize a new deque
        seqDeque = new ConcurrentLinkedDeque<>();
        //create a new mape and deque using the creation calls
        instanceMap = new ConcurrentSkipListMap<Integer, PaxosInstanceAgreement> ();
        
        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
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

        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value){
        // Your code here
    	
    	//add the seq to the deque
        seqDeque.add(seq);
        //create an object in the map for the value
        instanceMap.put (seq, new PaxosInstanceAgreement (value));
        //run the thread
        threadPool.execute(new Thread(this));    	
        
//    	//Paxos px = new Paxos(seq, value, value);
//    	//if seq is less then the minimum then exit
//    	if (seq < this.Min()) {
//    		return;
//    	}
//    	//else lock and update the maximum seen value
//    	this.mutex.lock();
//    	this.updateMaxSeqSeen(seq);
//    	this.mutex.unlock();
//    	//this.propose(seq, value);
//    	//propose the seq and value
//    	this.propose(seq, value);
    }
    
    //function to check majority of threads
    boolean getMajorityValue (String callerType, Request requestValue, PaxosCaller[] callerList, Thread[] threadList, PaxosInstanceAgreement agreementInstance){
    	//create a servicee for the thread pool
    	ExecutorService callerPool = Executors.newWorkStealingPool();
    	//iterate through the lists to create new caller and thread for each
    	for (int i=0; i<peers.length; i+=1){
    		//create new call with the caller type, request and index
    		callerList[i] = new PaxosCaller (callerType, requestValue, i);
    		//create a new thread with the index in the caller list
    		threadList[i] = new Thread (callerList[i]);
    		//run the thread in the pool
    		callerPool.execute(callerList[i]);
    	}
    	//number of accepted
    	int numberAccepted = 0;
    	//shut down the thread pool
    	callerPool.shutdown();
    	
    	try{
    		//wait for thread
    		callerPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    	}catch (Exception e){
    		//catch any errors
    		e.printStackTrace();
    	}
    	//iterate through caller list to check if anything was accepted
    	for (int i=0; i<peers.length; i+=1){
    		//if it is empty then skip
    		if (callerList[i].responseValue == null) {
    			continue;
    		}
    		//if it is accepted then increment the number of accepted
    		else if (callerList[i].responseValue.acceptBool) {
    			numberAccepted+=1;
    		}
    		//otherwise set the id to the new response id
    		else {
    			//set the unique id of instance
    			agreementInstance.uniqueId = callerList[i].responseValue.myUniqueId;
    		}
    	}
    	//return if the numbers of accepted threads is greater than halfs
    	boolean check;
    	//set check value and return
    	check = numberAccepted > peers.length / 2;
    	return check;
    }
    
    
    //function to create and set and isntance in the map
    public PaxosInstanceAgreement createResponse(int seq) {
    	//create a new instance
    	PaxosInstanceAgreement instanceResponse = new PaxosInstanceAgreement();
    	//put the instance into the map
    	instanceMap.put(seq, instanceResponse);
    	//return the instance to the caller
    	return instanceResponse;
    }
    
    //function to forget old data from algorithm
    private void removeOldInstances(){
    	//create a min value to hold the minimal value from function call
    	int min = Min();
    	//iterate through until all keys less than min is removed
    	while (instanceMap.firstKey() < min){
    		//if key is larger than min then remove
    		instanceMap.remove(instanceMap.firstKey());
    	}
    }
    
    //caller class for thread and implements the runnable interface
    class PaxosCaller implements Runnable{
    	//variable to hold the id of server
    	int serverIdCaller;
    	//string for the kind of caller
    	String callerType;
    	//request and response variables
    	Request requestValue;
    	//set the response value to null
    	Response responseValue = null;
    	
    	//empty constructor
    	PaxosCaller(){
    	}
    	//constructor method for paxos caller
    	PaxosCaller (String callerType, Request requestValue, int serverId){
    		//set the caller, request and server for caller object
    		this.serverIdCaller = serverId;
    		//set the caller type of class
    		this.callerType = callerType;
    		//set the request value of class type
    		this.requestValue = requestValue;
    	}
    	
    	//run function for the caller class
    	public void run (){
    		//if the serverId is equal to itself
        	if (serverIdCaller == me){
        		//if the caller type is accept then
        		if (callerType == "Accept") {
        			//if it is accept call accept function
        			responseValue = Accept(requestValue);
        		}
        		//if it is in prepare, accept or done
        		else if(callerType == "Prepare") {
        			//passed the value to prepare
        			responseValue = Prepare(requestValue);
        		}
        		else {
        			//if it is decide then send it to decide function
        			responseValue = Decide(requestValue);
        		}
        	}
        	//if it is not equal to itself then use the call function
        	else {
        		//set the response as the return from call function
        		responseValue = Call(callerType, requestValue, serverIdCaller);
        	}
        }
    }
    
    @Override
    public void run(){
    	//set the seq value from the deque
        int seqValue = seqDeque.pollFirst();
        //set the context of the seq in the instance map
        PaxosInstanceAgreement instanceVar = instanceMap.get (seqValue);
        //iterate through until instance is decided or dead
        while (!isDead() && !instanceVar.decided){
        	//create a variable for the next required id
        	int requiredUniqueID;
        	//findd the required id from the function
        	requiredUniqueID = (instanceVar.uniqueId / peers.length + 1) * peers.length + me;
        	//prep the proposal requrest
        	Request proposalRequest;
        	//set request to a new request with the seq value
        	proposalRequest = new Request(seqValue, requiredUniqueID);

        	//track proposals with two arrays of the size of peers
        	Thread callThreadsList[] = new Thread[peers.length];
        	//caller list of paxos
        	PaxosCaller callerList[] = new PaxosCaller[peers.length];       	

        	//if there is no majority then skip over this iteration
        	if (!getMajorityValue ("Prepare", proposalRequest, callerList, callThreadsList, instanceVar)) {
        		continue;
        	}

        	//check old values of accepted Match the value of any old accepted proposals.
        	int maxAcceptRequiredUniqueID = -1;
        	//iterate through the callers list
        	for (PaxosCaller paxosCallerI : callerList){
        		//if the response of the call is null or not accepted then pass iteration
        		if (paxosCallerI.responseValue == null || !paxosCallerI.responseValue.acceptBool) {
        			continue;
        		}
        		//check and set most recent accepted proposals of callers
        		if (paxosCallerI.responseValue.lastAcceptedId > maxAcceptRequiredUniqueID){
        			//if max is less than previous unique then set the instance variable to the last caller value
        			instanceVar.valueObject = paxosCallerI.responseValue.objectValue;
        			//set the max id to the last id
        			maxAcceptRequiredUniqueID = paxosCallerI.responseValue.lastAcceptedId;
        		}

        		//set the done seq array index to the current caller done
            	doneSeqs[paxosCallerI.responseValue.meIndex] = paxosCallerI.responseValue.doneValue;
            	//remove old data
            	removeOldInstances();
        	}
        	
        	//create and send accept requests
        	Request acceptRequest;
        	//create a new request
        	acceptRequest = new Request (seqValue, requiredUniqueID, instanceVar.valueObject);
        	//if there is not a majority then pass iteration
        	if (!getMajorityValue ("Accept", acceptRequest, callerList, callThreadsList, instanceVar)) {
        		continue;
        	}
        	//create and send decide request
        	Request decideRequest;
        	decideRequest= new Request (seqValue, instanceVar.valueObject, me, doneSeqs[me]);
        	//check majority
        	getMajorityValue("Decide", decideRequest, callerList, callThreadsList, instanceVar);
        }
        //if status is dead then close the pool
        if (isDead()) {
        	threadPool.shutdownNow();
        }
    }
    
    // RMI handler
    public Response Prepare(Request req){
        // your code here
    	//instance variable used to hold the value from the map
        PaxosInstanceAgreement instanceResponse;
        //grab the instance at the index of the seq of passed req
        instanceResponse = instanceMap.get (req.seqNum);
        //if the instance is null then create one
        if (instanceResponse == null){
        	//create an instance and set it in the map
        	instanceResponse = createResponse(req.seqNum);
//        	instanceResponse = new AgreementInstance();
//        	instanceMap.put(req.seq, instanceResponse);
        }
        //create a boolean to see if the request is accepted
        boolean isAccepted;
        //set the boolean value
        isAccepted = (req.uniqueId > instanceResponse.uniqueId);
        //if the instance is accepted then change the unique id
        if (isAccepted) {
        	//set unique id of the response
          instanceResponse.uniqueId = req.uniqueId;
        }
        //then return a new response with the following variables
        Response returnResponse = new Response(req.seqNum, isAccepted, instanceResponse.uniqueId, instanceResponse.lastAcceptReqId, instanceResponse.lastAcceptValue, me, doneSeqs[me]);
        return returnResponse;
    }

    public Response Accept(Request req){
        // your code here
    	//instance variable used to hold the value from the map
        PaxosInstanceAgreement instanceResponse;
        instanceResponse = instanceMap.get (req.seqNum);
        //if the instance is null then create a new instance
        if (instanceResponse == null){
        	//create a new instance and set it iin the map
        	instanceResponse = createResponse(req.seqNum);
//        	instanceResponse = new AgreementInstance();
//        	instanceMap.put(req.seq, instanceResponse);
        }
        //create boolean value for if the instance is accepted
        boolean isAccepted;
        //set the boolean variable
        isAccepted = (req.uniqueId >= instanceResponse.uniqueId);
        //if the instance is accepted then change the values
        if (isAccepted){
        	//set the instance unique id to the request id
        	instanceResponse.uniqueId = req.uniqueId;
        	//set the id to the request id
        	instanceResponse.lastAcceptReqId = req.uniqueId;
        	//set the last accepted value to the pass value
        	instanceResponse.lastAcceptValue = req.objectValue;
        }
        //create a new response with the parameters and return it
        Response returnResponse = new Response(req.seqNum, isAccepted, instanceResponse.uniqueId,me, doneSeqs[me]);
        //return the constructed response
        return returnResponse;
    }

    public Response Decide(Request req){
        // your code here
    	//instance variable used to hold the value from the map
    	PaxosInstanceAgreement instanceResponse;
    	//set the isntance response variable
    	instanceResponse = instanceMap.get (req.seqNum);
    	//if the instance is null then create a new instance
        if (instanceResponse == null){
        	//call response method and create it
        	instanceResponse = createResponse(req.seqNum);
//        	instanceResponse = new AgreementInstance();
//        	instanceMap.put(req.seq, instanceResponse);
        }
        //set the instance decided value to true
        instanceResponse.decided = true;
        //set the valueobject variable to the value pass
        instanceResponse.valueObject = req.objectValue;
        //set the doneseqs index to the request done value
        doneSeqs[req.meIndex] = req.doneValue;
        //return a new response
        return new Response();
    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
    	//set the doneseqs index of me to the passed int seq
    	doneSeqs[me] = seq;
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        // Your code here
    	//if the map is empty then return -1
    	if (instanceMap.isEmpty()) {
    		return -1;
    	}
    	//else return the last key in the map
  	    return instanceMap.lastKey();
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        // Your code here
    	//create a min variable and set it to the maximum number
    	int min = Integer.MAX_VALUE;
    	//itereate through the doneseqs
        for (int i = 0; i<doneSeqs.length; i+=1){
        	//if the value in the array index is less than the minimum value 
        	if (doneSeqs[i] < min) {
        		//then change the min value of the loop
        		min = doneSeqs[i];
        	}
        }
        //return the minimal number from doneseqs + 1
        return min + 1;
    }
    
    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        // Your code here
    	//if the seq is less than the min then
    	if (seq < Min()){
    		//return new retstatus with null
    		retStatus temp = new retStatus(State.Forgotten, null);
    		return temp;
        }
    	//create instance to hold value from map
        PaxosInstanceAgreement instanceResponse;
        //set the instance respoonse variable
        instanceResponse = instanceMap.get(seq);
        //if the instance is null then create a new instance
        if (instanceResponse == null){
        	//set the instance by calling the create response method
        	instanceResponse = createResponse(seq);      	
//        	instanceResponse = new AgreementInstance();
//        	instanceMap.put(seq, instanceResponse);
        }
        //create a state variable 
        State state;
        //if the instance is decided then set the decided state
        if (instanceResponse.decided) {
        	//set state to decided
        	state = State.Decided;
        }
        //else set the state to pending
        else {
        	//set state to pending
        	state = State.Pending;
        }
        //create a new status with the state and return
        retStatus returnValue = new retStatus(state, instanceResponse.valueObject);
        return returnValue;
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    public class StateP{
    	int prepProposal;
    	int accpProposal;
    	Object accpValue;
    }
    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }
}
