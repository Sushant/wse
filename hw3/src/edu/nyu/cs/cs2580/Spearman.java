package edu.nyu.cs.cs2580;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Spearman {
	private static PersistentStore _persist = PersistentStore.getInstance();
	private static int[] _pageRankArray;
	private static int[] _numViewArray;
	private static int _corpusSize;
	private static double sigma;
	public static void main(String[] args) {
		try {
			String pageRankFile = args[0];
			String numViewsFile = args[1];
			Map<Integer, DocumentIndexed> tempPageRankMap = _persist
					.loaddocIdMapPageRankPrepare(pageRankFile);
			Map<Integer, Float> pageRankMap = new HashMap<Integer, Float>();
			Map<Integer, Integer> numViewsMap = _persist
					.loadDocNumViewsMap(numViewsFile);
			for (Map.Entry<Integer, DocumentIndexed> entry : tempPageRankMap
					.entrySet()) {
				pageRankMap.put(entry.getKey(), entry.getValue().getPageRank());
			}
			pageRankMap = Utility.sortMapByFloatValues(pageRankMap);
			numViewsMap = Utility.sortMapByIntegerValues(numViewsMap);
			_pageRankArray = new int[pageRankMap.size()];
			_corpusSize = pageRankMap.size();
			_numViewArray = new int[numViewsMap.size()];
			int counter = 0;
			for (Map.Entry<Integer, Float> entry : pageRankMap.entrySet()) {
				_pageRankArray[counter] = entry.getKey();
				counter++;
			}
			counter = 0;
			for (Map.Entry<Integer, Integer> entry : numViewsMap.entrySet()) {
				_numViewArray[counter] = entry.getKey();
				counter++;
			}

			sigma = calculateCorrelation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static double calculateCorrelation() {
		double intermediate = 0;
		for(int i = 0 ; i < _pageRankArray.length ; i++){
			int indexInPageRank = i;
			int indexInNumViews = returnIndex(_numViewArray,_pageRankArray[i]);
			intermediate += Math.pow((indexInPageRank - indexInNumViews), 2);
		}
		intermediate *= 6;
		double denominator = Math.pow(_corpusSize, 3) - _corpusSize;
		intermediate /= denominator;
		double sigma = 1 - intermediate;
		return sigma;
	}
	private static int returnIndex(int[] tempArray, int key){
		int index = Arrays.binarySearch(tempArray, key);
		return index > 0 ? index : 0 ;
	}
}
