package edu.nyu.cs.cs2580;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

class Ranker {
	private Index _index;
	private static Map<String, Double> IDFMap = new HashMap<String, Double>();

	public Ranker(String index_source) {
		_index = new Index(index_source);
	}

	public Vector<ScoredDocument> runquery(String query) {
		Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
		for (int i = 0; i < _index.numDocs(); ++i) {
			//retrieval_results.add(runquery(query, i));
			//retrieval_results.add(cosineRanker(query, i));
			retrieval_results.add(phraseRanker(query, i));
		}
		return retrieval_results;
	}

	public ScoredDocument runquery(String query, int did) {

		// Build query vector
		Vector<String> qv = buildQueryVector(query);

		// Get the document vector. For hw1, you don't have to worry about the
		// details of how index works.
		Document d = _index.getDoc(did);
		Vector<String> dv = d.get_title_vector();

		// Score the document. Here we have provided a very simple ranking
		// model,
		// where a document is scored 1.0 if it gets hit by at least one query
		// term.
		double score = 0.0;
		for (int i = 0; i < dv.size(); ++i) {
			for (int j = 0; j < qv.size(); ++j) {
				if (dv.get(i).equals(qv.get(j))) {
					score = 1.0;
					break;
				}
			}
		}

		return new ScoredDocument(did, d.get_title_string(), score);
	}

	public ScoredDocument cosineRanker(String query, int did) {
		Vector<String> qv = buildQueryVector(query);

		// Get the document vector. For hw1, you don't have to worry about the
		// details of how index works.
		Document d = _index.getDoc(did);
		Vector<String> dv = d.get_title_vector();
		double score = 0;
		dv.addAll(d.get_body_vector());
		Vector<Double> docRepresentation = new Vector<Double>();
		Vector<Double> queryRepresentation = new Vector<Double>();
		Map<String, Integer> docFrequencyMap = returnDocumentFrequencyMap(dv);
		Map<String, Integer> queryFrequencyMap = returnDocumentFrequencyMap(qv);
		Set<String> termSet = new HashSet<String>();

		// ///////////////////////////////////////////////////////////////////////
		// Create Representation for document and query. ///
		// ///////////////////////////////////////////////////////////////////////
		for (String title : dv) {
			if (termSet.add(title)) {
				int _tf = docFrequencyMap.get(title);
				double _idf = returnIDF(title);
				double _tf_idf = _tf * _idf;
				docRepresentation.add(_tf_idf);
				if (qv.contains(title)) {
					int _tfQ = queryFrequencyMap.get(title);
					double _idfQ = returnIDF(title);
					double _tf_idfQ = _tfQ * _idfQ;
					queryRepresentation.add(_tf_idfQ);
				} else {
					queryRepresentation.add(0.0);
				}
			}
		}
		// ////////////////////////////////////////////////////////////////////////

		// ////////////////////////////////////////////////////////////////////////
		// Cosine Ranker ///
		// ////////////////////////////////////////////////////////////////////////
		double numerator = 0.0;
		double documentSquare = 0.0;
		double querySquare = 0.0;
		for (int i = 0; i < docRepresentation.size(); i++) {
			numerator = numerator
					+ (docRepresentation.get(i) * queryRepresentation.get(i));
			documentSquare = documentSquare
					+ Math.pow(docRepresentation.get(i), 2);
			querySquare = querySquare + Math.pow(queryRepresentation.get(i), 2);
		}
		double denominator = 0.0;
		denominator = Math.pow((documentSquare * querySquare), 0.5);
		if (denominator != 0) {
			score = numerator / denominator;
		}else{
			score = 0;
		}
		return new ScoredDocument(did, d.get_title_string(), score);
	}

	private double returnIDF(String s) {
		if (IDFMap.containsKey(s)) {
			return IDFMap.get(s);
		} else {
			int termFrequencyInAllDocuments = Document.documentFrequency(s);
			int numberOfTotalDocuments = _index.numDocs();
			double calcIdf = 1 + (Math.log(numberOfTotalDocuments
					/ termFrequencyInAllDocuments) / Math.log(2));
			IDFMap.put(s, calcIdf);
			return calcIdf;
		}
	}

	private Map<String, Integer> returnDocumentFrequencyMap(
			Vector<String> docVector) {
		Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
		for (String s : docVector) {
			if (frequencyMap.containsKey(s)) {
				Integer frequency = frequencyMap.get(s) + 1;
				frequencyMap.put(s, frequency);
			} else {
				frequencyMap.put(s, 1);
			}
		}
		return frequencyMap;
	}
	
	
	private Vector < String > buildQueryVector(String query) {
		Scanner s = new Scanner(query);
		Vector<String> qv = new Vector<String>();
		while (s.hasNext()) {
			String term = s.next();
			qv.add(term);
		}
		return qv;
	}
	
	public ScoredDocument phraseRanker(String query, int did){
		String queryStr = query + "\t" + query + "\t" + "-1";
		Document queryDocument = new Document(-1, queryStr);
		Vector < String > qpv = queryDocument.get_phrase_vector();
		Document d = _index.getDoc(did);
		int score = 0;
		if (qpv.size() == 0 && query.length() > 0) {
			score += d.getTermFrequency(query);
		} else {
			for (String s: qpv) {
				score += d.getPhraseFrequency(s);
			}
		}
		return new ScoredDocument(did, d.get_title_string(), score);
	}
}
