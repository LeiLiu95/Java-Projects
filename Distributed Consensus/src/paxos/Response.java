package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    // your data here
    //if it is accepted or not
    public boolean acceptBool;
    //variables needed are an index to self
    public int meIndex;
    //variable to done
    public int doneValue;
    //current seq number
    public int seqNum;
    //unique id value
    public int myUniqueId;
    //previous unique id
    public int lastAcceptedId;
    //the last accepted value
    public Object objectValue;
   
    // Your constructor and methods here
    
    //create an empty constructor for response
    public Response(){
    	//if a response then set it to true
    	this.acceptBool = true;
    }

    //variables needed for response without previous
    public Response(int seqNum, boolean acceptBool, int myUniqueId, int meIndex, int doneValue){
    	//set the variables needed and passed from parameters to object
    	this.meIndex = meIndex;
    	//set the done value
    	this.doneValue = doneValue;
    	//set the seq value
    	this.seqNum = seqNum;
    	//set unique id
    	this.myUniqueId = myUniqueId;
    	//set accepted value
    	this.acceptBool = acceptBool;
    }
    
    //variables needed for a prepare okay response
    public Response(int seqNum, boolean acceptBool, int myUniqueId, int lastAcceptedId, Object objectValue, int meIndex, int doneValue){
    	//setting the variables needed and passed for prepare okay response
    	//set the me index
    	this.meIndex = meIndex;
    	//set the done value of the class
    	this.doneValue = doneValue;
    	//set the seq value
    	this.seqNum = seqNum;
    	//set the unique id value
    	this.myUniqueId = myUniqueId;
    	//set the last accepted id
    	this.lastAcceptedId = lastAcceptedId;
    	//set the accepted boolean value
    	this.acceptBool = acceptBool;
    	//set the object value of class
    	this.objectValue = objectValue;
    }
}
