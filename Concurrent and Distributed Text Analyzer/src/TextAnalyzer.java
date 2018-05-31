import org.apache.hadoop.fs.Path;

//other imports needed for textanalyzer
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {

    // Replace "?" with your own output key / value types
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
//    public static class TextMapper extends Mapper<LongWritable, Text, ?, ?> {
	//output the value as text and a mapwritable
    public static class TextMapper extends Mapper<LongWritable, Text, Text, MapWritable> {
    	//no other values are needed for map method
    	public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you mapper function
    		//create a string that takes the values to a string
    		String input = value.toString();
    		//remove all other characters other than letters and numbers and set it to lowercase
    		input = input.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
    		//create a token for input
    		StringTokenizer tokenInput;
    		tokenInput = new StringTokenizer(input);
    		//create a hashmap for counting
    		HashMap<String, Integer> mainMap;
    		mainMap = new HashMap<String, Integer>();
    		//iterate through the input until no more inputs
    		while(tokenInput.hasMoreTokens()) {
    			//create a string and set it to the next input token
    			String rawInputWord = tokenInput.nextToken();
    			//check if the word is in the hashmap
    			if(!mainMap.containsKey(rawInputWord)){
    				//if it did not have the input word then add the word into the hashmap with an int of one for counter
    				mainMap.put(rawInputWord, 1);
    			}
    			//else increment the count of the input word
    			else{
    				//set current to the number from hashmap
    				int currentOccurence;
    				currentOccurence = mainMap.get(rawInputWord);
    				//increment current
    				currentOccurence += 1;
    				//add in the new input for the word
    				mainMap.put(rawInputWord, currentOccurence);
    			}
    		}
    		
    		//code that needs to be looped for the map
			for(String rawInputWord : mainMap.keySet()) {
				//map used for count 
				HashMap<String, Integer> secondMap;
				secondMap = new HashMap<String, Integer>();
				//add all of the data from master inot the new hashmap
				secondMap.putAll(mainMap);
				//if the count of input word is 1 then remove
				if(secondMap.get(rawInputWord) == 1) {
					secondMap.remove(rawInputWord);
				}
				//else decrement it by 1
				else {
					//set current to integer of hash
					int current;
					//set the current count to the count of the word
					current = secondMap.get(rawInputWord);
					//decrement the count
					current -=1;
					//add it back into the hash map
					secondMap.put(rawInputWord, current);
				}
				//create a text data for the input
				Text inputWord = new Text(rawInputWord);
				//create a map variable
				MapWritable returnMap = new MapWritable();
				//loop through to check the input and set it in the map
				for(String indexWord : secondMap.keySet()) {
					//create a variable text with the new text with the key from hashmap
					Text inputText = new Text(indexWord);
					//create an int writable with the number of occurences from the map
					IntWritable occurences = new IntWritable(secondMap.get(indexWord));
					//add the text and occurences to the map
					returnMap.put(inputText, occurences);
				}
				//add the word and map to the context to return
				context.write(inputWord, returnMap);
			}
    		
//    		String[] words = value.toString().split("\\s+");
//    		for(String word: words) {
//    			if(!StringUtils.isNumericSpace(word)) {
//    				textAnalyzerWritable.set(word);
//    				context.write(textAnalyzerWritable, countOne);
//    			}
//    		}
    		
//    		StringTokenizer iterator = new StringTokenizer(value.toString());
//    		while(iterator.hasMoreTokens()) {
//    			word.set(iterator.nextToken());
//    			context.write(word, one);
//    		}
        }
    }

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
//    public static class TextCombiner extends Reducer<?, ?, ?, ?> {
//        public void reduce(Text key, Iterable<Tuple> tuples, Context context)
    public static class TextCombiner extends Reducer<Text, MapWritable, Text, MapWritable> {
    	//no variables needed, changed iterable to mapwritable instead of tuple
        public void reduce(Text key, Iterable<MapWritable> tuples, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you combiner function
        	//create a mapwritable 
        	MapWritable returnMap = new MapWritable();
        	
        	//iterate through the map writable with the tuples
			for(MapWritable tupleElement : tuples) {
				//create a set of writables as a new hashset
				Set<Writable> currentSet;
				//create a writable hashset
				currentSet= new HashSet<Writable>();
				//iterate through the tuple and add each word to the hashset
				for(Writable inputWord : tupleElement.keySet()) {
					//add the word to the hashset
					currentSet.add(inputWord);
				}
				//iterate through the hashset checking each word
				for(Writable nextWord : currentSet) {
					//if the map contains the word
					if(returnMap.containsKey(nextWord)) {
						//create a new int value for the word
						IntWritable wordValue = new IntWritable(((IntWritable) returnMap.get(nextWord)).get() + ((IntWritable) tupleElement.get(nextWord)).get());
						//add the word with the int value to the map
						returnMap.put(nextWord, wordValue);
					}
					//else add the word to the map with the int from the prev ivous
					else {
						//add word to map with the current int
						returnMap.put(nextWord, (IntWritable) tupleElement.get(nextWord));
					}
				}
			}
			//add the key and map to the passed context variable
			context.write(key, returnMap);
        }
    }

    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function
//    public static class TextReducer extends Reducer<?, ?, Text, Text> {
    public static class TextReducer extends Reducer<Text, MapWritable, Text, Text> {
    	//variable for empty text
        private final static Text emptyText = new Text("");

//        public void reduce(Text key, Iterable<Tuple> queryTuples, Context context)
          public void reduce(Text key, Iterable<MapWritable> queryTuples, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you reducer function
        	  MapWritable returnMap;
        	  returnMap = new MapWritable();
        	  //iterate through the query and add it to 
        	  for(MapWritable tupleEntry : queryTuples) {
        		  //create a set with writables
        		  Set<Writable> currentSet = new HashSet<Writable>();
        		  //itereate throught the tuple entry for each word
        		  for(Writable inputWord : tupleEntry.keySet()) {
        			  //add the word to the set
        			  currentSet.add(inputWord);
        		  }
        		  //iterate through the hash to check for words
        		  for(Writable nextWord : currentSet) {
        			  //if the map does contain the word then
        			  if(returnMap.containsKey(nextWord)) {
        				  //set a int value to the word key
        				  IntWritable wordValue = new IntWritable(((IntWritable) returnMap.get(nextWord)).get() + ((IntWritable) tupleEntry.get(nextWord)).get());
        				  //add the word with the int value to the map
        				  returnMap.put(nextWord, wordValue);
        			  }
        			  //otherwise add it to the map
        			  else {
        				  //add the word with the word key int
        				  returnMap.put(nextWord, (IntWritable) tupleEntry.get(nextWord));
        			  }
				}
       	  }
            // Write out the results; you may change the following example
            // code to fit with your reducer function.
            //   Write out the current context key
            context.write(key, emptyText);
            //   Write out query words and their count
            //only one map is needer per querytuple
            List<String> queryList = new ArrayList<String>();
            //iterate through the map for each query word
            for(Writable queryWord: returnMap.keySet()) {
            	//add the query word to the map
            	queryList.add(queryWord.toString());
            }
            //sort the list in collection
            Collections.sort(queryList);
            //iterate through the list for each word
            for(String nextWord : queryList) {
            	//create a string for a coount
            	String counter;
            	//set it to the map value, add the < , > characters to the input
            	counter = returnMap.get(new Text(nextWord)).toString() + ">";
            	//the text of the query word will be < or ,
            	Text queryWordText = new Text("<" + nextWord + ",");
            	//write the text to the context
            	context.write(queryWordText, new Text(counter));
            }
//            for(String queryWord: map.keySet()){
//                String count = map.get(queryWord).toString() + ">";
//                queryWordText.set("<" + queryWord + ",");
//                context.write(queryWordText, new Text(count));
//            }
            //   Empty line for ending the current context key
            //cwrite the empty text to the context
            context.write(emptyText, emptyText);
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();

        // Create job
        Job job = new Job(conf, "ll28379_ecl625"); // Replace with your EIDs
        job.setJarByClass(TextAnalyzer.class);

        // Setup MapReduce job
        job.setMapperClass(TextMapper.class);
        //   Uncomment the following line if you want to use Combiner class
        //following line was uncommented
        job.setCombinerClass(TextCombiner.class);
        job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        //next two lines were uncommented
        
        //output key is changed to text class
        job.setMapOutputKeyClass(Text.class);
        //output value is changed to mapwritable
        job.setMapOutputValueClass(MapWritable.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    // Do not modify the main method
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
        System.exit(res);
    }

    // You may define sub-classes here. Example:
    // public static class MyClass {
    //
    // }
}



