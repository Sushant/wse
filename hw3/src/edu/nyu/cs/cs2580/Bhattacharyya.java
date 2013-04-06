package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Bhattacharyya {
	public static void main(String[] args) throws FileNotFoundException {
		String pathToPrfOutput = args[0];
		if(pathToPrfOutput.lastIndexOf("/") != pathToPrfOutput.length()-1){
			pathToPrfOutput +="/";
		}
		String pathToOutput = args[1];
		if(pathToOutput.lastIndexOf("/") != pathToOutput.length()-1){
			pathToOutput +="/";
		}
		List<String> queryList = new ArrayList<String>();
		File file = new File("data/queries.tsv");
		Scanner scan = new Scanner(file);
		while (scan.hasNext()) {
			String next = scan.nextLine();
			queryList.add(next);
		}
		for (int i = 0; i < queryList.size(); i++) {
			Map<String, Double> outerFileMap = readFile(pathToPrfOutput+queryList.get(i));
			for (int j = i + 1; j < queryList.size(); j++) {
				double bhattacharya = 0;
				Map<String, Double> innerFileMap = readFile(pathToPrfOutput+queryList.get(j));
				Set<String> set = createSet(outerFileMap, innerFileMap);
				for (String word : set) {
					double probabilityOuterMap = outerFileMap.get(word);
					double probabilityInnerFileMap = innerFileMap.get(word);
					bhattacharya += Math
							.sqrt((probabilityOuterMap * probabilityInnerFileMap));
				}
				System.out.println("QUERY1: " + queryList.get(i) + "QUERY2: "
						+ queryList.get(j) + "COEFFICIENT: " + bhattacharya);
			}
		}
	}

	private static Map<String, Double> readFile(String fileName)
			throws FileNotFoundException {
		Map<String, Double> queryRepresentationMap = new HashMap<String, Double>();
		File file = new File("data/PRF/" + fileName);
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
