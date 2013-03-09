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
	final int BULK_WRITE_SIZE = 300;
	private Vector<Document> _documents = new Vector<Document>();
	private Map<String, Map<String, List<Integer>>> _characterMap;
	
  public IndexerInvertedOccurrence(Options options) {
    super(options);
    _characterMap = new HashMap<String, Map<String, List<Integer>>>();
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {
	  List<String> documents = Utility.getFilesInDirectory(_options._corpusPrefix);
	  for (String filename: documents) {
		  processDocument(filename);
		  if (_documents.size() % BULK_WRITE_SIZE == 0) {
			  System.out.println("Processed files: " + _documents.size());
			  updateIndexWithMap(_characterMap);
			  _characterMap = new HashMap<String, Map<String, List<Integer>>>();
		  }
	  }
	  if (_characterMap != null) {
		  updateIndexWithMap(_characterMap);
	  }
  }

  private void processDocument(String filename) throws MalformedURLException, IOException {
	  String corpusFile = _options._corpusPrefix + "/" + filename;
	  
	  int docId = _documents.size();
	  String document;
	  document = Utility.extractText(corpusFile);
	  List<String> stemmedTokens = Utility.tokenize(document);
	  buildMapFromTokens(docId, stemmedTokens);
	  _documents.add(new DocumentIndexed(docId));
  }
  
  private void updateIndexWithMap(Map<String, Map<String, List<Integer>>> characterMap) {
	for (String chars : characterMap.keySet()) {
		Integer docBatch = _documents.size() / BULK_WRITE_SIZE;
		String filename = chars + docBatch.toString();
		String indexFile = _options._indexPrefix + "/" + filename + ".idx";
		Map<String, List<Integer>> wordMap = characterMap.get(chars);
		try {
			Map<String, List<Integer>> loadedWordMap = _persistentStore.load(indexFile);
			for (String word: wordMap.keySet()) {
				List<Integer> docList = wordMap.get(word);
				if (loadedWordMap.containsKey(word)) {
					loadedWordMap.get(word).addAll(docList);
					//Map<Integer, List<Integer>> loadedDocMap = loadedWordMap.get(word);
					/*for (Integer docId: docMap.keySet()) {
						loadedDocMap.put(docId, docMap.get(docId));
					}*/
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
		  	if (token.length() >= 2) {
		  		start = token.substring(0, 2);
		  	} else {
		  		start = token.substring(0, 1);
		  	}
			if (_characterMap.containsKey(start)) {
				Map<String, List<Integer>> wordMap = _characterMap.get(start);
				
				if (wordMap.containsKey(token)) {
					List<Integer> docList = wordMap.get(token);
					docList.add(docId);
					docList.add(tokenIndex);
					/*if (docMap.containsKey(docId)) {
						docMap.get(docId).add(tokenIndex);
					} else {
						List<Integer> occurrences = new ArrayList<Integer>();
						occurrences.add(tokenIndex);
						docMap.put(docId, occurrences);
					}*/
				} else {
					List<Integer> docList = new ArrayList<Integer>();
					docList.add(docId);
					docList.add(tokenIndex);
					wordMap.put(token, docList);
				}
			} else {
				List<Integer> docList = new ArrayList<Integer>();
				docList.add(docId);
				docList.add(tokenIndex);
				Map<String, List<Integer>> wordMap = new HashMap<String, List<Integer>>();
				wordMap.put(token, docList);
				_characterMap.put(start, wordMap);
			}
			tokenIndex++;
		}
  }

@Override
  public void loadIndex() throws IOException, ClassNotFoundException {
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
