package edu.nyu.cs.cs2580;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 *          information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
	private static final long serialVersionUID = 9184892508124423115L;
	private String fileNameOnDisk;
	private int numberOfIncomingLinks = 0;
	private int numberOfOutgoingLinks = 0;
	private Set<Integer> listOfIncomingLinks = new HashSet<Integer>();
	private Map<String, Integer> termFrequencyMap = new HashMap<String, Integer>();

	public DocumentIndexed(int docid) {
		super(docid);
	}

	public Map<String, Integer> getTermFrequencyMap() {
		return termFrequencyMap;
	}

	public void setTermFrequencyMap(Map<String, Integer> termFrequency) {
		this.termFrequencyMap = termFrequency;
	}

	public String getFileNameOnDisk() {
		return fileNameOnDisk;
	}

	public void setFileNameOnDisk(String fileNameOnDisk) {
		this.fileNameOnDisk = fileNameOnDisk;
	}

	public int getNumberOfIncomingLinks() {
		return numberOfIncomingLinks;
	}

	public void setNumberOfIncomingLinks(int numberOfIncomingLinks) {
		this.numberOfIncomingLinks = numberOfIncomingLinks;
	}

	public int getNumberOfOutgoingLinks() {
		return numberOfOutgoingLinks;
	}

	public void setNumberOfOutgoingLinks(int numberOfOutgoingLinks) {
		this.numberOfOutgoingLinks = numberOfOutgoingLinks;
	}

	public Set<Integer> getListOfOutgoingLinks() {
		return listOfIncomingLinks;
	}

	public void setListOfIncomingLinks(Set<Integer> listOfIncomingLinks) {
		this.listOfIncomingLinks = listOfIncomingLinks;
		this.numberOfIncomingLinks = this.listOfIncomingLinks.size();
	}

	public void addElementsToListOfIncomingLinks(Integer linkId) {
		this.listOfIncomingLinks.add(linkId);
		this.numberOfIncomingLinks = this.listOfIncomingLinks.size();
	}

}
