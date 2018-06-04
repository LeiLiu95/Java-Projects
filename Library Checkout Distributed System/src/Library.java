import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Library {
	//needed another class for books in library
	class Book{
		int num;
		boolean[] available;
		int[] recordID;
		String[] studentName;
		String bookName;
		
		//constructor for books
		public Book(String bookName, int quantity) {
			this.bookName = bookName;
			this.num = quantity;
			this.available = new boolean[quantity];
			for(int i = 0; i<quantity; i+=1) {
				this.available[i] = true;
			}
			this.studentName = new String[quantity];
			this.recordID = new int[quantity];
			for(int i = 0; i<quantity; i+=1) {
				this.recordID[i] = 0;
			}
		}
	}

	//using arraylist for books
	private ArrayList<Book> library;
	
	private int recordID=1;
	public Library(String file) {
		library = new ArrayList<Book>();

		try {
			Scanner fileReader = new Scanner(new FileReader(file));
			String line;
			while(fileReader.hasNextLine()) {
				line = fileReader.nextLine();

				String[] input = line.split(" ");
				String name = "";
				for(int i = 0; i<input.length-1; i+=1) {
					if(i==0) {
						name += input[i];
					}
					else {
						name += " " + input[i];
					}
				}
				int quantity = Integer.parseInt(input[input.length-1]);
				Book tempBook = new Book(name, quantity);
				library.add(tempBook);
			}
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String getCommand(String command) {
		String[] tokens = command.split(" ");
		switch(tokens[0]) {
			case "borrow": 
				String book ="";
				for(int i = 2; i<tokens.length; i+=1) {
					if(i==2) {
						book += tokens[i];
					}
					else {
						book += " " +tokens[i];
					}
				}
				return borrow(tokens[1], book);
			case "return": return bookReturn(tokens[1]);
			case "inventory": return inventory();
			case "list": 
				return list(tokens[1]);
			case "exit": return exit();
		}
		return "ERROR: No such command";
	}
	
	private synchronized String borrow(String student, String book) {
		for(int i = 0; i < library.size(); i+=1) {
			if(library.get(i).bookName.equals(book)) {
				if(library.get(i).num <=0) {
					return "Request Failed - Book not available";
				}
				for(int j = 0; j < library.get(i).available.length; j+=1) {
					if(library.get(i).available[j] == true) {
						library.get(i).recordID[j]=recordID;
						recordID+=1;
						library.get(i).num-=1;
						library.get(i).studentName[j] = student;
						library.get(i).available[j] = false;
						return ("Your request has been approved, " + library.get(i).recordID[j] + " " + library.get(i).studentName[j] + " " + library.get(i).bookName);
					}
				}
			}
		}
		return "Request Failed - We do not have this book";
		
	}
	
	private synchronized String bookReturn(String bookID) {
		int id = Integer.parseInt(bookID);
		if(id>=recordID) {
			return id + " not found, no such borrow record";
		}
		for(int i = 0; i<library.size(); i+=1) {
			for(int j = 0; j<library.get(i).available.length; j+=1) {
				if(library.get(i).recordID[j] == id && library.get(i).available[j]==false) {
					library.get(i).recordID[j]=0;
					
					library.get(i).num+=1;
					library.get(i).available[j] = true;
					library.get(i).studentName[j] = "";
					return id + " is returned";
				}
			}
		}
		return id + " not found, no such borrow record";
	}
	
	private synchronized String inventory() {
		String inventory = "";
		for(int i = 0; i<library.size(); i+=1) {
			if(inventory.equals("")) {
				inventory += library.get(i).bookName + " " + library.get(i).num;
			}
			else {
				inventory += "\n" + library.get(i).bookName + " " + library.get(i).num;
			}
		}

		return inventory;
	}
	
	private synchronized String list(String student) {
		String books = "";
		for(int i = 0; i<library.size(); i+=1) {
			for(int j = 0; j<library.get(i).studentName.length; j+=1){
				if(student.equals(library.get(i).studentName[j])) {
					if(books.equals("")) {
						books += library.get(i).recordID[j] + " " + library.get(i).bookName;
					}
					else {
						books += "\n" + library.get(i).recordID[j] + " " + library.get(i).bookName;
					}
				}
			}
		}
		if(books.equals("")) {
			return "No record found for " + student;
		}
		return books;
	}
	
	private synchronized String exit() {
		PrintStream inventory = null;
		try {
			inventory = new PrintStream(new File("inventory.txt"));
			String output = "";
			for(int i = 0; i<library.size(); i+=1) {
				if(i==0) {
					output += library.get(i).bookName + " " + library.get(i).num;
				}
				else {
					output += "\n" + library.get(i).bookName + " " + library.get(i).num;
				}
			}
			inventory.write(output.getBytes());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			inventory.close();
		}
		return "";
	}
}
