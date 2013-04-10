package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.Comparator;

class QueryRepresentation {
	int numDocs;
	int numTerms;
	Query query;
	Ranker rankDocs;
	Indexer indexer;
	private final String PRF_DIR = "data/PRF/";
	
	

	QueryRepresentation(Query query, Ranker rankdocs, int numDocs, int numTerms,
			Indexer indexer) {
		this.numDocs = numDocs;
		this.numTerms = numTerms;
		this.rankDocs = rankdocs;
		this.query = query;
		this.indexer = indexer;
	}

	Map<String, Double> computeRepresentation() throws MalformedURLException,
	IOException {
		//System.out.println("numDocs---" + numDocs);
		//System.out.println("numTerms---" + numTerms);
		//System.out.println("query---" + query);

		Map<String, Double> result = new HashMap<String, Double>();

		class Pair {
			String term;
			Double count;

			Pair(String term, Double count) {
				this.term = term;
				this.count = count;
			}
		}

		List<String> fileList = new ArrayList<String>();
		Vector<ScoredDocument> rankedDocuments = rankDocs.runQuery(query,
				numDocs);

		for (ScoredDocument it : rankedDocuments) {
			Document temp = it.get_doc();
			//System.out.println("temp.getUrl()-------" + temp.getUrl());
			fileList.add(temp.getUrl());
		}

		Map<String,Integer> termFreq = Utility.returnUniqueSet(fileList);
		int termsInDoc = termFreq.size();

		/////////////////Finding top m Terms///////////////////////////////////////////////		
		PriorityQueue<Pair> rankQueue = new PriorityQueue<Pair>(numTerms,
				new Comparator<Object>() {
			@Override
			public int compare(Object p1, Object p2) {
				Pair p3 = (Pair) p1;
				Pair p4 = (Pair) p2;
				if (p4.count < p3.count)
					return 1;
				else
					return -1;
			}
		});

		for(Map.Entry<String, Integer> entry : termFreq.entrySet()){
			Pair item = new Pair(entry.getKey(),(new Double(entry.getValue())));
			rankQueue.add(item);
			if (rankQueue.size() > numTerms) {
				rankQueue.poll();
			}
		}
		/////////////////////////////////////////////////////////////////////////////////////

		double total=0;
		for(Pair item: rankQueue){
			item.count = item.count / termsInDoc;
			total = total + item.count;
		}
		
		// Populating Map<String, Double> result from rankQueue
		// & Re-normalizing probabilities so output values sum to 1.
		while(!rankQueue.isEmpty()){
			Pair item = rankQueue.poll();
			item.count = item.count / total; 
			result.put(item.term, item.count);
		}
		
		return result;
	}

}
