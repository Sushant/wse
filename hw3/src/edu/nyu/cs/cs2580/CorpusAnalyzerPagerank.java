package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
	private Map<String, Integer> _fileNameTodocumentIdMap = new HashMap<String, Integer>();
	private Map<Integer, DocumentIndexed> _documentIdToDocumentMap = new HashMap<Integer, DocumentIndexed>();

	public CorpusAnalyzerPagerank(Options options) {
		super(options);
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
		List<String> fileNames = Utility
				.getFilesInDirectory(_options._corpusPrefix);
		int counter = 0;
		DocumentIndexed doc;
		for (String file : fileNames) {
			doc = new DocumentIndexed(counter);
			doc.setFileNameOnDisk(file);
			_fileNameTodocumentIdMap.put(file, counter);
			_documentIdToDocumentMap.put(counter, doc);
		}
		System.out.println("DOne ...");
		for (String file : fileNames) {
			System.out.println("Preparing " + file);
			HeuristicLinkExtractor extract = new HeuristicLinkExtractor(
					new File(_options._corpusPrefix + "/" + file));
			String nextLink;
			int docIdOfThisFile = _fileNameTodocumentIdMap.get(file);
			while ((nextLink = extract.getNextInCorpusLinkTarget()) != null) {
				if (_fileNameTodocumentIdMap.containsKey(nextLink)) {
					Integer docId = _fileNameTodocumentIdMap.get(nextLink);
					DocumentIndexed tempDoc = _documentIdToDocumentMap
							.get(docId);
					tempDoc.addElementsToListOfIncomingLinks(docIdOfThisFile);
				}
			}
		}
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
		return;
	}

	/**
	 * During indexing mode, this function loads the PageRank values computed
	 * during mining mode to be used by the indexer.
	 * 
	 * @throws IOException
	 */
	@Override
	public Object load() throws IOException {
		System.out.println("Loading using " + this.getClass().getName());
		return null;
	}

	public static void main(String[] args) throws IOException {
		Options option = new Options("conf/engine.conf");
		CorpusAnalyzerPagerank c = new CorpusAnalyzerPagerank(option);
		c.prepare();
	}
}
