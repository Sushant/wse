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
	private Map<String, Long> _docMap;
	
  public IndexerInvertedOccurrence(Options options) {
    super(options);
    _characterMap = new HashMap<String, Map<String, Map<Integer, List<Integer>>>>();
    _docMap = new HashMap<String, Long>();
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
	  buildMapFromTokens(docId, stemmedTokens);
	  DocumentIndexed doc = new DocumentIndexed(docId);
	  doc.setUrl(filename);
	  _documents.add(doc);
	  _docMap.put(filename, new Long(docId));
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
	 String metaDataFile = _options._indexPrefix + "/" + METADATA_FILE;
	 _docMap = _persistentStore.loadIndexMetadata(metaDataFile);
	 _totalTermFrequency = _docMap.get("totalTermFrequency");
	 _docMap.remove("totalTermFrequency");
	 _numDocs = _docMap.get("numDocs").intValue();
	 _docMap.remove("numDocs");
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
	  query.processQuery();
	  List<String> queryVector = query._tokens;
    return null;
  }

  @Override
  //Number of documents in which {@code term} appeared, over the full corpus.
  public int corpusDocFrequencyByTerm(String term) {
	  int corpusFrequency = 0;
	  String prefix = getTermPrefix(term);
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
	  String prefix = getTermPrefix(term);
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
  
  private String getTermPrefix(String term) {
	  if (term.length() >= 2) {
		  return term.substring(0, 2);
	  } else {
	  	  return term.substring(0, 1);
	  }
  }

  @Override
  //Number of times {@code term} appeared in the document {@code url}.
  public int documentTermFrequency(String term, String url) {
    int docTermFrequency = 0;
    if (_docMap.containsKey(url)) {
    	int docId = _docMap.get(url).intValue();
    	String prefix = getTermPrefix(term);
  	  	List<String> matchingDocs = Utility.getFileInDirectory(_options._indexPrefix, prefix, "idx");
  	  
  	  	for (String docName: matchingDocs) {
  		  String docPath = _options._indexPrefix + "/" + docName;
  		  try {
  			Map<String, Map<Integer, List<Integer>>> wordMap = _persistentStore.load(docPath);
  			if (wordMap.containsKey(term)) {
  				Map<Integer, List<Integer>> docMap = wordMap.get(term);
  				if (docMap.containsKey(docId)) {
  					docTermFrequency += docMap.get(docId).size();
  					break;
  				}
  			}
  		  } catch (IOException e) {
  			continue;
  		  }
  	  	}
    }
    return docTermFrequency;
  }
  
  public static void main(String[] args) throws IOException, ClassNotFoundException {
	Options option = new Options("conf/engine.conf");
	IndexerInvertedOccurrence in = new IndexerInvertedOccurrence(option);
	in.loadIndex();
	//System.out.println(in.corpusDocFrequencyByTerm("wikipedia"));
	//System.out.println(in.documentTermFrequency("0814736521", "Nickelodeon_(TV_channel)"));
  }
}
