/*
 * EID's of group members
 * 
 * Lei Liu = LL28379
 * Eric Li = ECL625
 */

public class MonitorThreadSynch {
	//int for counter
	final int counter;
	//int for number of threads that are released
	int numThreads; //the number of threads that have been released from the barrier
	//int for the idnex of the monitor
	int monitorIndex;
	
	public MonitorThreadSynch(int parties) {
		//set number of threads released to 0
		this.numThreads = 0;
		//set the counter to the parties
		this.counter = parties;
		//set monitorIndex to parties - 1
		this.monitorIndex = parties - 1; //set the starting monitorIndex
	}
	
	public synchronized int await() throws InterruptedException {
		//check done to prevent new thread entering while others are still leaving
		while(monitorIndex == -1) {
			//call wait until it is ready
			wait();		
		}
		//set the monitorIndex as parties - 1
		int index = monitorIndex; //assign this thread the current index
        //drecrement monitor index when a new thread enters
		monitorIndex-=1; 
		
        //check if all threads are passed
        while(monitorIndex != -1) {
        	//if they are not then wait
        	wait();
        }         
        
        //increment the number of threads released by 1
        numThreads+=1; //increment the number of threads released from the barrier
        //if the number of threads released == to maximum number then reset round
        if(counter == numThreads){
        	//reset variables when all threads are ready
        	//set number of thread released back to 0
        	numThreads = 0;
        	//set the monitor index to counter-1
        	monitorIndex = counter-1;
        }
        //notify all threads when done
        notifyAll(); 
        //return the index, parties - 1
	    return index;
	}
}
