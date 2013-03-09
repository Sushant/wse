package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.net.MalformedURLException;
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
	final int BULK_WRITE_SIZE = 350;
	private Vector<Document> _documents = new Vector<Document>();
	private Map<Character, Map<String, Map<Integer, Integer>>> _characterMap;
	
  public IndexerInvertedOccurrence(Options options) {
    super(options);
    _characterMap = new HashMap<Character, Map<String, Map<Integer, Integer>>>();
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
			  _characterMap = new HashMap<Character, Map<String, Map<Integer, Integer>>>();
		  }
	  }
	  if (_characterMap != null) {
		  updateIndexWithMap(_characterMap);
	  }
  }

  private void processDocument(String filename) {
	  String corpusFile = _options._corpusPrefix + "/" + filename;
	  
	  int docId = _documents.size();
	  String document;
	  try {
		  document = Utility.extractText(corpusFile);
		  List<String> tokens = Utility.tokenize(document);
		  buildMapFromTokens(docId, tokens);
		  _documents.add(new DocumentIndexed(docId));
	  } catch (MalformedURLException e) {
		  System.out.println(filename);
		  e.printStackTrace();
	  } catch (IOException e) {
		  System.out.println(filename);
		  e.printStackTrace();
	  }
  }
  
  private void updateIndexWithMap(
		Map<Character, Map<String, Map<Integer, Integer>>> characterMap) {
	for (Character c : characterMap.keySet()) {
		String indexFile = _options._indexPrefix + "/" + c + ".idx";
		Map<String, Map<Integer, Integer>> wordMap = characterMap.get(c);
		try {
			Map<String, Map<Integer, Integer>> loadedWordMap = _persistentStore.load(indexFile);
			for (String word: wordMap.keySet()) {
				Map<Integer, Integer> docMap = wordMap.get(word);
				if (loadedWordMap.containsKey(word)) {
					Map<Integer, Integer> loadedDocMap = loadedWordMap.get(word);
					for (Integer docId: docMap.keySet()) {
						loadedDocMap.put(docId, docMap.get(docId));
					}
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
	  for(String token : tokens){
			String stemmedToken = Stemmer.getStemmedWord(token);
			char start = stemmedToken.charAt(0);
			if (_characterMap.containsKey(start)) {
				Map<String, Map<Integer, Integer>> wordMap = _characterMap.get(start);
				
				if (wordMap.containsKey(stemmedToken)) {
					Map <Integer, Integer> docMap = wordMap.get(stemmedToken);
					if (docMap.containsKey(docId)) {
						Integer occurrences = docMap.get(docId);
						docMap.put(docId, occurrences + 1);
					} else {
						docMap.put(docId, 1);
					}
				} else {
					Map <Integer, Integer> docMap = new HashMap<Integer, Integer>();
					docMap.put(docId, 1);
					wordMap.put(stemmedToken, docMap);
				}
			} else {
				Map <Integer, Integer> docMap = new HashMap<Integer, Integer>();
				docMap.put(docId, 1);
				Map<String, Map<Integer, Integer>> wordMap = new HashMap<String, Map<Integer, Integer>>();
				wordMap.put(stemmedToken, docMap);
				_characterMap.put(start, wordMap);
			}
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
