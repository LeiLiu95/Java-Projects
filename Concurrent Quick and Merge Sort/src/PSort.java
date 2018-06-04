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
	
	//parallel sort function
	public static void parallelSort(int[] A, int begin, int end){
		//create a pool for forks
		commonPool = new ForkJoinPool();
		//create a PSort object for sorting
		PSort sort = new PSort(A, begin, end - 1);
		//begin the quick sorting in parallel
		commonPool.invoke(sort);		
		commonPool.shutdown();
		//used for testing
		//printArray(A);
	}
	
	@Override
	protected void compute() {
		//if there are less than 16 elements in the array then sort
		if(end - begin < 16){
			Arrays.sort(pSortArray, begin, end + 1);
		}
		//otherwise, break it down and do it in parallel
		else {
			int pivotIndex = pivotIndex(pSortArray, begin, end);
			PSort threadLeft = new PSort(pSortArray, begin, pivotIndex - 1);
			PSort threadRight = new PSort(pSortArray, pivotIndex + 1, end);
			threadLeft.fork();
			threadRight.compute();
			threadLeft.join();
		}
	}
	
	
	//function to create a pivot for quick sort
	int pivotIndex(int[] qArray, int begin, int end){
		int index = begin - 1;
		int endValue = qArray[end];
		for (int i = begin; i < end; i+=1) {
			if (qArray[i] < endValue) {
				index+=1;
				swap(qArray, index, i);
			}
		}
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