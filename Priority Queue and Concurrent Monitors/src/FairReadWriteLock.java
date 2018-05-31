public class FairReadWriteLock {
	//variables needed
	//current number of readers
	int numOfReaders = 0;
	//current number of writers
	int numOfWriters = 0;
	//number of read and write requests
	int nextThread = 0;
	//what turn it is on
	int tid = 0;
           
	//not needed functions anymore
	
	//function to check if number of writers is greater than zero
	private boolean readRequirementsViolated() {
		return numOfWriters > 0;
	}
	
	//function to check if number of readers is greater than 0 or if number of writers is greater than 0
	private boolean writeRequirementsViolated() {
		return (numOfReaders > 0 || numOfWriters > 0);
	}
	
	//function to put thread to sleep with wait() function call
	private void sleepThread() {
		try {
			//wait until it is notified to continue 
			wait();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void beginRead() {
		//set the id of reader thread to the current tid
		int readerThreadId = tid;
		//increment tid
		tid+=1;
		//check if there are any writer threads or if the next thread is the reader
		while(readerThreadId!=nextThread || numOfWriters > 0){ 
			//if either condition is met then wait
			try{
				wait();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		//increment the next thread
		nextThread+=1; 
		//increment number of reader threads
		numOfReaders+=1;
	}
	
	public synchronized void endRead() {
		//decrement the number of readers by 1
		numOfReaders-=1;
		//notify all waiting threads
		notifyAll();
	}
	
	public synchronized void beginWrite() {
		//set the id of writer thread to the current tid
		int writerThreadId = tid;
		//increment tid
		tid+=1;
		//check if there are any readers,writers or current writer thread is waiting
		while(numOfWriters > 0 || numOfReaders > 0 || writerThreadId != nextThread){
			//if any conditions are true then wait
			try{
				wait();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		//increment the next thread 
		nextThread+=1;
		//increment the number of writer threads
		numOfWriters+=1;
	}
	
	public synchronized void endWrite() {
		//decrement number of writers by 1
		numOfWriters-=1;
		//notify all waiting threads
		notifyAll();
	}

}
	