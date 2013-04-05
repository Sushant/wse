package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedOccurrence extends Indexer {

	// Stores all Document in memory.
	final int BULK_DOC_PROCESSING_SIZE = 300;
	final int BULK_DOC_WRITE_SIZE = 100;
	final String METADATA_FILE = "index.dat";
	private Vector<Document> _documents = new Vector<Document>();
	private Map<String, Map<String, Map<Integer, List<Integer>>>> _characterMap;
	private Map<String, Long> _docMap;
	private Map<String, Set<Integer>> _queryDocSet; // Caching for nextDoc
	private Map<String, Map<Integer, Integer>> _termOccurrencesMap; // Caching for docTermFrequency
	
  public IndexerInvertedOccurrence(Options options) {
    super(options);
    _characterMap = new HashMap<String, Map<String, Map<Integer, List<Integer>>>>();
    _docMap = new HashMap<String, Long>();
    _queryDocSet = new HashMap<String, Set<Integer>>();
    _termOccurrencesMap = new HashMap<String, Map<Integer,Integer>>();
    
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {
	  List<String> documents = Utility.getFilesInDirectory(_options._corpusPrefix);
	  for (String filename: documents) {
		  processDocument(filename);
		  if (_numDocs % BULK_DOC_WRITE_SIZE == 0) {
				_persistentStore.saveDoc(_options._indexPrefix + "/" + String.valueOf(_numDocs) + ".dat", _documents);
				_documents.clear();
			}
		  if (_numDocs % BULK_DOC_PROCESSING_SIZE == 0) {
			  System.out.println("Processed files: " + _numDocs);
			  updateIndexWithMap(_characterMap);
			  _characterMap.clear();
		  }
	  }
	  // Update index with all the remaining files from corpus
	  if (!_characterMap.isEmpty()) { 
		  updateIndexWithMap(_characterMap);
		  _characterMap.clear();
	  }
	  if (!_documents.isEmpty()) {
		  _persistentStore.saveDoc(_options._indexPrefix + "/" + String.valueOf(_numDocs) + ".dat", _documents);
		  _documents.clear();
	  }
	  saveIndexMetadata();
	  System.out.println("Indexed " + Integer.toString(_numDocs) + " docs with " +
		        Long.toString(_totalTermFrequency) + " terms.");
  }

  private void saveIndexMetadata() throws IOException {
	Map<String, Long> dataMap = new HashMap<String, Long>();
	dataMap.put("numDocs", new Long(_numDocs));
	dataMap.put("totalTermFrequency", _totalTermFrequency);
	dataMap.putAll(_docMap);
	String metaDataFile = _options._indexPrefix + "/" + METADATA_FILE;
	_persistentStore.saveIndexMetadata(metaDataFile, dataMap);
  }

  private void processDocument(String filename) throws MalformedURLException, IOException {
	  String corpusFile = _options._corpusPrefix + "/" + filename;
	  int docId = _numDocs;
	  String document = Utility.extractText(corpusFile);
	  List<String> stemmedTokens = Utility.tokenize(document);
	  buildMapFromTokens(docId, filename, stemmedTokens);
	  _numDocs++;
  }
  
  private void updateIndexWithMap(Map<String, Map<String, Map<Integer, List<Integer>>>> characterMap) {
	for (String chars : characterMap.keySet()) {
		Integer docBatch = _numDocs / BULK_DOC_PROCESSING_SIZE;
		String filename = chars + docBatch.toString();
		String indexFile = _options._indexPrefix + "/" + filename + ".idx";
		Map<String, Map<Integer, List<Integer>>> wordMap = characterMap.get(chars);
		try {
			Map<String, Map<Integer, List<Integer>>> loadedWordMap = _persistentStore.load(indexFile);
			for (String word: wordMap.keySet()) {
				Map<Integer, List<Integer>> docMap = wordMap.get(word);
				if (loadedWordMap.containsKey(word)) {
					Map<Integer, List<Integer>> loadedDocMap = loadedWordMap.get(word);
					for (Integer docId: docMap.keySet()) {
						loadedDocMap.put(docId, docMap.get(docId));
					}
				} else {
					loadedWordMap.put(word, docMap);
				}
			}
			_persistentStore.save(indexFile, loadedWordMap);
		} catch (IOException e) {
			try {
				_persistentStore.save(indexFile, wordMap);
			} catch (IOException e1) {
				System.out.println("Failed to write " + indexFile);
			}
		}
	}
  }
  

  private void buildMapFromTokens(int docId, String docName, List<String> tokens) {
	  int tokenIndex = 0;
	  for(String token : tokens){
		  String start;
		  Map<Integer, List<Integer>> newDocMap = new HashMap<Integer, List<Integer>>();
		  List<Integer> newOccurrencesList = new ArrayList<Integer>();
		  newOccurrencesList.add(tokenIndex);
		  newDocMap.put(docId, newOccurrencesList);
			
		  	if (token.length() >= 2) {
		  		start = token.substring(0, 2);
		  	} else {
		  		start = token.substring(0, 1);
		  	}
			if (_characterMap.containsKey(start)) {
				Map<String, Map<Integer, List<Integer>>> wordMap = _characterMap.get(start);
				
				if (wordMap.containsKey(token)) {
					Map<Integer, List<Integer>> docMap = wordMap.get(token);
					if (docMap.containsKey(docId)) {
						docMap.get(docId).add(tokenIndex);
					} else {
						docMap.put(docId, newOccurrencesList);
					}
				} else {
					wordMap.put(token, newDocMap);
				}
			} else {
				Map<String, Map<Integer, List<Integer>>> wordMap = new HashMap<String, Map<Integer, List<Integer>>>();
				wordMap.put(token, newDocMap);
				_characterMap.put(start, wordMap);
			}
			tokenIndex++;
	  }
	  DocumentIndexed doc = new DocumentIndexed(docId);
	  doc.setUrl(docName);
	  doc.setTotalWordsInDoc(tokens.size());
	  _documents.add(doc);
	  _docMap.put(docName, new Long(docId));
	  _totalTermFrequency = _totalTermFrequency + tokens.size();
  }

  
  
  ///// Loading related functions.
  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
	  loadIndexMetadata();
  }

  private void loadIndexMetadata() throws IOException {
	 String metaDataFile = _options._indexPrefix + "/" + METADATA_FILE;
	 _docMap = _persistentStore.loadIndexMetadata(metaDataFile);
	 _totalTermFrequency = _docMap.get("totalTermFrequency");
	 _docMap.remove("totalTermFrequency");
	 _numDocs = _docMap.get("numDocs").intValue();
	 _docMap.remove("numDocs");
  }

@Override
  public Document getDoc(int docid) {
	try {
		int quotient = docid / BULK_DOC_WRITE_SIZE;
		int remainder = docid % BULK_DOC_WRITE_SIZE;
		int docFile = (quotient + 1) * BULK_DOC_WRITE_SIZE;
		String fileName = _options._indexPrefix + "/" + docFile + ".dat";
		List<DocumentIndexed> docs = _persistentStore.loadDoc(fileName);
		return docs.get(remainder);
	} catch (Exception e) {
		return null;
	}
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}.
   */
  @Override
  public Document nextDoc(Query query, int docid) {
	docid++;
	String queryStr = query._query;
	Set<Integer> phraseDocSet = new HashSet<Integer>();
	Set<Integer> tokenDocSet = new HashSet<Integer>();
	Set<Integer> finalDocSet = new HashSet<Integer>();
	if (_queryDocSet.containsKey(queryStr)) {
		finalDocSet = _queryDocSet.get(queryStr);
	} else {
			QueryPhrase queryPhrase = new QueryPhrase(queryStr);
			queryPhrase.processQuery();

			for (Vector<String> phrase : queryPhrase._phraseTokens) {
				if (finalDocSet.isEmpty()) {
					finalDocSet = docSetFromPhraseTokens(phrase, docid);
				} else {
					finalDocSet.retainAll(docSetFromPhraseTokens(phrase, docid));
				}
			}
			if (!finalDocSet.isEmpty() && !queryPhrase._phraseTokens.isEmpty()) {
				phraseDocSet.addAll(finalDocSet);
				if (!queryPhrase._tokens.isEmpty()) {
					tokenDocSet = docSetFromTokens(queryPhrase._tokens, docid);
					finalDocSet.retainAll(tokenDocSet);
				}
			} else {
				if (queryPhrase._phraseTokens.isEmpty()) {
					tokenDocSet = docSetFromTokens(queryPhrase._tokens, docid);
					finalDocSet.addAll(tokenDocSet);
				}
			}

			if (finalDocSet.isEmpty()) {
				return null;
			}
			if (_queryDocSet.size() == 100) {
				_queryDocSet.clear();
			}
			_queryDocSet.put(queryStr, finalDocSet);
	}
	int nextDocId = getMinNextDocId(finalDocSet, docid);
	if (nextDocId == -1) {
		return null;
	}
	return getDoc(nextDocId);
  }
  
  
  private Set<Integer> docSetFromPhraseTokens(Vector<String> phrase, int currentDocId) {
	  Map<String, Map<Integer, List<Integer>>> tokenMap = new HashMap<String, Map<Integer, List<Integer>>>();
		for (String t: phrase) {
			String prefix = Utility.getTermPrefix(t);
			List<String> matchedIndexDocs = Utility.getFileInDirectory(_options._indexPrefix, prefix, ".idx");
			for (String matchedIndexDoc : matchedIndexDocs) {
				try {
					Map<String, Map<Integer, List<Integer>>> wordMap = _persistentStore.load(_options._indexPrefix + "/" + matchedIndexDoc);
					if (wordMap.containsKey(t)) {
						if (tokenMap.containsKey(t)) {
							Map<Integer, List<Integer>> docMap = tokenMap.get(t);
							docMap.putAll(wordMap.get(t));
						} else {
							tokenMap.put(t, wordMap.get(t));
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (tokenMap.keySet().size() != phrase.size()) {
			// We did not find a doc where all the tokens are present
			return new HashSet<Integer>();
		}
		return getDocSetFromPhraseTokenMap(tokenMap, phrase);
  }
  
  private Set<Integer> getDocSetFromPhraseTokenMap(Map<String, Map<Integer, List<Integer>>> tokenMap, Vector<String> phrase) {
	Set<Integer> docSet = getDocSetFromTokenMap(tokenMap);
	Set<Integer> finalOccSet = new HashSet<Integer>();
	Set<Integer> finalDocSet = new HashSet<Integer>();
	for (Integer docId: docSet) {
		int subtractionFactor = 0;
		Map<String, List<Integer>> occurrenceMap = new HashMap<String, List<Integer>>();
		for (String term : phrase) {
			List<Integer> occurrenceList = new ArrayList<Integer>();
			for (Integer occurrence: tokenMap.get(term).get(docId)) {
				occurrenceList.add(occurrence - subtractionFactor); 
			}
			occurrenceMap.put(term, occurrenceList);
			subtractionFactor++;
		}
		for (List<Integer> ol: occurrenceMap.values()) {
			if (finalOccSet.isEmpty()) {
				finalOccSet.addAll(ol);
			} else {
				finalOccSet.retainAll(ol);
			}
		}
		if (!finalOccSet.isEmpty()) {
			finalDocSet.add(docId);
		}
	}
	return finalDocSet;
}

  private Set<Integer> docSetFromTokens(Vector<String> tokens, int currentDocId) {
	  Map<String, Map<Integer, List<Integer>>> tokenMap = new HashMap<String, Map<Integer, List<Integer>>>();
		for (String t: tokens) {
			String prefix = Utility.getTermPrefix(t);
			List<String> matchedIndexDocs = Utility.getFileInDirectory(_options._indexPrefix, prefix, ".idx");
			for (String matchedIndexDoc : matchedIndexDocs) {
				try {
					Map<String, Map<Integer, List<Integer>>> wordMap = _persistentStore.load(_options._indexPrefix + "/" + matchedIndexDoc);
					if (wordMap.containsKey(t)) {
						if (tokenMap.containsKey(t)) {
							Map<Integer, List<Integer>> docMap = tokenMap.get(t);
							docMap.putAll(wordMap.get(t));
						} else {
							tokenMap.put(t, wordMap.get(t));
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (tokenMap.keySet().size() != tokens.size()) {
			// We did not find a doc where all the tokens are present
			return new HashSet<Integer>();
		}
		return getDocSetFromTokenMap(tokenMap);
  }
  
  
  private Set<Integer> getDocSetFromTokenMap(Map<String, Map<Integer, List<Integer>>> tokenMap) {
	  Set<Integer> finalDocSet = new HashSet<Integer>();
	  for (String term: tokenMap.keySet()) {
		  if (finalDocSet.isEmpty()) {
			  finalDocSet = tokenMap.get(term).keySet();
		  } else {
			  finalDocSet.retainAll(tokenMap.get(term).keySet());
		  }
	  }
	  return finalDocSet;
  }
  
  private int getMinNextDocId(Set<Integer> docSet, int currentDocId) {
	  Integer matchedDocId = Integer.MAX_VALUE;
	  for (Integer docId: docSet) {
		  if (docId >= currentDocId && matchedDocId > docId) {
			  matchedDocId = docId;
		  }
	  }
	  if (matchedDocId == Integer.MAX_VALUE) {
		  return -1;
	  } else {
		  return matchedDocId.intValue();
	  }
  }

  @Override
  //Number of documents in which {@code term} appeared, over the full corpus.
  public int corpusDocFrequencyByTerm(String term) {
	  int corpusFrequency = 0;
	  String prefix = Utility.getTermPrefix(term);
	  List<String> matchingDocs = Utility.getFileInDirectory(_options._indexPrefix, prefix, "idx");
	  
	  for (String docName: matchingDocs) {
		  String docPath = _options._indexPrefix + "/" + docName;
		  try {
			Map<String, Map<Integer, List<Integer>>> wordMap = _persistentStore.load(docPath);
			if (wordMap.containsKey(term)) {
				corpusFrequency += wordMap.get(term).size();
			}
		  } catch (IOException e) {
			continue;
		  }
	  }
	  return corpusFrequency;
  }

  @Override
  //Number of times {@code term} appeared in corpus.
  public int corpusTermFrequency(String term) {
	  int corpusTermFrequency = 0;
	  String prefix = Utility.getTermPrefix(term);
	  List<String> matchingDocs = Utility.getFileInDirectory(_options._indexPrefix, prefix, "idx");
	  
	  for (String docName: matchingDocs) {
		  String docPath = _options._indexPrefix + "/" + docName;
		  try {
			Map<String, Map<Integer, List<Integer>>> wordMap = _persistentStore.load(docPath);
			if (wordMap.containsKey(term)) {
				Map<Integer, List<Integer>> docMap = wordMap.get(term);
				for (Integer docId: docMap.keySet()) {
					corpusTermFrequency += docMap.get(docId).size();
				}
			}
		} catch (IOException e) {
			continue;
		}
	  }
	  return corpusTermFrequency;
  }
  

  @Override
  //Number of times {@code term} appeared in the document {@code url}.
  public int documentTermFrequency(String term, String url) {
    if (_docMap.containsKey(url)) {
    	int docId = _docMap.get(url).intValue();
    	return documentTermFrequency(term, docId);
    }
    return 0;
  }
  
  private int documentTermFrequency(String term, int docId) {
	  if (!_termOccurrencesMap.containsKey(term)) {
		String prefix = Utility.getTermPrefix(term);
	  	List<String> matchingDocs = Utility.getFileInDirectory(_options._indexPrefix, prefix, "idx");
	  	Map<Integer, Integer> docFrequencyMap = new HashMap<Integer, Integer>();
	  	for (String docName: matchingDocs) {
		  String docPath = _options._indexPrefix + "/" + docName;
		  try {
			Map<String, Map<Integer, List<Integer>>> wordMap = _persistentStore.load(docPath);
			if (wordMap.containsKey(term)) {
				Map<Integer, List<Integer>> docMap = wordMap.get(term);
				for (Integer dId: docMap.keySet()) {
					docFrequencyMap.put(dId, docMap.get(dId).size());
				}
			}
		  } catch (IOException e) {
			continue;
		  }
	  	}
	  	if (_termOccurrencesMap.size() == 10) {
	  		_termOccurrencesMap.clear();
	  	}
	  	_termOccurrencesMap.put(term, docFrequencyMap);
	  	System.out.println(_termOccurrencesMap);
	  }
	  try {
		  return _termOccurrencesMap.get(term).get(docId);
	  } catch (Exception e) {
		  return 0;
	  }
  }
  
  public static void main(String[] args) throws IOException, ClassNotFoundException {
	Options option = new Options("conf/engine.conf");
	IndexerInvertedOccurrence in = new IndexerInvertedOccurrence(option);
	in.loadIndex();
	//System.out.println(in.corpusDocFrequencyByTerm("wikipedia"));
	//System.out.println(in.documentTermFrequency("0814736521", "Nickelodeon_(TV_channel)"));

	Query q = new Query("\"web\"");

	
	Document doc = null;
	int docid = -1;
	int counter = 0;
	while ((doc = in.nextDoc(q, docid)) != null) {
		counter++;
		docid = doc._docid;
		System.out.println(docid + " " + doc.getUrl());
	}
	System.out.println("Count:  " + counter);
  }
}
