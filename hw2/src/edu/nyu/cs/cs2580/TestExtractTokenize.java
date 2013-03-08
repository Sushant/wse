package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.List;

public class TestExtractTokenize {
	public static void main(String[] args) throws Exception {
		String rawData = ExtractText.extractText("/Users/prasadkapde/websearchengine/data/wiki/2011");
		System.out.println(rawData);
		List<String> list = new Tokenizer().tokenize(rawData);
		System.out.println(list);
		List<String> newList = new ArrayList<String>();
		for(String s : list){
			newList.add(new Stemmer().getStemmedWord(s));
		}
		System.out.println(newList);
	}
}
