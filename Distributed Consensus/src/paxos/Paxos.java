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
    	int uniqueId = 0;
    	int lastAcceptReqId = -1;
    	boolean decided = false;

    	Object lastAcceptValue = null;
    	Object valueObject = null;
    	
    	PaxosInstanceAgreement(){
    	}
    	
    	//constructor with a passed object
    	PaxosInstanceAgreement (Object value){
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
        for (int i=0; i<doneSeqs.length; i+=1) {
        	doneSeqs[i] = -1;
        }
        threadPool = Executors.newWorkStealingPool();
        seqDeque = new ConcurrentLinkedDeque<>();
        instanceMap = new ConcurrentSkipListMap<Integer, PaxosInstanceAgreement> ();
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
    	
        seqDeque.add(seq);
        instanceMap.put (seq, new PaxosInstanceAgreement (value));
        threadPool.execute(new Thread(this));    	
    }
    
    //function to check majority of threads
    boolean getMajorityValue (String callerType, Request requestValue, PaxosCaller[] callerList, Thread[] threadList, PaxosInstanceAgreement agreementInstance){
    	ExecutorService callerPool = Executors.newWorkStealingPool();
    	for (int i=0; i<peers.length; i+=1){
    		callerList[i] = new PaxosCaller (callerType, requestValue, i);
    		threadList[i] = new Thread (callerList[i]);
    		callerPool.execute(callerList[i]);
    	}
    	int numberAccepted = 0;
    	callerPool.shutdown();
    	
    	try{
    		callerPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    	for (int i=0; i<peers.length; i+=1){
    		if (callerList[i].responseValue == null) {
    			continue;
    		}
    		else if (callerList[i].responseValue.acceptBool) {
    			numberAccepted+=1;
    		}
    		else {
    			agreementInstance.uniqueId = callerList[i].responseValue.myUniqueId;
    		}
    	}
    	boolean check;
    	check = numberAccepted > peers.length / 2;
    	return check;
    }
    
    
    //function to create and set and isntance in the map
    public PaxosInstanceAgreement createResponse(int seq) {
    	PaxosInstanceAgreement instanceResponse = new PaxosInstanceAgreement();
    	instanceMap.put(seq, instanceResponse);
    	return instanceResponse;
    }
    
    //function to forget old data from algorithm
    private void removeOldInstances(){
    	int min = Min();
    	while (instanceMap.firstKey() < min){
    		instanceMap.remove(instanceMap.firstKey());
    	}
    }
    
    //caller class for thread and implements the runnable interface
    class PaxosCaller implements Runnable{
    	int serverIdCaller;
    	String callerType;
    	Request requestValue;
    	Response responseValue = null;
    	
    	//empty constructor
    	PaxosCaller(){
    	}
    	//constructor method for paxos caller
    	PaxosCaller (String callerType, Request requestValue, int serverId){
    		this.serverIdCaller = serverId;
    		this.callerType = callerType;
    		this.requestValue = requestValue;
    	}
    	
    	//run function for the caller class
    	public void run (){
        	if (serverIdCaller == me){
        		if (callerType == "Accept") {
        			responseValue = Accept(requestValue);
        		}
        		else if(callerType == "Prepare") {
        			responseValue = Prepare(requestValue);
        		}
        		else {
        			responseValue = Decide(requestValue);
        		}
        	}
        	else {
        		responseValue = Call(callerType, requestValue, serverIdCaller);
        	}
        }
    }
    
    @Override
    public void run(){
        int seqValue = seqDeque.pollFirst();
        PaxosInstanceAgreement instanceVar = instanceMap.get (seqValue);
        while (!isDead() && !instanceVar.decided){
        	int requiredUniqueID;
        	requiredUniqueID = (instanceVar.uniqueId / peers.length + 1) * peers.length + me;
        	Request proposalRequest;
        	proposalRequest = new Request(seqValue, requiredUniqueID);

        	Thread callThreadsList[] = new Thread[peers.length];
        	PaxosCaller callerList[] = new PaxosCaller[peers.length];       	

        	//if there is no majority then skip over this iteration
        	if (!getMajorityValue ("Prepare", proposalRequest, callerList, callThreadsList, instanceVar)) {
        		continue;
        	}

        	int maxAcceptRequiredUniqueID = -1;
        	//iterate through the callers list
        	for (PaxosCaller paxosCallerI : callerList){
        		if (paxosCallerI.responseValue == null || !paxosCallerI.responseValue.acceptBool) {
        			continue;
        		}
        		//check and set most recent accepted proposals of callers
        		if (paxosCallerI.responseValue.lastAcceptedId > maxAcceptRequiredUniqueID){
        			instanceVar.valueObject = paxosCallerI.responseValue.objectValue;
        			maxAcceptRequiredUniqueID = paxosCallerI.responseValue.lastAcceptedId;
        		}

            	doneSeqs[paxosCallerI.responseValue.meIndex] = paxosCallerI.responseValue.doneValue;
            	removeOldInstances();
        	}
        	
        	Request acceptRequest;
        	acceptRequest = new Request (seqValue, requiredUniqueID, instanceVar.valueObject);
        	if (!getMajorityValue ("Accept", acceptRequest, callerList, callThreadsList, instanceVar)) {
        		continue;
        	}
        	Request decideRequest;
        	decideRequest= new Request (seqValue, instanceVar.valueObject, me, doneSeqs[me]);
        	getMajorityValue("Decide", decideRequest, callerList, callThreadsList, instanceVar);
        }
        if (isDead()) {
        	threadPool.shutdownNow();
        }
    }
    
    // RMI handler
    public Response Prepare(Request req){
        PaxosInstanceAgreement instanceResponse;
        instanceResponse = instanceMap.get (req.seqNum);
        if (instanceResponse == null){
        	instanceResponse = createResponse(req.seqNum);
        }
        boolean isAccepted;
        isAccepted = (req.uniqueId > instanceResponse.uniqueId);
        if (isAccepted) {
          instanceResponse.uniqueId = req.uniqueId;
        }
        Response returnResponse = new Response(req.seqNum, isAccepted, instanceResponse.uniqueId, instanceResponse.lastAcceptReqId, instanceResponse.lastAcceptValue, me, doneSeqs[me]);
        return returnResponse;
    }

    public Response Accept(Request req){
        PaxosInstanceAgreement instanceResponse;
        instanceResponse = instanceMap.get (req.seqNum);
        if (instanceResponse == null){
        	instanceResponse = createResponse(req.seqNum);
        }
        boolean isAccepted;
        isAccepted = (req.uniqueId >= instanceResponse.uniqueId);
        if (isAccepted){
        	instanceResponse.uniqueId = req.uniqueId;
        	instanceResponse.lastAcceptReqId = req.uniqueId;
        	instanceResponse.lastAcceptValue = req.objectValue;
        }
        Response returnResponse = new Response(req.seqNum, isAccepted, instanceResponse.uniqueId,me, doneSeqs[me]);
        return returnResponse;
    }

    public Response Decide(Request req){
    	PaxosInstanceAgreement instanceResponse;
    	instanceResponse = instanceMap.get (req.seqNum);
        if (instanceResponse == null){
        	instanceResponse = createResponse(req.seqNum);
        }
        instanceResponse.decided = true;
        instanceResponse.valueObject = req.objectValue;
        doneSeqs[req.meIndex] = req.doneValue;
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
    	int min = Integer.MAX_VALUE;
        for (int i = 0; i<doneSeqs.length; i+=1){
        	if (doneSeqs[i] < min) {
        		min = doneSeqs[i];
        	}
        }
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
    	if (seq < Min()){
    		retStatus temp = new retStatus(State.Forgotten, null);
    		return temp;
        }
        PaxosInstanceAgreement instanceResponse;
        instanceResponse = instanceMap.get(seq);
        if (instanceResponse == null){
        	instanceResponse = createResponse(seq);      	
        }
        State state;
        if (instanceResponse.decided) {
        	state = State.Decided;
        }
        else {
        	state = State.Pending;
        }
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
