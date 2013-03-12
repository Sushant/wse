package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			if (stemmedToken.matches("[a-zA-Z0-9]*")) {
				stemmedToken = Stemmer.getStemmedWord(stemmedToken
						.toLowerCase());
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
		Analyzer an = new EnglishAnalyzer(Version.LUCENE_30, set);
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

	private static List<Byte> compressByte(Integer input) {
		String st = Integer.toBinaryString(input);
		int size = st.length();
		int index = 0;
		System.out.println("Size " + st.length());
		int iteration = size / 8;
		int padding = 8 - (size % 8);
		String[] binary = new String[iteration+1];
		int begin = size - 8;
		int end = size;
		System.out.println(iteration);
		while (iteration > 0){
			binary[index] = st.substring(begin, end);
			end = begin;
			begin = begin - 8;
			iteration--;
			index++;
		}
		String temp = "";
		while(padding > 0){
			temp += "0";
			padding--;
		}
		temp += st.substring(0,end);
		binary[index] = temp;
		List<Byte> returnList = new ArrayList<Byte>();
		for(int i = binary.length - 1 ; i >= 0 ; i--){
			System.out.println(binary[i]);
			int tempByte = Integer.parseInt(binary[i],2);
			returnList.add((byte)(tempByte));
		}
		System.out.println(returnList);
		return returnList;
	}
	private static int decompressFromListOfBytes(List<Byte> list){
		String binary ="";
		for(Byte b : list){
			int i = b;
			if(b.toString().contains("-")){
			i = (int) b & 0xff;
			}
			String temp = Integer.toBinaryString(i);
			int size = temp.length();
			int padding = 8 - size;
			char[] pads = new char[padding];
			Arrays.fill(pads,'0');
			String padString = new String(pads);
			temp = padString + temp;
			binary += temp;
		}
		System.out.println(binary);
		return Integer.parseInt(binary, 2);
	}
	private static Byte compress(Integer input) {
		String binary = Integer.toBinaryString(input);
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
		Byte bytes = new Byte((byte) i);
		return bytes;
	}

	private static Integer decompress(Byte by) {
		String i1 = String.valueOf(by);
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
		return i5;
	}

	public static List<List<Byte>> createCompressedList(Map<Integer, List<Integer>> map) {
		System.out.println(map);
		List<Integer> returnList = new ArrayList<Integer>();
		Integer nextKey = 0;
		for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
			returnList.add(entry.getKey() - nextKey);
			nextKey = entry.getKey();
			returnList.add(entry.getValue().size());
			List<Integer> tempList = entry.getValue();
			returnList.add(tempList.get(0));
			for (int i = 1; i < tempList.size(); i++) {
				returnList.add(tempList.get(i) - tempList.get(i - 1));
			}
		}
		System.out.println(returnList);
		List<List<Byte>> compressList = new ArrayList<List<Byte>>();
		for (Integer s : returnList) {
			List<Byte> tempList = compressByte(s);
			compressList.add(tempList);
		}
		return compressList;
	}

	public static Map<Integer, List<Integer>> createDecompressedMap(
			List<List<Byte>> list1) {
		int size = 0;
		Map<Integer, List<Integer>> map = new LinkedHashMap<Integer, List<Integer>>();
		Integer st = 0;
		Integer lastSt = 0;
		List<Integer> list = new ArrayList<Integer>();
		for (List<Byte> b : list1) {
			Integer normal = decompressFromListOfBytes(b);
			list.add(normal);
		}
		while (size < list.size()) {
			st = list.get(size);
			st = st + lastSt;
			lastSt = st;
			size = size + 1;
			int tempSize = list.get(size);
			System.out.println("size:" + tempSize);
			size++;
			int temp = 0;
			List<Integer> tempList = new ArrayList<Integer>();
			while (temp < tempSize) {
				tempList.add(list.get(size));
				size++;
				temp++;
			}
			map.put(st, tempList);
		}
		for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
			List<Integer> returnList = new ArrayList<Integer>();
			List<Integer> tempList = entry.getValue();
			returnList.add(tempList.get(0));
			for (int i = 1; i < tempList.size(); i++) {
				returnList.add((returnList.get(returnList.size() - 1))
						+ tempList.get(i));
			}
			entry.setValue(returnList);
		}
		return map;
	}

	public static void main(String[] args) throws MalformedURLException,
			IOException {
		List<Integer> tempList = new ArrayList<Integer>();
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		tempList.add(1);
		tempList.add(7);
		map.put(1, tempList);

		tempList = new ArrayList<Integer>();
		tempList.add(6);
		tempList.add(17);
		tempList.add(197);
		map.put(2, tempList);

		tempList = new ArrayList<Integer>();
		tempList.add(1);
		map.put(3, tempList);
		System.out.println("Map -- " + map);
		List<List<Byte>> list1 = createCompressedList(map);
		System.out.println("CompressList -- " + list1);
		System.out.println("Decompress -- " + createDecompressedMap(list1));
			/*System.out.println(Integer.toBinaryString(127896));
			List<Byte> list = compressByte(127896);
			System.out.println(decompressFromListOfBytes(list));
			System.out.println();*/
		}
}
