import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
	private int maxSize;
	public int currentSize;
	private LinkedList<Node> queue;
	final Lock lock = new ReentrantLock();
	final Condition notFull = lock.newCondition();
	final Condition notEmpty = lock.newCondition();
	
	public PriorityQueue(int maxSize) {
		this.maxSize = maxSize;
		this.currentSize = 0;
		queue = new LinkedList<Node>();
	}
	
	public int add(String name, int priority){
        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // This method blocks when the list is full.
		
		Node temp = new Node(name, priority);
		lock.lock();
		try {
			temp.acquireLock();
			if(currentSize==0) {
				if(currentSize<maxSize) {
					queue.add(temp);
					currentSize+=1;
					temp.releaseLock();
					try {
						notEmpty.signal();
					}
					catch(Exception e) {
						
					}
					return 0;
				}
			}
			int i;
			for(i=0;i<currentSize; i+=1) {
				if(temp.name.equals(((Node) queue.get(i)).getName())) {
					temp.releaseLock();
					return -1;
				}
			}
			while(currentSize==maxSize) {
				try {
					notFull.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for(i=0; i<maxSize; i+=1) {
				if(i==currentSize) {
					queue.addLast(temp);
					currentSize+=1;
					
					break;
				}

				else if(temp.priority>((Node) queue.get(i)).getPriority()) {
					queue.add(i, temp);
					currentSize+=1;
					break;
				}
			
			}
			temp.releaseLock();
			try {
				notEmpty.signal();
			}
			catch(Exception e) {
				
			}
			return i;
		}
		finally {
			lock.unlock();
		}
	}

	public int search(String name){
		lock.lock();
		try {
			if(currentSize==0) {
				return -1;
			}
			int i;
			for(i=0; i<currentSize; i+=1) {
				if(name == (((Node) queue.get(i)).getName())) {
					return i;
				}
			}
			return -1;
		}
		finally {
			lock.unlock();
		}
	}

	public String getFirst(){
		lock.lock();
		try {
			while(currentSize==0) {
				try {
					notEmpty.await();
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(currentSize>0) {
				Node temp = (Node) queue.remove();
				currentSize-=1;
				try {
					notFull.signal();
				}
				catch(Exception e) {
					
				}
				return temp.getName();
			}
			return null;
		}
		finally {
			lock.unlock(); 
		}
	}
	
	private class Node{
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

