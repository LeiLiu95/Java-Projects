/*
 * EID's of group members
 *
 * Lei Liu = LL28379
 * Eric Li = ECL625
 */
import java.util.concurrent.Semaphore;

public class ThreadSynch {
	//int for number of threads waiting
	private int threadsWaiting;
	//int for max number of parties
	private int parties;
	//int for counter
	private int counter;
	//lock for start
	private Semaphore start;
	//semaphore acting as a lock
	private Semaphore lock;
	//semaphore for next
	private Semaphore next;
	
	public ThreadSynch(int parties) {
		//set object parties to the passed parties
		this.parties = parties;
		//initialize lock with 1 permit
		lock = new Semaphore(1);
		//initialize a semaphore with the maximum number of permits for parties
		start = new Semaphore(parties);
		//initialize a semaphore for the maximum number of permits for parties
		next = new Semaphore(parties);
		
		try {
			//try to acquire all the permits in parties
			next.acquire(parties);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//initialize variables for number waiting to 0
		threadsWaiting = 0;
		//initialize variables for counter as 0
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

		//acquire a permit for the start semaphore
		start.acquire();
		//use the lock semaphore to increment variables
		lock.acquire();
		//increment number for waiting
		threadsWaiting+=1;
		//set the return index as the parties - 1
		int index = parties - threadsWaiting;
		//release lock after changing and grabbing values
		lock.release();
		
		//if the number of threads waiting is greater than parties then release all permits for next
		if(parties <= threadsWaiting){
			//release the permits from next
			next.release(parties);
		}
		//acquire another permit for next
		next.acquire();
		//acquire lock to enter CS
		lock.acquire();
		//increment counter
		counter+=1;
		//release the next permit
		next.release();
		//check if the counter is == to parties
		if(counter==parties){
			//if it is then release all start permits
			start.release(parties);
			//reset variables for next round
			threadsWaiting=0;
			//reset the counter variable
			counter=0;
			//acquire all the next permits 
			next.acquire(parties);
		}
		//release the lock
		lock.release();
		//then return the index
	    return index;
//		//System.out.println("check");
//		while(next3==true) {
//			if(parties == max) {
//				semParties.acquire();
//				next=false;
//				next2=false;
//				//System.out.println(next3);
//				next3=false;
//				parties = max;
//				counter = 0;
//				//System.out.println(next3);
//				semParties.release();
//			}
//			//System.out.println(next3);
//		}
//		semParties.acquire();
//		int index = parties - 1;
//		parties-=1;
//		//System.out.println(counter);
//		//counter+=1;
//		//System.out.println(counter);
//		//semMax.acquire();
//		if(parties==0) {
//			next=true;
//		}
//		semParties.release();
//		//System.out.println("enter");
//		while(next!=true) {
//			
//		}
//		//System.out.println("exit1");
//		
//		semParties.acquire();
////		System.out.println(parties);
////		parties+=1;
////		System.out.println(parties);
//		counter+=1;
//		//System.out.println(parties);
//		if(counter==max) {
//			next2=true;
//		}
//		//System.out.println("enter");
//		semParties.release();
//		
//		
//		//while(next==true && next2!=true) {
//		while(next2!=true) {
//		}
//		//System.out.println("exit");
//		
//		semParties.acquire();
//		//next = false;
//		//next2 = false;
//		//System.out.println(parties);
//		next3=true;
//		semParties.release();
//		
//	    return index;
	}
}
