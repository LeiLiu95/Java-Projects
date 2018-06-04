import java.util.concurrent.Semaphore;

public class ThreadSynch {
	private int threadsWaiting;
	private int parties;
	private int counter;
	private Semaphore start;
	private Semaphore lock;
	private Semaphore next;
	
	public ThreadSynch(int parties) {
		this.parties = parties;
		lock = new Semaphore(1);
		start = new Semaphore(parties);
		next = new Semaphore(parties);
		
		try {
			next.acquire(parties);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		threadsWaiting = 0;
		counter = 0;
	}
	
	
	public int await() throws InterruptedException {
		// Waits until all parties have invoked await on this ThreadSynch.
		// If the current thread is not the last to arrive then it is
		// disabled for thread scheduling purposes and lies dormant until
		// the last thread arrives.
		// Returns: the arrival index of the current thread, where index
		// (parties - 1) indicates the first to arrive and zero indicates
		// the last to arrive.

		start.acquire();
		lock.acquire();
		threadsWaiting+=1;
		int index = parties - threadsWaiting;
		lock.release();
		
		if(parties <= threadsWaiting){
			next.release(parties);
		}
		next.acquire();
		lock.acquire();
		counter+=1;
		next.release();
		if(counter==parties){
			start.release(parties);
			threadsWaiting=0;
			counter=0;
			next.acquire(parties);
		}
		lock.release();
	    return index;
	}
}
