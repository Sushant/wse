package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedDoconly extends Indexer {
	// Term document frequency, key is the integer representation of the term
	// and
	// value is the number of documents the term appears in.
	private Map<String, Integer> _termDocFrequency = new HashMap<String, Integer>();
	// Term frequency, key is the integer representation of the term and value
	// is
	// the number of times the term appears in the corpus.
	private Map<String, Integer> _termCorpusFrequency = new HashMap<String, Integer>();
	
	final int BULK_DOC_PROCESSING_SIZE = 1000;
	final int BULK_DOC_WRITE_SIZE = 300;
	final String METADATA_FILE = "index.dat";
	private Vector<Document> _documents = new Vector<Document>();
	private Map<Character, Map<String, List<Integer>>> _characterMap;

	public IndexerInvertedDoconly(Options options) {
		super(options);
		 _characterMap = new HashMap<Character, Map<String, List<Integer>>>();
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
				writeFile(_characterMap);
				_characterMap.clear();
			}
		}
		if (!_characterMap.isEmpty()) {
			writeFile(_characterMap);
			_characterMap.clear();
		}
		_persistentStore.saveDoc(_options._indexPrefix + "/" + String.valueOf(_numDocs) + ".dat", _documents);
		mergeAll();
		_documents.clear();
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
		int docId = _numDocs;
		String document = Utility.extractText(corpusFile);
		List<String> stemmedTokens = Utility.tokenize(document);
		buildMapFromTokens(docId, stemmedTokens);
		_numDocs++;
	}

	private void buildMapFromTokens(int docId, List<String> stemmedTokens) {
		System.out.println("DocId : " + docId);
		DocumentIndexed doc = new DocumentIndexed(docId);
		Map<String, Integer> termFrequency = new HashMap<String, Integer>();
		for (String stemmedToken : stemmedTokens) {
			if (termFrequency.containsKey(stemmedToken)) {
				int value = termFrequency.get(stemmedToken);
				value++;
				termFrequency.put(stemmedToken, value);
			} else {
				termFrequency.put(stemmedToken, 1);
			}
			char start = stemmedToken.charAt(0);
			if (_characterMap.containsKey(start)) {
				Map<String, List<Integer>> wordMap = _characterMap.get(start);
				if (wordMap.containsKey(stemmedToken)) {
					List<Integer> docList = wordMap.get(stemmedToken);
					if (!docList.contains(docId)) {
						docList.add(docId);
					}
					wordMap.put(stemmedToken, docList);
				} else {
					List<Integer> tempList = new ArrayList<Integer>();
					tempList.add(docId);
					wordMap.put(stemmedToken, tempList);
				}
			} else {
				Map<String, List<Integer>> tempMap = new HashMap<String, List<Integer>>();
				List<Integer> tempList = new ArrayList<Integer>();
				tempList.add(docId);
				tempMap.put(stemmedToken, tempList);
				_characterMap.put(start, tempMap);
			}
		}
		_totalTermFrequency = _totalTermFrequency + stemmedTokens.size();
		doc.setTermFrequency(termFrequency);
		_documents.add(doc);
	}

	private void mergeAll() throws IOException {
		List<String> files = Utility.getFilesInDirectory(_options._indexPrefix);
		System.out.println("Files: " + files);
		for (String file : files) {
			if (file.endsWith(".idx")) {
				System.out.println("Merging... " + file);
				Map<Character, Map<String, List<Integer>>> characterMap = readAll(file);
				String fileName = _options._indexPrefix + "/" + file;
				File charFile = new File(fileName);
				charFile.delete();
				writeFile(characterMap);
			}
		}
	}

	private Map<Character, Map<String, List<Integer>>> readAll(String fileName)
			throws FileNotFoundException {
		String file = _options._indexPrefix + "/" + fileName;
		Scanner scan = new Scanner(new File(file));
		Map<Character, Map<String, List<Integer>>> CharacterMap = new HashMap<Character, Map<String, List<Integer>>>();
		Map<String, List<Integer>> tempMap = new HashMap<String, List<Integer>>();
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			String lineArray[] = line.split(" ");
			String word = lineArray[0];
			List<Integer> tempList = new ArrayList<Integer>();
			for (int i = 1; i < lineArray.length; i++) {
				tempList.add(Integer.parseInt(lineArray[i]));
			}
			if (tempMap.containsKey(word)) {
				List<Integer> temp = tempMap.get(word);
				temp.addAll(tempList);
				tempMap.put(word, temp);
			} else {
				tempMap.put(word, tempList);
			}
		}
		CharacterMap.put(fileName.charAt(0), tempMap);
		return CharacterMap;
	}

	private void writeFile(
			Map<Character, Map<String, List<Integer>>> characterMap)
			throws IOException {
		for (Map.Entry<Character, Map<String, List<Integer>>> entry : characterMap
				.entrySet()) {
			String path = _options._indexPrefix + "/" + entry.getKey() + ".idx";
			File file = new File(path);
			OutputStream out = new FileOutputStream(file, true);
			Map<String, List<Integer>> tempMap = entry.getValue();
			for (Map.Entry<String, List<Integer>> entry1 : tempMap.entrySet()) {
				List<Integer> tempList = entry1.getValue();
				String docs;
				StringBuilder sb = new StringBuilder();
				for (Integer docId : tempList) {
					sb.append(docId).append(" ");
				}
				docs = sb.toString();
				String word = entry1.getKey() + " " + docs;
				out.write(word.getBytes());
				out.write("\n".getBytes());
			}
			out.close();
		}
	}

	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
		loadIndexMetadata();
	}

	private void loadIndexMetadata() throws IOException {
		 String metaDataFile = _options._indexPrefix + "/" + METADATA_FILE;
		 Map<String, Long> dataMap = _persistentStore.loadIndexMetadata(metaDataFile);
		 _totalTermFrequency = dataMap.get("totalTermFrequency");
		 _numDocs = dataMap.get("numDocs").intValue();
	  }
	
	@Override
	public Document getDoc(int docid) {
		try {
			int quotient = docid / 300;
			int remainder = docid % 300;
			int docFile = (quotient + 1) * 300;
			String fileName = _options._indexPrefix + "/" + docFile+".dat";
			List<Document> docs = _persistentStore.loadDoc(fileName);
			return docs.get(remainder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 * 
	 * @throws Exception
	 */
	@Override
	public Document nextDoc(Query query, int docid) {
		try {
			query.processQuery();
			List<String> queryVector = query._tokens;
			Map<String, List<Integer>> queryMap = new HashMap<String, List<Integer>>();
			List<List<Integer>> list = new ArrayList<List<Integer>>();
			for (String search : queryVector) {
				String fileName = _options._indexPrefix + "/"
						+ search.charAt(0)+".idx";
				System.out.println(fileName);
				String cmd = "grep '\\<" + search + "\\>' " + fileName;
				List<String> commands = new ArrayList<String>();
				commands.add("/bin/bash");
				commands.add("-c");
				commands.add(cmd);
				ProcessBuilder pb = new ProcessBuilder(commands);
				Process p;
				p = pb.start();
				InputStreamReader isr = new InputStreamReader(
						p.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				String s[];
				String line = br.readLine();
				s = line.split(" ");
				System.out.println("S size --> " + s.length);
				List<Integer> tempList = new ArrayList<Integer>();
				for (int i = 1; i < s.length; i++) {
					tempList.add(Integer.parseInt(s[i]));
				}
				list.add(tempList);
			}
			System.out.println("List Size --> " + list.size());
			if (list.size() == 1) {
				int index = list.get(0).indexOf((docid));
				if (index + 1 <= list.get(0).size() - 1) {
					return getDoc(list.get(0).get(index + 1));
				} else {
					return null;
				}
			}
			int min = Integer.MAX_VALUE;
			int index = Integer.MAX_VALUE;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).size() < min) {
					min = list.get(i).size();
					index = i;
				}
			}
			List<Integer> tempInteger = list.get(index);
			System.out.println("List before-->" + list.size());
			list.remove(index);
			System.out.println("List after-->" + list.size());
			System.out.println(list);
			System.out.println(tempInteger);
			System.out.println("Doc Id" + docid);
			int index1 = tempInteger.indexOf(docid);
			System.out.println("Index1 --> " + index1);
			for (int i = index1 + 1; i < tempInteger.size(); i++) {
				boolean flag = false;
				for (List<Integer> tempList1 : list) {
					flag = tempList1.contains(tempInteger.get(i));
					if (!flag) {
						break;
					}
				}
				if (flag) {
					return getDoc(tempInteger.get(i));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public static void main(String[] args) throws IOException {
		Options option = new Options("conf/engine.conf");
		IndexerInvertedDoconly in = new IndexerInvertedDoconly(option);
		Date d = new Date();
		//in.constructIndex();
		Query query = new Query("arthur zero");
		Document doc = in.nextDoc(query, 2);
		System.out.println(doc._docid);
		Date d1 = new Date();
		System.out.println(d);
		System.out.println(d1);



		

	}
}
