/**
 * 
 */
package edu.nyu.cs.cs2580;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * This class handles converting the AddressBook object to JSON and dumping it
 * to a file, and loading JSON from file and converting back to AddressBook
 * object using google-gson. This helps in abstracting how we are maintaining
 * persistence, and can be easily modified if we later decide to move say to a
 * database.
 * 
 */

public class PersistentStore {
	/**
	 * PersistentStore singleton
	 */
	private static final PersistentStore INSTANCE = new PersistentStore();
	private Gson _gson;

	private PersistentStore() {
		_gson = new GsonBuilder().create();
	}

	public static PersistentStore getInstance() {
		return INSTANCE;
	}

	public void save(String filepath,
			Map<String, Map<Integer, List<Integer>>> indexMap)
			throws IOException {
		Map<String, Map<String, List<String>>> storedMap = new HashMap<String, Map<String, List<String>>>();
		Map<String, List<String>> storeMap;
		for (Map.Entry<String, Map<Integer, List<Integer>>> entry : indexMap
				.entrySet()) {
			storeMap = new HashMap<String, List<String>>();
			List<String> tempList;
			Map<Integer, List<Integer>> tempMap = entry.getValue();
			for (Map.Entry<Integer, List<Integer>> entry1 : tempMap.entrySet()) {
				tempList = new ArrayList<String>();
				for (Integer i : entry1.getValue()) {
					tempList.add(i.toString());
				}
				storeMap.put(entry1.getKey().toString(), tempList);
			}
			storedMap.put(entry.getKey(), storeMap);
		}
		Writer writer = new OutputStreamWriter(new FileOutputStream(filepath));
		_gson.toJson(storedMap, writer);
		writer.close();
	}

	public Map<String, Map<Integer, List<Integer>>> load(String filepath)
			throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(filepath));

		Map<String, Map<String, List<String>>> indexMap = _gson.fromJson(
				reader,
				new TypeToken<Map<String, Map<String, List<String>>>>() {
				}.getType());

		reader.close();
		Map<String, Map<Integer, List<Integer>>> returnMap = new HashMap<String, Map<Integer, List<Integer>>>();
		for (Map.Entry<String, Map<String, List<String>>> entry : indexMap
				.entrySet()){
			Map<Integer,List<Integer>> tempReturnMap = new HashMap<Integer,List<Integer>>();
			List<Integer> tempList;
			for(Map.Entry<String, List<String>> entry1 : entry.getValue().entrySet()){
				tempList = new ArrayList<Integer>();
				for(String s : entry1.getValue()){
					tempList.add(Integer.parseInt(s));
				}
				try{
				tempReturnMap.put(Integer.parseInt(entry1.getKey()), tempList);
				}catch(Exception e){
					System.out.println(entry1.getKey());
					e.printStackTrace();
					System.exit(1);
				}
			}
			returnMap.put(entry.getKey(), tempReturnMap);
		}
		return returnMap;
	}

	/*
	 * public Map<Integer, List<Integer>> loadDocMap(String filepath, String
	 * word) throws IOException { Map<String, Map<Integer, List<Integer>>>
	 * loadedWordMap = load(filepath); if (loadedWordMap.containsKey(word)) {
	 * return loadedWordMap.get(word); } return new HashMap<Integer,
	 * List<Integer>>(); }
	 * 
	 * public void saveDocMap(String filepath, String word, Map<Integer,
	 * List<Integer>> docMap) throws IOException { Map<String, Map<Integer,
	 * List<Integer>>> loadedWordMap; try { loadedWordMap = load(filepath); }
	 * catch (IOException e) { loadedWordMap = new HashMap<String, Map<Integer,
	 * List<Integer>>>(); } loadedWordMap.put(word, docMap); save(filepath,
	 * loadedWordMap); }
	 */

	public void saveDoc(String filePath, List<Document> doc) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(filePath));
		_gson.toJson(doc, writer);
		writer.close();
	}

	public List<DocumentIndexed> loadDoc(String filePath) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(filePath));
		List<DocumentIndexed> docList = _gson.fromJson(reader,
				new TypeToken<List<DocumentIndexed>>() {
				}.getType());
		return docList;
	}

	public void saveIndexMetadata(String filepath, Map<String, Long> dataMap)
			throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(filepath));
		_gson.toJson(dataMap, writer);
		writer.close();
	}

	public Map<String, Long> loadIndexMetadata(String filepath)
			throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(filepath));

		Map<String, Long> dataMap = _gson.fromJson(reader,
				new TypeToken<Map<String, Long>>() {
				}.getType());

		reader.close();
		return dataMap;
	}

	public void saveBytes(String filepath,
			Map<String, List<List<Integer>>> byteMap) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(filepath));
		_gson.toJson(byteMap, writer);
		writer.close();
	}

	public Map<String, List<List<Integer>>> loadBytes(String filepath)
			throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(filepath));

		Map<String, List<List<Integer>>> byteMap = _gson.fromJson(reader,
				new TypeToken<Map<String, List<List<Integer>>>>() {
				}.getType());
		reader.close();
		return byteMap;
	}
}
