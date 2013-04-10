package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
	private PersistentStore _persist = PersistentStore.getInstance();
	private Map<String, Integer> _fileNameTodocumentIdMap = new HashMap<String, Integer>();
	private Map<Integer, DocumentIndexed> _corpusGraph = new HashMap<Integer, DocumentIndexed>();
	private float _corpusSize = 0.0f;
	private float lamda = 0.1f;
	private int iterations = 1;
	private final String CORPUS_GRAPH_FILE = "data/index/corpusGraph.dat";
	private final String NAME_TO_DOCID_FILE = "data/index/nameDocIdMap.dat";
	private final String PAGE_RANK_FILE = "data/index/pageRankMap.dat";

	public CorpusAnalyzerPagerank(Options options) {
		super(options);
	}

	private void _loadFileToDocIdMap() throws IOException {
		  try {
		    	_fileNameTodocumentIdMap =  _persist.loadFileMapPageRankPrepare(NAME_TO_DOCID_FILE);
		  } catch (IOException ie) {
			  Utility.saveFileNameToDocIdMap(_options._corpusPrefix, NAME_TO_DOCID_FILE);
			  _fileNameTodocumentIdMap =  _persist.loadFileMapPageRankPrepare(NAME_TO_DOCID_FILE);
		  }
	 }
	
	/**
	 * This function processes the corpus as specified inside {@link _options}
	 * and extracts the "internal" graph structure from the pages inside the
	 * corpus. Internal means we only store links between two pages that are
	 * both inside the corpus.
	 * 
	 * Note that you will not be implementing a real crawler. Instead, the
	 * corpus you are processing can be simply read from the disk. All you need
	 * to do is reading the files one by one, parsing them, extracting the links
	 * for them, and computing the graph composed of all and only links that
	 * connect two pages that are both in the corpus.
	 * 
	 * Note that you will need to design the data structure for storing the
	 * resulting graph, which will be used by the {@link compute} function.
	 * Since the graph may be large, it may be necessary to store partial graphs
	 * to disk before producing the final graph.
	 * 
	 * @throws IOException
	 */
	@Override
	public void prepare() throws IOException {
		System.out.println("Preparing " + this.getClass().getName());
		_loadFileToDocIdMap();
		List<String> fileNames = Utility.getFilesInDirectory(_options._corpusPrefix);
		int counter = 0;
		DocumentIndexed doc;
		_corpusSize = fileNames.size();
		for (String file : fileNames) {
			doc = new DocumentIndexed(counter);
			doc.setFileNameOnDisk(file);
			doc.setPageRank((float) (1.0 / _corpusSize));
			_corpusGraph.put(counter, doc);
			counter++;
		}
		for (String file : fileNames) {
			HeuristicLinkExtractor extract = new HeuristicLinkExtractor(new File(_options._corpusPrefix + "/" + file));
			String nextLink;
			int docIdOfThisFile;
			try {
				docIdOfThisFile = _fileNameTodocumentIdMap.get(file);
			} catch (Exception e) {
				System.out.println(file + " failed.");
				continue;
			}
			int numberOfOutgoingLinks = 0;
			Set<Integer> listOfOutgoingLinks = new HashSet<Integer>();
			while ((nextLink = extract.getNextInCorpusLinkTarget()) != null) {
				if (_fileNameTodocumentIdMap.containsKey(nextLink)) {
					numberOfOutgoingLinks++;
					Integer docId = _fileNameTodocumentIdMap.get(nextLink);
					listOfOutgoingLinks.add(docId);
				}
			}
			DocumentIndexed tempDoc = _corpusGraph.get(docIdOfThisFile);
			tempDoc.setNumberOfOutgoingLinks(numberOfOutgoingLinks);
			tempDoc.setListOfOutgoingLinks(listOfOutgoingLinks);
		}
		_persist.saveDocIdMapForPageRankPrepare(CORPUS_GRAPH_FILE, _corpusGraph);
		_fileNameTodocumentIdMap.clear();
		_corpusGraph.clear();
		System.out.println("Prepare finished.");
		return;
	}

	/**
	 * This function computes the PageRank based on the internal graph generated
	 * by the {@link prepare} function, and stores the PageRank to be used for
	 * ranking.
	 * 
	 * Note that you will have to store the computed PageRank with each document
	 * the same way you do the indexing for HW2. I.e., the PageRank information
	 * becomes part of the index and can be used for ranking in serve mode.
	 * Thus, you should store the whatever is needed inside the same directory
	 * as specified by _indexPrefix inside {@link _options}.
	 * 
	 * @throws IOException
	 */
	@Override
	public void compute() throws IOException {
		System.out.println("Computing using " + this.getClass().getName());
		_fileNameTodocumentIdMap = _persist.loadFileMapPageRankPrepare(NAME_TO_DOCID_FILE);
		_corpusGraph = _persist.loaddocIdMapPageRankPrepare(CORPUS_GRAPH_FILE);
		_corpusSize = _fileNameTodocumentIdMap.size();
		double[] _I = new double[(int) _corpusSize];
		double[] _R = new double[(int) _corpusSize];
		for (int i = 0; i < _I.length; i++) {
			_I[i] = (float) 1.0 / _corpusSize;
		}
		
		for (int j = 0; j < iterations; j++) {
			for (int i = 0; i < _I.length; i++) {
				_R[i] = lamda / _corpusSize;
			}
			for (Entry<String, Integer> entry : _fileNameTodocumentIdMap.entrySet()) {

				DocumentIndexed document = _corpusGraph.get(entry
						.getValue());
				Set<Integer> listOfDocuments = document.getListOfOutgoingLinks();
				float rank = document.getPageRank();
				if (listOfDocuments.size() > 0) {
					for (Integer doc : listOfDocuments) {
						DocumentIndexed tempDoc = _corpusGraph
								.get(doc);
						int _id = tempDoc._docid;
						_R[_id] = _R[_id]
								+ ((1.0f - lamda) * rank / listOfDocuments.size());
					}
				} else {
					for (Entry<String, Integer> entry1 : _fileNameTodocumentIdMap.entrySet()) {
						DocumentIndexed tempDoc = _corpusGraph.get(_fileNameTodocumentIdMap.get(entry1.getKey()));
						int _id = tempDoc._docid;
						_R[_id] = _R[_id] + ((1.0f - lamda) * rank / _corpusSize);
					}
				}
				for (int i = 0; i < _I.length; i++) {
					_I[i] = _R[i];
					Document tempDoc = _corpusGraph.get(i);
					tempDoc.setPageRank((float)_R[i]);
				}

			}
		}
		
		System.out.println("Corpus Size... " + _corpusGraph.size());
		_savePageRankMap();
		_corpusGraph.clear();
		_fileNameTodocumentIdMap.clear();
		return;
	}
	
	private void _savePageRankMap() throws IOException {
		Map<Integer, Float> _pageRankMap = new HashMap<Integer, Float>();
		for (DocumentIndexed doc: _corpusGraph.values()) {
			_pageRankMap.put(doc._docid, doc.getPageRank());
		}
		_persist.savePageRankMap(PAGE_RANK_FILE, _pageRankMap);
	}

	/**
	 * During indexing mode, this function loads the PageRank values computed
	 * during mining mode to be used by the indexer.
	 * 
	 * @throws IOException
	 */
	@Override
	public Object load() throws IOException {
		return _persist.loadPageRankMap(PAGE_RANK_FILE);
	}

	public static void main(String[] args) throws IOException {
		Options option = new Options("conf/engine.conf");
		CorpusAnalyzerPagerank c = new CorpusAnalyzerPagerank(option);
		c.prepare();
		c.compute();
	}
}
