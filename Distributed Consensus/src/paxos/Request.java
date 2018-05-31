package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: You may need the sequence number for each paxos instance and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
	static final long serialVersionUID=1L;
    // Your data here
    //sequence nnumber is needed
	public int seqNum;
    //the index to current 
    public int meIndex;
    //int that shows done
    public int doneValue;
	//id needed that is higher than previous
    public int uniqueId;
    //object that holds a value or object
    public Object objectValue;

    // Your constructor and methods here

    //empty constructor
    public Request() {
    	
    }
    
    //when preparing a request, the higher id is needed and the sequence
    public Request(int seqVariable, int uniqueId){
    	//set the classes values to the passed parameters
    	this.seqNum = seqVariable;
    	//set the unique id
    	this.uniqueId = uniqueId;
    }

    //when preparing an accept requests, the higher id and the value is needed
    public Request(int seqVariable, int uniqueId, Object objectValue){
    	//set the classes values to the passed parameters
    	this.seqNum = seqVariable;
    	//set unique id
    	this.uniqueId = uniqueId;
    	//set the value variable
    	this.objectValue = objectValue;
    }

    //when preparing decide requests, value, me index and done is needed
    public Request(int seqVariable, Object objectValue, int meIndex, int doneValue){
    	//set the classes values to the passed parameters
    	this.seqNum = seqVariable;
    	//set the ojbect value
    	this.objectValue = objectValue;
    	//set the me index
    	this.meIndex = meIndex;
    	//set the done value
    	this.doneValue = doneValue;
    }

    
}
