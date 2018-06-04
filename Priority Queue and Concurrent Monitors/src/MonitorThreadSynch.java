public class MonitorThreadSynch {
	final int counter;
	int numThreads; //the number of threads that have been released from the barrier
	int monitorIndex;
	
	public MonitorThreadSynch(int parties) {
		this.numThreads = 0;
		this.counter = parties;
		this.monitorIndex = parties - 1; //set the starting monitorIndex
	}
	
	public synchronized int await() throws InterruptedException {
		while(monitorIndex == -1) {
			wait();		
		}
		int index = monitorIndex; //assign this thread the current index
		monitorIndex-=1; 
		
        while(monitorIndex != -1) {
        	wait();
        }         
        
        numThreads+=1; //increment the number of threads released from the barrier
        if(counter == numThreads){
        	numThreads = 0;
        	monitorIndex = counter-1;
        }
        notifyAll(); 
	    return index;
	}
}
