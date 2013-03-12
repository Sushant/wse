package edu.nyu.cs.cs2580;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 *          information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
	private static final long serialVersionUID = 9184892508124423115L;
	private Map<String, Integer> termFrequency = new HashMap<String, Integer>();

	public Map<String, Integer> getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(Map<String, Integer> termFrequency) {
		this.termFrequency = termFrequency;
	}
	
	public DocumentIndexed(int docid) {
		super(docid);
	}
}
