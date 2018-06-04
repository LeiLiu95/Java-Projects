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
    	public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
        {
    		String input = value.toString();
    		input = input.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
    		StringTokenizer tokenInput;
    		tokenInput = new StringTokenizer(input);
    		HashMap<String, Integer> mainMap;
    		mainMap = new HashMap<String, Integer>();
    		while(tokenInput.hasMoreTokens()) {
    			String rawInputWord = tokenInput.nextToken();
    			if(!mainMap.containsKey(rawInputWord)){
    				mainMap.put(rawInputWord, 1);
    			}
    			else{
    				int currentOccurence;
    				currentOccurence = mainMap.get(rawInputWord);
    				currentOccurence += 1;
    				mainMap.put(rawInputWord, currentOccurence);
    			}
    		}
    		
			for(String rawInputWord : mainMap.keySet()) {
				HashMap<String, Integer> secondMap;
				secondMap = new HashMap<String, Integer>();
				secondMap.putAll(mainMap);
				if(secondMap.get(rawInputWord) == 1) {
					secondMap.remove(rawInputWord);
				}
				else {
					int current;
					current = secondMap.get(rawInputWord);
					current -=1;
					secondMap.put(rawInputWord, current);
				}
				Text inputWord = new Text(rawInputWord);
				MapWritable returnMap = new MapWritable();
				for(String indexWord : secondMap.keySet()) {
					Text inputText = new Text(indexWord);
					IntWritable occurences = new IntWritable(secondMap.get(indexWord));
					returnMap.put(inputText, occurences);
				}
				context.write(inputWord, returnMap);
			}
        }
    }

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
//    public static class TextCombiner extends Reducer<?, ?, ?, ?> {
//        public void reduce(Text key, Iterable<Tuple> tuples, Context context)
    public static class TextCombiner extends Reducer<Text, MapWritable, Text, MapWritable> {
        public void reduce(Text key, Iterable<MapWritable> tuples, Context context)
            throws IOException, InterruptedException
        {
        	MapWritable returnMap = new MapWritable();

			for(MapWritable tupleElement : tuples) {
				Set<Writable> currentSet;
				currentSet= new HashSet<Writable>();
				for(Writable inputWord : tupleElement.keySet()) {
					currentSet.add(inputWord);
				}
				for(Writable nextWord : currentSet) {
					if(returnMap.containsKey(nextWord)) {
						IntWritable wordValue = new IntWritable(((IntWritable) returnMap.get(nextWord)).get() + ((IntWritable) tupleElement.get(nextWord)).get());
						returnMap.put(nextWord, wordValue);
					}
					else {
						returnMap.put(nextWord, (IntWritable) tupleElement.get(nextWord));
					}
				}
			}
			context.write(key, returnMap);
        }
    }

    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function
//    public static class TextReducer extends Reducer<?, ?, Text, Text> {
    public static class TextReducer extends Reducer<Text, MapWritable, Text, Text> {
        private final static Text emptyText = new Text("");

          public void reduce(Text key, Iterable<MapWritable> queryTuples, Context context)
            throws IOException, InterruptedException
        {
        	  MapWritable returnMap;
        	  returnMap = new MapWritable();
        	  for(MapWritable tupleEntry : queryTuples) {
        		  Set<Writable> currentSet = new HashSet<Writable>();
        		  for(Writable inputWord : tupleEntry.keySet()) {
        			  currentSet.add(inputWord);
        		  }
        		  for(Writable nextWord : currentSet) {
        			  if(returnMap.containsKey(nextWord)) {
        				  IntWritable wordValue = new IntWritable(((IntWritable) returnMap.get(nextWord)).get() + ((IntWritable) tupleEntry.get(nextWord)).get());
        				  returnMap.put(nextWord, wordValue);
        			  }
        			  else {
        				  returnMap.put(nextWord, (IntWritable) tupleEntry.get(nextWord));
        			  }
				}
       	  }
            context.write(key, emptyText);
            List<String> queryList = new ArrayList<String>();
            for(Writable queryWord: returnMap.keySet()) {
            	queryList.add(queryWord.toString());
            }
            Collections.sort(queryList);
            for(String nextWord : queryList) {
            	String counter;
            	count = returnMap.get(new Text(nextWord)).toString() + ">";
            	Text queryWordText = new Text("<" + nextWord + ",");
            	context.write(queryWordText, new Text(counter));
            }
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



