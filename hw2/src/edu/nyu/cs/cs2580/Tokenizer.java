package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

class Tokenizer {
	public List<String> tokenize(String inputString) {
		List<String> tokens = new ArrayList<String>();
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		Scanner scan = new Scanner(inputString);
		scan.useDelimiter(pattern);
		while (scan.hasNext()) {
			tokens.add(scan.next());
		}
		return tokens;
	}
}
