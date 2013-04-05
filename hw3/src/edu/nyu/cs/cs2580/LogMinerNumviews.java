package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class LogMinerNumviews extends LogMiner {
	private PersistentStore _persist = PersistentStore.getInstance();

  public LogMinerNumviews(Options options) {
    super(options);
  }

  /**
   * This function processes the logs within the log directory as specified by
   * the {@link _options}. The logs are obtained from Wikipedia dumps and have
   * the following format per line: [language]<space>[article]<space>[#views].
   * Those view information are to be extracted for documents in our corpus and
   * stored somewhere to be used during indexing.
   *
   * Note that the log contains view information for all articles in Wikipedia
   * and it is necessary to locate the information about articles within our
   * corpus.
   *
   * @throws IOException
   */
  @Override
  public void compute() throws IOException {
    System.out.println("Computing using " + this.getClass().getName());
    List<String> logFiles = Utility.getFileInDirectory(_options._logPrefix, ".log");
    Map<String, Integer>_fileNameTodocumentIdMap;
    try {
    	_fileNameTodocumentIdMap =  _persist.loadFileMapPageRankPrepare("data/FileMap.dat");
    } catch (IOException ie) {
    	Utility.saveFileNameToDocIdMap(_options._corpusPrefix);
    	_fileNameTodocumentIdMap =  _persist.loadFileMapPageRankPrepare("data/FileMap.dat");
    }
    _computeNumViews(logFiles, _fileNameTodocumentIdMap);
    //System.out.println(logFiles);
    //System.out.println(_fileNameTodocumentIdMap);
    return;
  }

  private void _computeNumViews(List<String> logFiles, Map<String, Integer> _fileNameTodocumentIdMap) {
	  BufferedReader reader;
	  Map<Integer, Integer> docNumViewsMap = new HashMap<Integer, Integer>();
	  for (String logFile : logFiles) {
		try {
			reader = new BufferedReader(new FileReader(_options._logPrefix + "/" + logFile));
			String line = null;
			int numViews = 0;
			while ((line = reader.readLine()) != null) {
				String[] tokens =  line.split(" ");
				if (tokens.length == 3) {
					String docName = tokens[1];
					try {
						numViews = Integer.parseInt(tokens[2]);
					} catch (NumberFormatException nfe) {
						continue;
					}
					try {
						docName = URLDecoder.decode(docName, "UTF-8");
					} catch (IllegalArgumentException iae) {
						
					}
					if (_fileNameTodocumentIdMap.containsKey(docName)) {
						Integer docId = _fileNameTodocumentIdMap.get(docName);
						if (docNumViewsMap.containsKey(docId)) {
							numViews += docNumViewsMap.get(docId);
						}
						docNumViewsMap.put(_fileNameTodocumentIdMap.get(docName), numViews);
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			continue;
		} catch (IOException e) {
			continue;
		}
	  }
	  try {
		  _persist.saveDocNumViewsMap("data/NumViewsMap.dat", docNumViewsMap);
	  } catch (IOException e) {
		  System.out.println("Failed to save num views map");
		  e.printStackTrace();
	  }
}

/**
   * During indexing mode, this function loads the NumViews values computed
   * during mining mode to be used by the indexer.
   * 
   * @throws IOException
   */
  @Override
  public Object load() throws IOException {
    return _persist.loadDocNumViewsMap("data/NumViewsMap.dat");
  }
  
  public static void main(String[] args) throws IOException {
	  Options option = new Options("conf/engine.conf");
	  LogMiner logMiner = new LogMinerNumviews(option);
	  logMiner.compute();
  }

}
