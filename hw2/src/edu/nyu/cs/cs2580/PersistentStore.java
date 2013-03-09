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

	private PersistentStore() {

	}
	
	public static PersistentStore getInstance() {
		return INSTANCE;
	}

	public void save(String filepath, Map<String, List<Integer>> indexMap) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(filepath));

		Gson gson = new GsonBuilder().create();
		gson.toJson(indexMap, writer);

		writer.close();
	}

	public Map<String, List<Integer>> load(String filepath) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(filepath));

		Gson gson = new GsonBuilder().create();
		Map<String, List<Integer>> indexMap = gson.fromJson(reader, new TypeToken<Map<String, List<Integer>>>() {}.getType());

		reader.close();
		return indexMap;
	}
}
