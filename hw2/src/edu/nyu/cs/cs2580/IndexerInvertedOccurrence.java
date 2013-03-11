package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
  public IndexerInvertedOccurrence(Options options) {
    super(options);
    _characterMap = new HashMap<String, Map<String, Map<Integer, List<Integer>>>>();
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {
	  List<String> documents = Utility.getFilesInDirectory(_options._corpusPrefix);
	  for (String filename: documents) {
		  processDocument(filename);
		  if (_documents.size() % BULK_DOC_PROCESSING_SIZE == 0) {
			  System.out.println("Processed files: " + _documents.size());
			  updateIndexWithMap(_characterMap);
			  _characterMap.clear();
		  }
	  }
	  // Update index with all the remaining files from corpus
	  if (_characterMap != null) { 
		  updateIndexWithMap(_characterMap);
	  }
	  saveIndexMetadata();
	  System.out.println("Indexed " + Integer.toString(_numDocs) + " docs with " +
		        Long.toString(_totalTermFrequency) + " terms.");
  }

  private void saveIndexMetadata() throws IOException {
	Map<String, Long> dataMap = new HashMap<String, Long>();
	dataMap.put("numDocs", new Long(_numDocs));
	dataMap.put("totalTermFrequency", _totalTermFrequency);
	String metaDataFile = _options._indexPrefix + "/" + METADATA_FILE;
	_persistentStore.saveIndexMetadata(metaDataFile, dataMap);
  }

  private void processDocument(String filename) throws MalformedURLException, IOException {
	  String corpusFile = _options._corpusPrefix + "/" + filename;
	  
	  int docId = _documents.size();
	  String document;
	  document = Utility.extractText(corpusFile);
	  List<String> stemmedTokens = Utility.tokenize(document);
	  buildMapFromTokens(docId, stemmedTokens);
	  _documents.add(new DocumentIndexed(docId));
	  _numDocs++;
  }
  
  private void updateIndexWithMap(Map<String, Map<String, Map<Integer, List<Integer>>>> characterMap) {
	for (String chars : characterMap.keySet()) {
		Integer docBatch = _documents.size() / BULK_DOC_PROCESSING_SIZE;
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
  

  private void buildMapFromTokens(int docId, List<String> tokens) {
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
	  _totalTermFrequency = _totalTermFrequency + tokens.size();
  }

  
  
  ///// Loading related functions.
  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
	  loadIndexMetadata();
  }

  private void loadIndexMetadata() throws IOException {
	 String metaDataFile = _options._indexPrefix + "/index.dat";
	 Map<String, Long> dataMap = _persistentStore.loadIndexMetadata(metaDataFile);
	 _totalTermFrequency = dataMap.get("totalTermFrequency");
	 _numDocs = dataMap.get("numDocs").intValue();
  }

@Override
  public Document getDoc(int docid) {
    SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
    return null;
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}.
   */
  @Override
  public Document nextDoc(Query query, int docid) {
    return null;
  }

  @Override
  public int corpusDocFrequencyByTerm(String term) {
    return 0;
  }

  @Override
  public int corpusTermFrequency(String term) {
    return 0;
  }

  @Override
  public int documentTermFrequency(String term, String url) {
    SearchEngine.Check(false, "Not implemented!");
    return 0;
  }
}
