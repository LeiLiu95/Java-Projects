package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    public boolean acceptBool;
    public int meIndex;
    public int doneValue;
    public int seqNum;
    public int myUniqueId;
    public int lastAcceptedId;
    public Object objectValue;
   
    //create an empty constructor for response
    public Response(){
    	this.acceptBool = true;
    }

    //variables needed for response without previous
    public Response(int seqNum, boolean acceptBool, int myUniqueId, int meIndex, int doneValue){
    	this.meIndex = meIndex;
    	this.doneValue = doneValue;
    	this.seqNum = seqNum;
    	this.myUniqueId = myUniqueId;
    	this.acceptBool = acceptBool;
    }
    
    //variables needed for a prepare okay response
    public Response(int seqNum, boolean acceptBool, int myUniqueId, int lastAcceptedId, Object objectValue, int meIndex, int doneValue){
    	this.meIndex = meIndex;
    	this.doneValue = doneValue;
    	this.seqNum = seqNum;
    	this.myUniqueId = myUniqueId;
    	this.lastAcceptedId = lastAcceptedId;
    	this.acceptBool = acceptBool;
    	this.objectValue = objectValue;
    }
}
