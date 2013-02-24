package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


// @CS2580: This is a simple implementation that you will be changing
// in homework 2.  For this homework, don't worry about how this is done.
class Document {
  public int _docid;

  private static HashMap < String , Integer > _dictionary = new HashMap < String , Integer >();
  private static Vector < String > _rdictionary = new Vector < String >();
  private static HashMap < Integer , Integer > _df = new HashMap < Integer , Integer >();
  private static HashMap < Integer , Integer > _tf = new HashMap < Integer , Integer >();
  private static int _total_tf = 0;
  
  private Vector < Integer > _body;
  private Vector < Integer > _title;
  private Vector < Pair<Integer, Integer> > _phrase;
  private String _titleString;
  private int _numviews;
  
  public static int documentFrequency(String s){
    return _dictionary.containsKey(s) ? _df.get(_dictionary.get(s)) : 0;
  }

  public static int termFrequency(String s){
    return _dictionary.containsKey(s) ? _tf.get(_dictionary.get(s)) : 0;
  }

  public static int termFrequency(){
    return _total_tf;
  }
  
  public Document(int did, String content){
    Scanner s = new Scanner(content).useDelimiter("\t");

    _titleString = s.next();
    _title = new Vector < Integer >();
    _body = new Vector < Integer >();

    readTermVector(_titleString, _title);
    readTermVector(s.next(), _body);
    HashSet < Integer > unique_terms = new HashSet < Integer >();
    for (int i = 0; i < _title.size(); ++i){
      int idx = _title.get(i);
      unique_terms.add(idx);
      int old_tf = _tf.get(idx);
      _tf.put(idx, old_tf + 1);
      _total_tf++;
    }
    for (int i = 0; i < _body.size(); ++i){
      int idx = _body.get(i);
      unique_terms.add(idx);
      int old_tf = _tf.get(idx);
      _tf.put(idx, old_tf + 1);
      _total_tf++;
    }
    for (Integer idx : unique_terms){
      if (_df.containsKey(idx)){
        int old_df = _df.get(idx);
        _df.put(idx,old_df + 1);
      }
    }
    _numviews = Integer.parseInt(s.next());
    _docid = did;
  }
  
  public String get_title_string(){
    return _titleString;
  }

  public int get_numviews(){
    return _numviews;
  }

  public Vector < String > get_title_vector(){
    return getTermVector(_title);
  }

  public Vector < String > get_body_vector(){
     return getTermVector(_body);  
  }

  
  public int getTermFrequency(String term) {
	  return (getTermFrequencyInVector(term, getTermVector(_title)) + getTermFrequencyInVector(term, getTermVector(_body))); 
  }
  
  
  private int getTermFrequencyInVector(String queryString, Vector < String > tv){
	  int frequency = 0;
	  for (String docString: tv) {
		  if (queryString.equals(docString)) {
			  frequency += 1;
		  }
	  }
	  return frequency;
  }
  
  public int getPhraseFrequency(String phrase) {
	  return (getPhraseFrequencyInVector(phrase, getTermVector(_title)) + getPhraseFrequencyInVector(phrase, getTermVector(_body))); 
  }
  
  
  private int getPhraseFrequencyInVector(String queryPhrase, Vector < String > tv){
	  int frequency = 0;
	  for (int i = 0; i < tv.size() - 1; i++) {
		  StringBuffer docPhrase = new StringBuffer(tv.get(i)).append(" ").append(tv.get(i + 1));
		  if (queryPhrase.equals(docPhrase.toString())) {
			  frequency += 1;
		  }
	  }
	  return frequency;
  }
  
  public Vector < String > get_phrase_vector(){
	  _phrase = new Vector < Pair < Integer, Integer> >();
	  readPhraseVector(_title, _phrase);
	  return getPhraseVector(_phrase);
  }
  
  private Vector < String > getTermVector(Vector < Integer > tv){
    Vector < String > retval = new Vector < String >();
    for (int idx : tv){
      retval.add(_rdictionary.get(idx));
    }
    return retval;
  }
  
  private Vector < String > getPhraseVector(Vector< Pair<Integer, Integer> > pv){
	  Vector < String > retval = new Vector < String >();
	    for (Pair <Integer, Integer> p_idx: pv){
	      StringBuffer phrase = new StringBuffer(_rdictionary.get(p_idx.getL())).append(" ").append(_rdictionary.get(p_idx.getR()));
	      retval.add(phrase.toString());
	    }
	    return retval;
  }
   

  private void readTermVector(String raw,Vector < Integer > tv){
    Scanner s = new Scanner(raw);
    while (s.hasNext()){
      String term = s.next();
      int idx = -1;
      if (_dictionary.containsKey(term)){
        idx = _dictionary.get(term);
      } else {
        idx = _rdictionary.size();
        _rdictionary.add(term);
        _dictionary.put(term, idx);
        _tf.put(idx,0);
        _df.put(idx,0);
      }
      tv.add(idx);
    }
    return;
  }
  
  private void readPhraseVector(Vector < Integer > tv, Vector < Pair < Integer, Integer> > pv){
	  if (tv.size() >= 2) {
		  for (int i = 0; i < tv.size() - 1; i++) {
			  Pair < Integer, Integer > p =  new Pair<Integer, Integer>(tv.get(i), tv.get(i + 1));
			  pv.add(p);
		  }  
	  }
	  return;
  }
 
}
