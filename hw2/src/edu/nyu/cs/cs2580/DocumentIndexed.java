package edu.nyu.cs.cs2580;

import java.util.Vector;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 *          information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
	private static final long serialVersionUID = 9184892508124423115L;
	private Vector<String> _titleTokens = new Vector<String>();
	private Vector<String> _bodyTokens = new Vector<String>();

	public Vector<String> get_titleTokens() {
		return _titleTokens;
	}

	public void set_titleTokens(Vector<String> _titleTokens) {
		this._titleTokens = _titleTokens;
	}

	public Vector<String> get_bodyTokens() {
		return _bodyTokens;
	}

	public void set_bodyTokens(Vector<String> _bodyTokens) {
		this._bodyTokens = _bodyTokens;
	}

	public DocumentIndexed(int docid) {
		super(docid);
	}
}
