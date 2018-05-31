import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

//UT-EID = LL28379
//UT-EID = ECL625


public class PMerge extends RecursiveAction{
	//ForkJoinPool needed for threads
	private static ForkJoinPool commonPool;
	//make copies of arrays for each array
	private int[] A;
	private int[] B;
	private int[] C;
//	private int[] indexA;
//	private int[] indexB;
//	private int[] indexC;
	//keep track of current number of threads and max number of threads
	private int numThreads;
	private int currentThreads;
	
	//constructor class for PMerge
	public PMerge(int[] A, int[] B, int[] C, int numThreads) {
		//this.commonPool = new ForkJoinPool(numThreads);
		this.A = A;
		this.B = B;
		this.C = C;
//		this.indexA = indexA;
//		this.indexB = indexB;
//		this.indexC = indexC;
		this.numThreads = numThreads;
		this.currentThreads = 1;
	}
	//merge array A and B into C
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		//initialize pool and begin pmerge
		commonPool = new ForkJoinPool(numThreads);
		PMerge pMerge = new PMerge(A, B, C, numThreads);
		
		commonPool.invoke(pMerge);
		//close out of pool when exiting
		commonPool.shutdown();
		
	}
	
	@Override
	protected void compute() {
		//if length of arrays are small then just compute
		if(A.length < 2 || B.length < 2) {
			mergeParts(A, B, C);
		}
		//check if there is not enough threads for the process
		else if(A.length < numThreads && B.length < numThreads) {
			//check if any more threads are allowed
			if(currentThreads < numThreads) {
//				int midA = A.length/2;
//				int midB = B.length/2;
//				int[] A1 = Arrays.copyOfRange(A, 0, midA);
//				int[] A2 = Arrays.copyOfRange(A, midA, A.length);
//				
//				int[] B1 = Arrays.copyOfRange(B, 0, midB);
//				int[] B2 = Arrays.copyOfRange(B, midB, B.length);
//				int[] C1 = new int[A1.length + B1.length];
//				int[] C2 = new int[A2.length + B2.length];
//				this.currentThreads+=1;
//				PMerge thread1 = new PMerge(A1, B1, C, currentThreads);
//				this.currentThreads-=1;
//				
//				this.currentThreads+=1;
//				PMerge thread2 = new PMerge(A2, B2, C, currentThreads);
//				this.currentThreads-=1;
//				invokeAll(thread1, thread2);
//				thread1.join();
				
				//use this to watch for threads
				
				//keep track of thread count
				this.currentThreads+=1;
				PMerge thread1 = new PMerge(A, B, C, currentThreads);
				this.currentThreads-=1;
				
				this.currentThreads+=1;
				PMerge thread2 = new PMerge(A, B, C, currentThreads);
				this.currentThreads-=1;
				//begin thread work and join
				thread1.fork();
				thread2.compute();
				thread1.join();
				//mergeParts(thread1, thread2, C);
//				thread1.fork();
//				thread2.compute();
//				thread1.join();
				Arrays.sort(C, 0, C.length);
			}
		}
		else {
			//if previous conditions fail then merge A and B into C
			mergeParts(A, B, C);
		}
		
	}
	
	//function that merges A and B into C in orders
	public void mergeParts(int[] A, int[] B, int[] C) {
		int indexA = 0;
		int indexB = 0;
		int indexC = 0;
		while(indexA < A.length && indexB < B.length) {
			if(A[indexA] <= B[indexB]) {
				C[indexC] = A[indexA];
				indexA+=1;
			}
			else {
				C[indexC] = B[indexB];
				indexB+=1;
			}
			indexC+=1;
		}
		if(indexA == A.length) {
			while(indexB < B.length) {
				C[indexC] = B[indexB];
				indexC+=1;
				indexB+=1;
			}
		}
		else if(indexB == B.length) {
			while(indexA < A.length) {
				C[indexC] = A[indexA];
				indexC+=1;
				indexA+=1;
			}
		}
	}
}
