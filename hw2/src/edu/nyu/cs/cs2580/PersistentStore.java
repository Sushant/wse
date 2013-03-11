/**
 * 
 */
package edu.nyu.cs.cs2580;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * This class handles converting the AddressBook object to JSON and dumping it to a file,
 * and loading JSON from file and converting back to AddressBook object using google-gson.
 * This helps in abstracting how we are maintaining persistence, and can be easily modified
 * if we later decide to move say to a database. 
 * 
 */

public class PersistentStore {
	/**
	 *  PersistentStore singleton
	 */
	private static final PersistentStore INSTANCE = new PersistentStore();
	private Gson _gson;
	
	private PersistentStore() {
		_gson = new GsonBuilder().create();
	}
	
	public static PersistentStore getInstance() {
		return INSTANCE;
	}

	public void save(String filepath, Map<String, Map<Integer, List<Integer>>> indexMap) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(filepath));
		_gson.toJson(indexMap, writer);
		writer.close();
	}

	public Map<String, Map<Integer, List<Integer>>> load(String filepath) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(filepath));

		Map<String, Map<Integer, List<Integer>>> indexMap = _gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, List<Integer>>>>() {}.getType());

		reader.close();
		return indexMap;
	}
	
	/*public Map<Integer, List<Integer>> loadDocMap(String filepath, String word) throws IOException {
		Map<String, Map<Integer, List<Integer>>> loadedWordMap = load(filepath);
		if (loadedWordMap.containsKey(word)) {
			return loadedWordMap.get(word);
		}
		return new HashMap<Integer, List<Integer>>();
	}
	
	public void saveDocMap(String filepath, String word, Map<Integer, List<Integer>> docMap) throws IOException {
		Map<String, Map<Integer, List<Integer>>> loadedWordMap;
		try {
			loadedWordMap = load(filepath);
		} catch (IOException e) {
			loadedWordMap = new HashMap<String, Map<Integer, List<Integer>>>();
		}
			loadedWordMap.put(word, docMap);
			save(filepath, loadedWordMap);
	}*/
	
	public void saveDoc(String filePath, List<Document> doc) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(filePath));
		_gson.toJson(doc, writer);
		writer.close();
	}
	
	public List<Document> loadDoc(String filePath) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(filePath));
		List<Document> docList = _gson.fromJson(reader, new TypeToken<List<Document>>() {}.getType());
		return docList;
	}
	
	public void saveIndexMetadata(String filepath, Map<String, Long> dataMap) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(filepath));
		_gson.toJson(dataMap, writer);
		writer.close();
	}
	
	public Map<String, Long> loadIndexMetadata(String filepath) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(filepath));

		Map<String, Long> dataMap = _gson.fromJson(reader, new TypeToken<Map<String, Long>>() {}.getType());

		reader.close();
		return dataMap;
	}
}
