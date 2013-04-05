package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2 based on a refactoring of your favorite
 * Ranker (except RankerPhrase) from HW1. The new Ranker should no longer rely
 * on the instructors' {@link IndexerFullScan}, instead it should use one of
 * your more efficient implementations.
 */
public class RankerFavorite extends Ranker {

  public RankerFavorite(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
  }
  Float numOfWordsColl = (float)_indexer.totalTermFrequency();
  Map <String, Float> c_qi_Map = new HashMap <String, Float>();  
  Vector<String> qv = new Vector<String>();
	
  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
    Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
    Document doc = null;
    int docid = -1;
    Date start = new Date();
    ////////////////////// Construction of Query Vector///////////////////////////////////
    Scanner s1 = new Scanner(query._query);
	Pattern pattern = Pattern.compile(
	   "\"[^\"]*\"" );
	String token, modifiedToken;
	while ((token = s1.findInLine(pattern)) != null) {
		int end = token.length() -1;
		modifiedToken = token.substring(1, end);
		query._query = query._query.replace(token,modifiedToken);
	}
	s1.close();
	
	Scanner s = new Scanner(query._query);
	while (s.hasNext()) {
		String term = s.next();
		qv.add(term);
	}
	s.close();
	
    ////////////////////// Iterating over Doc Vector////////////////
	while((doc = _indexer.nextDoc(query, docid)) != null) {
    	rankQueue.add(queryLikelihoodRanker(query, doc));
    	if (rankQueue.size() > numResults) {
        rankQueue.poll();
      }
    	docid =  doc._docid;
    }
    //////////////////////////////////////////////////////////////////////
    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    ScoredDocument scoredDoc = null;
    while ((scoredDoc = rankQueue.poll()) != null) {
        results.add(scoredDoc);
    }
    Collections.sort(results, Collections.reverseOrder());
    Date end = new Date();
    System.out.println("start---------"+ start);
    System.out.println("end---------"+ end);
    return results;
  }
  
  
  public ScoredDocument queryLikelihoodRanker(Query query, Document d) { 	
		float score = 0;
		float numOfWordsDoc = (float)d.getTotalWordsInDoc();  
		float lambda = (float) 0.5;
		float f_qi, c_qi;
		// Create Representation for query. //
		// ///////////////////////////////////////////////////////////////////////
		for (String title : qv) {
			f_qi = (float)_indexer.documentTermFrequency(title, d.getUrl());
			float f_qi_d = 0;
			if (numOfWordsDoc != 0) {
				f_qi_d = (1 - lambda) * (f_qi / numOfWordsDoc);
			}
			c_qi = 0.0f;
			if(c_qi_Map.containsKey(title)){
				c_qi = c_qi_Map.get(title);
			}else{
				c_qi = (float)_indexer.corpusTermFrequency(title);
				c_qi_Map.put(title, c_qi);
			}
			float c_qi_d = 0;
			if (numOfWordsColl != 0) {
				c_qi_d = lambda * (c_qi / numOfWordsColl);
			}
			score += Math.log((f_qi_d + c_qi_d)/Math.log(2.0));
		}

		// Query Likelihood ///
		score = (float) Math.pow(2, score);
		//System.out.println("score--------"+score);
		return new ScoredDocument(d , score);
	}
}
