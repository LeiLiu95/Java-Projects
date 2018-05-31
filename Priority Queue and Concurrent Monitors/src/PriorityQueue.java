import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
	//int to hold list maximum size and current size
	private int maxSize;
	public int currentSize;
	//a linked list of the node object
	private LinkedList<Node> queue;
	//reentrant lock
	final Lock lock = new ReentrantLock();
	//two locks for not full and not empty
	final Condition notFull = lock.newCondition();
	final Condition notEmpty = lock.newCondition();
	//private Semaphore gLock;
	
	public PriorityQueue(int maxSize) {
		this.maxSize = maxSize;
		this.currentSize = 0;
//		lock = new ReentrantLock();
		queue = new LinkedList<Node>();
//		notFull = lock.newCondition();
//		notEmpty = lock.newCondition();
		//gLock = new Semaphore(1);
        // Creates a Priority queue with maximum allowed size as capacity
	}
	
	public int add(String name, int priority){
        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // This method blocks when the list is full.
		
		//create a node to add/check
		Node temp = new Node(name, priority);
		//lock the lock
		lock.lock();
		//do try/finally
		try {
			//acquire the lock on the node
			temp.acquireLock();
			//if the list is 0 then return
			if(currentSize==0) {
				if(currentSize<maxSize) {
					//add the element into the queue or linked list
					queue.add(temp);
					//increment the current size of linked list by 1
					currentSize+=1;
					//release the lock
					temp.releaseLock();
					try {
						notEmpty.signal();
					}
					catch(Exception e) {
						
					}
					//lock.unlock();
					return 0;
				}
			}
			//variable used to hold index or position
			int i;
			//for loop to check if the variable exists in the linked list
			for(i=0;i<currentSize; i+=1) {
				//if the name exists then exit and return -1
				if(temp.name.equals(((Node) queue.get(i)).getName())) {
					//release lock
					temp.releaseLock();
					//return lock because a node with the same name exists
					return -1;
				}
			}
			//if the current size is equal to max
			while(currentSize==maxSize) {
				try {
					//wait for the list to be available
					notFull.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//for loop to go through maxsize to see where to insert node
			for(i=0; i<maxSize; i+=1) {
				//if last element of current list then add to last spot
				if(i==currentSize) {
					queue.addLast(temp);
					//increment the current size
					currentSize+=1;
					
					break;
				}
				//
//				else if(temp.name.equals(((Node) queue.get(i)).getName())) {
//					temp.releaseLock();
//					//lock.unlock();
//					return -1;
//				}
				//check if the node priority is greater than the current node index
				else if(temp.priority>((Node) queue.get(i)).getPriority()) {
					//if it is then add node into the current idnex
					queue.add(i, temp);
					//increment the list size
					currentSize+=1;
					break;
				}
			
			}
			//when done release the lock for the temp node
			temp.releaseLock();
			try {
				notEmpty.signal();
			}
			catch(Exception e) {
				
			}
			//lock.unlock();
			//return the index
			return i;
		}
		finally {
			//unlock the lock
			lock.unlock();
		}
	}

	public int search(String name){
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.

		//lock the lock
		lock.lock();
		//do code then return and unlock
		try {
			//if list is empty then return -1
			if(currentSize==0) {
				return -1;
			}
			//variable to use as index
			int i;
			//for loop to iterate through list
			for(i=0; i<currentSize; i+=1) {
				//if the name is found then return the index
				if(name == (((Node) queue.get(i)).getName())) {
					return i;
				}
			}
			//if not found then return -1
			return -1;
		}
		//unlock the lock
		finally {
			lock.unlock();
		}
	}

	public String getFirst(){
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
		
		//lock the lock
		lock.lock();
		//try statement
		try {
			//if the list is empty then wait
			while(currentSize==0) {
				//wait until not empty signal
				try {
					notEmpty.await();
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			//when there is an element
			if(currentSize>0) {
				//create a temporary node that is equal to the removed node
				Node temp = (Node) queue.remove();
				//decrement the current list size
				currentSize-=1;
				try {
					//signal one other wait that the list is not full anymore
					notFull.signal();
				}
				catch(Exception e) {
					
				}
				//System.out.println(temp.priority);
				return temp.getName();
			}
			//return null if anything failed
			return null;
		}
		//unlock the lock when done
		finally {
			lock.unlock(); 
		}
	}
	
	private class Node{
		//variables used for Node
		//priority level, name and lock
		private int priority;
		private String name;
		private ReentrantLock lock;
		
		//constructors
		public Node(String name, int priority){
			this.priority = priority;
			this.name = name;
			this.lock = new ReentrantLock();
		}
		
		//setter and getter methods
		public String getName() {
			return this.name;
		}
		
		public int getPriority() {
			return this.priority;
		}
		
		public void acquireLock() {
			this.lock.lock();
			return;
		}
		
		public void releaseLock() {
			this.lock.unlock();
			return;
		}
	}
}

