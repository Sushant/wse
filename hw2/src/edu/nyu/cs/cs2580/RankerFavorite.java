package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

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

  
  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
    Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
    Document doc = null;
    int docid = -1;
    Date start = new Date();
    while ((doc = _indexer.nextDoc(query, docid)) != null) {
      rankQueue.add(queryLikelihoodRanker(query, doc));
      if (rankQueue.size() > numResults) {
        rankQueue.poll();
      }
      docid = doc._docid;
    }

    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    ScoredDocument scoredDoc = null;
    while ((scoredDoc = rankQueue.poll()) != null) {
        results.add(scoredDoc);
    }
    Collections.sort(results, Collections.reverseOrder());
    Date end = new Date();
    return results;
  }
  
  
  public ScoredDocument queryLikelihoodRanker(Query query, Document d) {
	  	Scanner s = new Scanner(query._query);
		Vector<String> qv = new Vector<String>();
		while (s.hasNext()) {
			String term = s.next();
			qv.add(term);
		}
		// Get the document vector. For hw1, you don't have to worry about the
		// details of how index works.
	
		float score = 0;
		Vector<Float> queryRepresentation = new Vector<Float>();
		Set<String> termSet = new HashSet<String>();
		float numOfWordsDoc = (float)d.getTotalWordsInDoc();  // total num of words in document
		float numOfWordsColl = (float)_indexer.totalTermFrequency();   // total number of words in corpus
		float lambda = (float) 0.5;
		float f_qi, c_qi;
		// Create Representation for query. //
		// ///////////////////////////////////////////////////////////////////////
		for (String title : qv) {
			if (termSet.add(title)) {
				f_qi = (float)_indexer.documentTermFrequency(title, d.getUrl());
				float f_qi_d = 0;
				if (numOfWordsDoc != 0) {
					f_qi_d = (1 - lambda) * (f_qi / numOfWordsDoc);
				}
				c_qi = (float)_indexer.corpusTermFrequency(title);
				float c_qi_d = 0;
				if (numOfWordsColl != 0) {
					c_qi_d = lambda * (c_qi / numOfWordsColl);
				}
				queryRepresentation.add(f_qi_d + c_qi_d);
			}
		}

		// Query Likelihood ///
		//////////////////////////////////////////////////////////////////////////
		for (int i = 0; i < queryRepresentation.size(); i++) { 
			score += Math.log(queryRepresentation.get(i))/Math.log(2.0) ;
		}
		score = (float) Math.pow(2, score);
		return new ScoredDocument(d , score);
	}
}
