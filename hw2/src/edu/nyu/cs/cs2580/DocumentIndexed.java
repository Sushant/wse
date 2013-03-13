package edu.nyu.cs.cs2580;

import java.util.HashMap;
import java.util.Map;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 *          information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
	private static final long serialVersionUID = 9184892508124423115L;
	private Map<String, Integer> termFrequencyMap = new HashMap<String, Integer>();
	

	public Map<String, Integer> getTermFrequencyMap() {
		return termFrequencyMap;
	}

	public void setTermFrequencyMap(Map<String, Integer> termFrequency) {
		this.termFrequencyMap = termFrequency;
	}
	
	public DocumentIndexed(int docid) {
		super(docid);
	}
}
