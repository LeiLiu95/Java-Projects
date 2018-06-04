import java.util.*;

import javax.naming.spi.DirStateFactory.Result;

import java.io.*;

public class Main {

	public static void main(String[] args) {
		Scanner kb = new Scanner(System.in);
		String quit = "/quit"; 					//if the command /quit is read then the program will close
		
		while(true){
			String user_input=kb.next();		//reads next input
			if(quit.compareTo(user_input)==0){
				break;
			}
			if(user_input.charAt(0)== '/'){
				System.out.println("invalid command " + user_input);
				continue;
			}
			else{
				String user_input_end = kb.next();	//the second word is taken
				if(getWordLadderDFS(user_input, user_input_end).size()==0){
					System.out.println("no word ladder can be found between " + user_input + " and " + user_input_end);
				}
				else{
					ArrayList<String> new_list = getWordLadderDFS(user_input, user_input_end);
					System.out.println("a " + (new_list.size()-2) + "-rung word ladder exists between " + user_input + " and " + user_input_end);
					for(String string_compare : new_list){
						System.out.println(string_compare);
					}
				}
			}
		}
		kb.close();
		return;
	}
	/**
	 * 
	 * @param x, first string to compare
	 * @param y, checks to see if x and y has only one difference
	 * @return, returns true or false to see if string x and y only have one difference
	 */
	public static boolean Difference_Of_One(String x, String y){
		int length=x.length();
		boolean check=false;
		for(int k=0; k<length; k+=1){
			if(x.charAt(k)!= y.charAt(k)){
				if(check==true){
					return false;
				}
				else{
					check=true;
				}
			}
		}
		if(check!=true){
			return false;
		}
		return true;
	}
	
	/** 
	 * 
	 * @param x, string passed to method
	 * @param Dictionary, a word set that is passed to see what words can be checked
	 * @return, returns an arraylist of the words with only one difference to string x
	 */
	public static ArrayList<String> Connection(String x, Set<String> Dictionary){
		ArrayList<String> return_arraylist = new ArrayList<String>();
		for(String string_compare : Dictionary){			//a for loop that checks all words in the dictionary that has one difference
			if(Difference_Of_One(x, string_compare)){		//with x
				return_arraylist.add(string_compare);
			}
		}
		return return_arraylist;
	}
	
	/**
	 * 
	 * @param start, the start word used to check 
	 * @param end, the last word that the ladder connects to
	 * @param dict, dictionary that is passed to the method for the check
	 * @return
	 */
	public static ArrayList<String> DFSwordladder(String start, String end, Set<String> dict){
		ArrayList<String> links=Connection(start,dict);
		if(links.contains(end)==true){								//base case, if the end is found then return
			ArrayList<String> wordLadder=new ArrayList<String>();
			wordLadder.add(end);
			wordLadder.add(start);
			return wordLadder;
		}
		
		if(links.isEmpty()){										//return null if end is not found
			return null;
		}
		
		dict.removeAll(links);	//removes all visited nodes
		int length=dict.size();
		String minimal_String=null;
		ArrayList<String> minimal_path=new ArrayList<String>();
		minimal_path=null;
		for(String s: links){										//checks with recursion for DFS word ladder
			ArrayList<String> Paths=DFSwordladder(s,end,dict);
			if(Paths != null){
				if(Paths.size()<length){
					minimal_path=Paths;
					length=Paths.size();
					minimal_String=s;
				}
			}
			else{
				continue;
			}
		}
		if(minimal_path==null){
			return null;
		}
		dict.add(minimal_String);
		minimal_path.add(start);
		return minimal_path;										//returns the array
	}	
	
	/**
	 * 
	 * @param start, start word needed for the ladder
	 * @param end, end word that will finish the ladder
	 * @return, returns an array list if a ladder exists or an empty array list if none exists
	 */
	public static ArrayList<String> getWordLadderDFS(String start, String end) {
		ArrayList<String> return_list=new ArrayList<String>();
		if(start.length()!=end.length()){
			return return_list;
		}
		Set<String> dict = makeDictionary();
		boolean start_check=false;
		boolean end_check=false;	
		start=start.toUpperCase();
		end=end.toUpperCase();
		for(String s:dict){
			if(s.equals(start)){
				start_check=true;
			}
			if(s.equals(end)){
				end_check=true;
			}
		}
		if(start_check!=true || end_check!=true){
			return return_list;
		}
		ArrayList<String> ladder = DFSwordladder(start,end,dict);
		if(ladder==null){
			return return_list;
		}
		if(start.equals(end)){
			return return_list;
		}
		ArrayList<String> flip_arraylist=new ArrayList<String>();
		flip_arraylist.add(start.toLowerCase());
		for(int i = ladder.size()-2; i>=0; i-=1){
			flip_arraylist.add(ladder.get(i).toLowerCase());
		}
		return flip_arraylist;
	}
	
	/**
	 * 
	 * @param start, start word for the bfs ladder
	 * @param end, end word for the bfs ladder
	 * @param dict, dictionary used to check for comparisons
	 * @return, returns an arraylist 
	 */
	public static ArrayList<String> BFSwordladder(String start, String end, Set<String> dict){
		ArrayList<String> return_list=new ArrayList<String>();
		ArrayList<String> stack=new ArrayList<String>();
		ArrayList<String> ladder=Connection(start,dict);
		if(ladder.contains(end)==true){				//if end is found then a ladder exists
			return_list.add(start);
			return_list.add(end);
			return return_list;
		}
		if(ladder==null){							//if end is not found then no ladder exists
			return return_list;
		}
		ArrayList<String> dict_removed=new ArrayList<String>();
		for(int i=0; i<ladder.size(); i+=1){				//removes the words in dictionary
			String index=ladder.get(i);
			dict_removed.add(index);
			dict.remove(index);
		}
		for(int i=0; i<ladder.size();i+=1){					//a recursive check is called to see if a ladder exists
			String check=ladder.get(i);
			return_list= BFSwordladder(check,end,dict);
			if(return_list.contains(end)){
				return_list.add(0,start);
				return return_list;
			}
		}
		
		return return_list;
	}
	
    public static ArrayList<String> getWordLadderBFS(String start, String end) {
		ArrayList<String> return_list=new ArrayList<String>();
		if(start.length()!=end.length()){
			return return_list;
		}
		Set<String> dict = makeDictionary();
		boolean start_check=false;
		boolean end_check=false;	
		start=start.toUpperCase();
		end=end.toUpperCase();
		for(String s:dict){
			if(s.equals(start)){
				start_check=true;
			}
			if(s.equals(end)){
				end_check=true;
			}
		}
		if(start_check!=true || end_check!=true){
			return return_list;
		}

		return_list=BFSwordladder(start,end,dict);
		if(return_list.size()!=0){
			return_list.remove(start);
			return_list.add(0,start);
		}
		return return_list;
	}
    
    
    
	public static Set<String>  makeDictionary () {
		Set<String> words = new HashSet<String>();
		Scanner infile = null;
		try {
			infile = new Scanner (new File("five_letter_words.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("Dictionary File not Found!");
			e.printStackTrace();
			System.exit(1);
		}
		while (infile.hasNext()) {
			words.add(infile.next().toUpperCase());
		}
		return words;
	}
}
