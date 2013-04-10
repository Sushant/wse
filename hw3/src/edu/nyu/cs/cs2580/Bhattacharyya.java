package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Bhattacharyya {
	public static void main(String[] args) throws IOException {
		String pathToPrfOutput = args[0];
		String pathToOutput = args[1];
		List<String> queryList = new ArrayList<String>();
		Map<String,String> filePathMap = new HashMap<String,String>();
		File file = new File(pathToPrfOutput);
		Scanner scan = new Scanner(file);
		while (scan.hasNext()) {
			String next = scan.nextLine();
			String[] array = next.split(":");
			String query = array[0];
			String filePath = array[1];
			queryList.add(query);
			filePathMap.put(query,filePath);
		}
		File fileToWrite = new File(pathToOutput);
		OutputStream out = new FileOutputStream(fileToWrite);
		for (int i = 0; i < queryList.size(); i++) {
			Map<String, Double> outerFileMap = readFile(filePathMap.get(queryList.get(i)));
			for (int j = i + 1; j < queryList.size(); j++) {
				double bhattacharya = 0;
				Map<String, Double> innerFileMap = readFile(filePathMap.get(queryList.get(j)));
				Set<String> set = createSet(outerFileMap, innerFileMap);
				for (String word : set) {
					double probabilityOuterMap = outerFileMap.get(word);
					double probabilityInnerFileMap = innerFileMap.get(word);
					bhattacharya += Math
							.sqrt((probabilityOuterMap * probabilityInnerFileMap));
				}
				out.write(queryList.get(i).getBytes());
				out.write("\t".getBytes());
				out.write(queryList.get(j).getBytes());
				out.write("\t".getBytes());
				out.write(String.valueOf(bhattacharya).getBytes());
				out.write("\n".getBytes());
			}
		}
	}

	private static Map<String, Double> readFile(String fileName)
			throws FileNotFoundException {
		Map<String, Double> queryRepresentationMap = new HashMap<String, Double>();
		File file = new File(fileName);
		Scanner scan = new Scanner(file);
		while (scan.hasNext()) {
			String line = scan.nextLine();
			String[] value = line.split("\t");
			queryRepresentationMap.put(value[0], Double.parseDouble(value[1]));
		}
		return queryRepresentationMap;
	}

	private static Set<String> createSet(Map<String, Double> map1,
			Map<String, Double> map2) {
		Set<String> returnSet = new HashSet<String>();
		returnSet.addAll(map1.keySet());
		returnSet.retainAll(map2.keySet());
		return returnSet;
	}
}
