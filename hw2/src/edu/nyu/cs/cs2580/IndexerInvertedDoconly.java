package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
	private Vector<Document> _documents = new Vector<Document>();

	public IndexerInvertedDoconly(Options options) {
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}

	@Override
	public void constructIndex() throws IOException {
		List<String> files = Utility
				.getFilesInDirectory(_options._corpusPrefix);
		int counter = 0;
		Map<Character, Map<String, List<Integer>>> characterMap = new HashMap<Character, Map<String, List<Integer>>>();
		for (String file : files) {
			counter++;
			String url = _options._corpusPrefix + "/" + file;
			String document = Utility.extractText(url);
			List<String> tokens = Utility.tokenize(document);
			int docId = _documents.size();
			DocumentIndexed doc = new DocumentIndexed(docId);
			for (String token : tokens) {
				String stemmedToken = Stemmer.getStemmedWord(token);
				char start = stemmedToken.charAt(0);
				if (characterMap.containsKey(start)) {
					Map<String, List<Integer>> tempMap = characterMap
							.get(start);
					if (tempMap.containsKey(stemmedToken)) {
						List<Integer> tempList = tempMap.get(stemmedToken);
						if (!tempList.contains(docId)) {
							tempList.add(docId);
						}
						tempMap.put(stemmedToken, tempList);
					} else {
						List<Integer> tempList = new ArrayList<Integer>();
						tempList.add(docId);
						tempMap.put(stemmedToken, tempList);
					}
				} else {
					Map<String, List<Integer>> tempMap = new HashMap<String, List<Integer>>();
					List<Integer> tempList = new ArrayList<Integer>();
					tempList.add(docId);
					tempMap.put(stemmedToken, tempList);
					characterMap.put(start, tempMap);
				}
			}
			if (counter % 1000 == 0) {
				writeFile(characterMap);
				characterMap.clear();
			}
			_documents.add(doc);
		}
		if (!characterMap.isEmpty()) {
			writeFile(characterMap);
			characterMap.clear();
		}
		mergeAll();
	}

	private void mergeAll() throws IOException {
		List<String> files = Utility.getFilesInDirectory(_options._indexPrefix);
		for (String file : files) {
			System.out.println("Merging... " + file);
			Map<Character, Map<String, List<Integer>>> characterMap = readAll(file);
			String fileName = _options._indexPrefix + "/" + file;
			File charFile = new File(fileName);
			charFile.delete();
			writeFile(characterMap);
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
			tempMap.put(word, tempList);
		}
		CharacterMap.put(fileName.charAt(0), tempMap);
		return CharacterMap;
	}

	private void writeFile(
			Map<Character, Map<String, List<Integer>>> characterMap)
			throws IOException {
		for (Map.Entry<Character, Map<String, List<Integer>>> entry : characterMap
				.entrySet()) {
			String path = _options._indexPrefix + "/" + entry.getKey();
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
	}

	@Override
	public Document getDoc(int docid) {
		SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
		return null;
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
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

	public static void main(String[] args) throws IOException {
		Options option = new Options("conf/engine.conf");
		IndexerInvertedDoconly in = new IndexerInvertedDoconly(option);
		Date d = new Date();
		in.constructIndex();
		Date d1 = new Date();
		System.out.println(d);
		System.out.println(d1);
	}
}
