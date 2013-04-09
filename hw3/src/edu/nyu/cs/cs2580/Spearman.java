package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Spearman {
	private static PersistentStore _persist = PersistentStore.getInstance();
	private static int[] _pageRankArray;
	private static int _corpusSize;
	private static Map<Integer, Integer> _numViewsMap = new HashMap<Integer, Integer>();
	private static double sigma;

	public static void main(String[] args) {
		try {
			String pageRankFile = args[0];
			String numViewsFile = args[1];
			Map<Integer, Float> pageRankMap = _persist
					.loadPageRankMap(pageRankFile);
			Map<Integer, Integer> numViewsMap = _persist
					.loadDocNumViewsMap(numViewsFile);
			pageRankMap = Utility.sortMapByFloatValues(pageRankMap);
			numViewsMap = Utility.sortMapByIntegerValues(numViewsMap);
			_pageRankArray = new int[pageRankMap.size()];
			_corpusSize = pageRankMap.size();
			int counter = 0;
			for (Map.Entry<Integer, Float> entry : pageRankMap.entrySet()) {
				_pageRankArray[counter] = entry.getKey();
				counter++;
			}
			counter = 1;
			for (Map.Entry<Integer, Integer> entry : numViewsMap.entrySet()) {
				_numViewsMap.put(entry.getKey(), counter);
				counter++;
			}
			sigma = calculateCorrelation();
			System.out.println(sigma);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static double calculateCorrelation() {
		double intermediate = 0;
		for (int i = 0; i < _pageRankArray.length; i++) {
			int indexInPageRank = i + 1;
			int indexInNumViews = returnIndex(_pageRankArray[i]);
			intermediate += Math.pow((indexInPageRank - indexInNumViews), 2);
		}
		intermediate *= 6;
		double denominator = Math.pow(_corpusSize, 3) - _corpusSize;
		intermediate /= denominator;
		double sigma = 1 - intermediate;
		return sigma;
	}

	private static int returnIndex(int key) {
		int index = 0;
		if (_numViewsMap.containsKey(key)) {
			index = _numViewsMap.get(key);
		}
		return index;
	}
}
