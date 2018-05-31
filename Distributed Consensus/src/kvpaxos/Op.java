package kvpaxos;
import java.io.Serializable;

/**
 * You may find this class useful, free to use it.
 */
public class Op implements Serializable{
    static final long serialVersionUID=33L;
    String op;
    int ClientSeq;
    //key value used for op
    String key;
    //integer value for op
    Integer value;
    //string is used which is the id of the client caller
    String clientCallerId;
    
    //a to string function for op
    public String toString (){
    	//if the op has the put function then
    	if (op.equals ("Put")) {
    		//return a function with the client id and the value
    		return "(" + clientCallerId + ", " + Integer.toString (value) + ")";
    	}
    	//other wise return the client id 
    	else {
    		return "(" + clientCallerId+ ",)";
    	}
    }
    
    public Op(String op, String clientCallerId, String key, Integer value){
        this.op = op;
        //this.ClientSeq = ClientSeq;
        //set the caller id to the client caller
        this.clientCallerId = clientCallerId;
        //set the key for op
        this.key = key;
        //set value for op
        this.value = value;
    }
}
