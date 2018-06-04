package kvpaxos;
import java.io.Serializable;

/**
 * You may find this class useful, free to use it.
 */
public class Op implements Serializable{
    static final long serialVersionUID=33L;
    String op;
    int ClientSeq;
    String key;
    Integer value;
    String clientCallerId;
    
    //a to string function for op
    public String toString (){
    	if (op.equals ("Put")) {
    		return "(" + clientCallerId + ", " + Integer.toString (value) + ")";
    	}
    	else {
    		return "(" + clientCallerId+ ",)";
    	}
    }
    
    public Op(String op, String clientCallerId, String key, Integer value){
        this.op = op;
        this.clientCallerId = clientCallerId;
        this.key = key;
        this.value = value;
    }
}
