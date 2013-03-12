package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

class Utility {
	public static List<String> tokenize(String input) throws IOException {
		List<String> tempTokens = new ArrayList<String>();
		TokenStream stream = analyze(input);
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		while (stream.incrementToken()) {
			String stemmedToken = cattr.toString().trim();
			if(stemmedToken.matches("[a-zA-Z0-9]*")){
				stemmedToken = Stemmer.getStemmedWord(stemmedToken.toLowerCase());
				tempTokens.add(stemmedToken);
			}
		}
		stream.end();
		stream.close();
		return tempTokens;
	}

	private static TokenStream analyze(String input) {
		Set<String> set = new HashSet<String>();
		set.add("a");
		set.add("b");
		Analyzer an = new EnglishAnalyzer(Version.LUCENE_30,
				set);
		TokenStream stream = an
				.tokenStream("FileName", new StringReader(input));
		an.close();
		return stream;
	}

	public static String extractText(String url) throws MalformedURLException,
			IOException {
		String sourceUrlString = url;
		if (sourceUrlString.indexOf(':') == -1)
			sourceUrlString = "file:" + sourceUrlString;
		Source source = new Source(new URL(sourceUrlString));
		source.fullSequentialParse();
		return source.getTextExtractor().toString();
	}

	public static List<String> getFilesInDirectory(String directory) {
		File folder = new File(directory);
		List<String> files = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			files.add(fileEntry.getName());
		}
		return files;
	}
	
	public static List<String> getFileInDirectory(String directory, String prefix, String extension) {
		File folder = new File(directory);
		List<String> files = new ArrayList<String>();
		
		for (final File fileEntry : folder.listFiles()) {
			String filename = fileEntry.getName();
			if (filename.endsWith(extension) && filename.startsWith(prefix)) {
				files.add(filename);
			}
		}
		return files;
	}
	
	private static String compress(String input){
		Integer integer = Integer.parseInt(input);
		String binary = Integer.toBinaryString(integer);
		String st = "";
		int counter = 1;
		boolean flag = true;
		for (int i = binary.length() - 1; i >= 0; i--) {
			if (counter == 8) {
				if (flag) {
					st += "1";
					st += binary.charAt(i);
					flag = false;
				} else {
					st += "0";
					st += binary.charAt(i);
				}
				counter = 1;
			} else {
				st += binary.charAt(i);
			}
			counter++;
		}
		int size = st.length();
		String temp = "";
		flag = true;
		while (size % 8 != 0) {
			if (size < 8) {
				if (flag) {
					temp += "1";
					flag = false;
				} else {
					temp += "0";
				}
			} else {
				temp += "0";
			}
			size++;
		}
		String finalSt = "";
		for (int i = st.length() - 1; i >= 0; i--) {
			finalSt += st.charAt(i);
		}
		finalSt = temp + finalSt;
		System.out.println(finalSt);
		int i = Integer.parseInt(finalSt, 2);
		String hexString = Integer.toHexString(i);
		return hexString;
	}
	private static String decompress(String s) {
		Integer i = Integer.parseInt(s, 16);
		String i1 = Integer.toBinaryString(i);
		int counter = 1;
		String ft = "";
		for (int x = i1.length() - 1; x >= 0; x--) {
			if (counter != 8) {
				ft += i1.charAt(x);
			} else {
				counter = 0;
			}
			counter++;
		}
		String gt = "";
		for (int x = ft.length() - 1; x >= 0; x--) {
			gt += ft.charAt(x);
		}
		Integer i5 = Integer.parseInt(gt, 2);
		return i5.toString();
	}
	public static List<String> createCompressedList(List<String> list) {
		System.out.println(list);
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		for (int i = 0; i < list.size() - 1; i = i + 2) {
			if (map.containsKey(list.get(i))) {
				List<String> tempList = map.get(list.get(i));
				tempList.add(list.get(i + 1));
			} else {
				List<String> tempList = new ArrayList<String>();
				tempList.add(list.get(i + 1));
				map.put(list.get(i), tempList);
			}
		}
		List<String> returnList = new ArrayList<String>();
		String nextKey = "0";
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			returnList.add(String.valueOf(Integer.parseInt(entry.getKey()) - Integer.parseInt(nextKey)));
			nextKey = entry.getKey();
			returnList.add(String.valueOf(entry.getValue().size()));
			List<String> tempList = entry.getValue();
			returnList.add(tempList.get(0));
			for (int i = 1; i < tempList.size(); i++) {
				returnList.add(String.valueOf(Integer.parseInt(tempList.get(i)) - Integer.parseInt(tempList.get(i-1))));
			}
		}
		List<String> compressList = new ArrayList<String>();
		for(String s : returnList){
			String hex = compress(s);
			compressList.add(hex);
		}
		return compressList;
	}
	
	public static Map<String,List<String>> createDecompressedMap(List<String> list1){
		int size = 0;
		Map<String,List<String>> map = new LinkedHashMap<String,List<String>>();
		String st = "";
		Integer lastSt = 0;
		List<String> list = new ArrayList<String>();
		for(String s : list1){
			String normal = decompress(s);
			list.add(normal);
		}
		while(size < list.size()){
			st = list.get(size);
			st = String.valueOf(Integer.parseInt(st) + lastSt);
			lastSt = Integer.parseInt(st);
			size = size + 1;
			int tempSize = Integer.parseInt(list.get(size));
			System.out.println("size:" + tempSize);
			size++;
			int temp = 0;
			List<String> tempList = new ArrayList<String>();
			while (temp < tempSize ){
				tempList.add(list.get(size));
				size++;
				temp++;
			}
			map.put(st, tempList);
		}
		for(Map.Entry<String, List<String>> entry : map.entrySet()){
			List<String> returnList = new ArrayList<String>();
			List<String> tempList = entry.getValue();
			returnList.add(tempList.get(0));
			for(int i = 1 ; i < tempList.size() ; i++){
				returnList.add(String.valueOf(Integer.parseInt(returnList.get(returnList.size() - 1)) + Integer.parseInt(tempList.get(i))));
			}
			entry.setValue(returnList);
		}
	return map;
	}
	public static void main(String[] args) throws MalformedURLException,
			IOException {
		List<String> tempList = new ArrayList<String>();
		tempList.add("1");
		tempList.add("1");
		tempList.add("1");
		tempList.add("7");
		tempList.add("2");
		tempList.add("6");
		tempList.add("2");
		tempList.add("17");
		tempList.add("2");
		tempList.add("197");
		tempList.add("3");
		tempList.add("1");
		System.out.println("List -- " + tempList);
		List<String> list1 = createCompressedList(tempList);
		System.out.println("CompressList -- " + list1);
		System.out.println("Decompress -- " + createDecompressedMap(list1));
		
		System.out.println(getFileInDirectory("data/index", "i", "dat"));
	}
}
