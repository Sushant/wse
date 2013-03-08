package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Tokenizer {
	public List<String> tokenize(String input){
		List<String> tokens = new ArrayList<String>();
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		Scanner scan = new Scanner(input);
		scan.useDelimiter(pattern);
	     while (scan.hasNext()) {
	    	 String next = scan.next();
	    	 next.trim();
	    	 if(!next.isEmpty())
	    		 tokens.add(next);
	     }
	     return tokens;
	}
}
