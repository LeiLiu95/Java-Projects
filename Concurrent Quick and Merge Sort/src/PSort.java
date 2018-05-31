//UT-EID = LL28379
//UT-EID = ECL625


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction{
	//need pool for forkjoinpool
	private static ForkJoinPool commonPool;
	//create variables in the class for array, start and end
	private int[] pSortArray;
	private int begin;
	private int end;
	
	//PSort constructor
	PSort(int[] A, int begin, int end) {
		this.pSortArray = A;
		this.begin = begin;
		this.end = end;
	}

	//insert sort was used
//	public static int[] insertSort(int[] A) {
//		int swap;
//		for(int i = 1; i < A.length; i += 1) {
//			for(int j = i; j > 0; j -= 1) {
//				if(A[j] < A[j-1]) {
//					swap = A[j];
//					A[j] = A[j-1];
//					A[j-1] = swap;
//				}
//			}
//		}
//		return A;
//	}
	
	//parallel sort function
	public static void parallelSort(int[] A, int begin, int end){
		//create a pool for forks
		commonPool = new ForkJoinPool();
		//create a PSort object for sorting
		PSort sort = new PSort(A, begin, end - 1);
		//begin the quick sorting in parallel
		commonPool.invoke(sort);		
		//once complete, shut down the pool
		commonPool.shutdown();
		//used for testing
		//printArray(A);
	}
	
	@Override
	protected void compute() {
		//if there are less than 16 elements in the array then sort
		if(end - begin < 16){
			//pSortArray = insertSort(pSortArray);
			Arrays.sort(pSortArray, begin, end + 1);
		}
		//otherwise, break it down and do it in parallel
		else {
			//create a pivot index for quick sort
			int pivotIndex = pivotIndex(pSortArray, begin, end);
			//create one thread for the first half of the array
			PSort threadLeft = new PSort(pSortArray, begin, pivotIndex - 1);
			//create another thread for the second half of the array
			PSort threadRight = new PSort(pSortArray, pivotIndex + 1, end);
			//begin thread 1
			threadLeft.fork();
			//begin thread 2
			threadRight.compute();
			//join together the threads when done
			threadLeft.join();
		}
	}
	
	
	//function to create a pivot for quick sort
	int pivotIndex(int[] qArray, int begin, int end){
		//set the index to the start with one less
		int index = begin - 1;
		//get the value of the last element in the array
		int endValue = qArray[end];
		//iterate through the array to move elements
		for (int i = begin; i < end; i+=1) {
			//if the element is less than the end then swap
			if (qArray[i] < endValue) {
				//increment index
				index+=1;
				//swap elements
				swap(qArray, index, i);
			}
		}
		//increment index, do swap then return the index
		index+=1;
		swap(qArray, index, end);
		return index;
	}

	//function to swap the elements in two places of an array
	void swap(int[] arraySwap, int first, int next){
		int temporary = arraySwap[first];
		arraySwap[first] = arraySwap[next];
		arraySwap[next] = temporary;
	}
	
}