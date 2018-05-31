
public class testPriorityQueue implements Runnable{
	final PriorityQueue q;
	
	public testPriorityQueue(PriorityQueue q) {
		this.q=q;
	}
	
//	public void run() {
//		int index = -1;
//
//		for (int round = 0; round < ROUND; ++round) {
//			System.out.println("Thread " + Thread.currentThread().getId() + " is WAITING round:" + round);
//			try {
//				index = gate.await();
//				//System.out.println("Thread " + Thread.currentThread().getId() + " is " + index);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			System.out.println("Thread " + Thread.currentThread().getId() + " is leaving round:" + round);
//		}
//	}
	public void run() {

	}
//	public static void test() {
//		int maxSize = 5;
//		PriorityQueue x = new PriorityQueue(5);
//		x.add("a", 8);
//		//System.out.println(x.getFirst());
//		x.add("b", 2);
//		x.add("c", 3);
//		x.add("d", 4);
//		x.add("e", 9);
////		x.add("f", 9);
//		System.out.println(x.search("a"));
//		
//		//x.add("c", 1);
//		int y = x.currentSize;
//		for(int i = 0; i<y; i+=1) {
//			System.out.println(x.getFirst());
//		}
//	}
	
	public static void main(String[] args) {
//		ThreadSynch gate = new ThreadSynch(SIZE);
//		Thread[] t = new Thread[SIZE];
//		
//		for (int i = 0; i < SIZE; ++i) {
//			t[i] = new Thread(new testThreadSynch(gate));
//		}
//		
//		for (int i = 0; i < SIZE; ++i) {
//			t[i].start();
//		}
		PriorityQueue x = new PriorityQueue(4);
//		Thread t1 = new Thread(new testPriorityQueue(x));
////		x.test();
//		Thread t2 = new Thread(new testPriorityQueue(x));	
//		t1.start();
//		t2.start();
//		int y = x.currentSize;
//		for(int i = 0; i<y; i+=1) {
//			System.out.println(x.getFirst());
//		}

		x.add("a", 6);
		//System.out.println(x.getFirst());
//		x.getFirst();
		x.add("b", 7);
//		x.getFirst();
		x.add("c", 3);
//		x.getFirst();
		x.add("d", 4);
		x.add("a", 1);
//		x.add("e", 9);
//		x.add("f", 9);
		System.out.println(x.search("a"));
//		
//		//x.add("c", 1);
		int y = x.currentSize;
		for(int i = 0; i<y; i+=1) {
			//System.out.println(x.getFirst());
		}
		System.out.println("check");
		System.out.println(x.search("a"));
		System.out.println(x.search("b"));
		System.out.println(x.search("c"));
		System.out.println(x.search("e"));
    }
}
