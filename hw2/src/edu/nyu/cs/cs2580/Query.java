package edu.nyu.cs.cs2580;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Representation of a user query.
 * 
 * In HW1: instructors provide this simple implementation.
 * 
 * In HW2: students must implement {@link QueryPhrase} to handle phrases.
 * 
 * @author congyu
 * @auhtor fdiaz
 */
public class Query {
  public String _query = null;
  public Vector<String> _tokens = new Vector<String>();

  public Query(String query) {
    try {
    	 query.toLowerCase();
		_query = URLDecoder.decode(query, "UTF-8");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		System.out.println("URL DEcoder Error");
		e.printStackTrace();
	}
  }

  public void processQuery() {
    if (_query == null) {
      return;
    }
    Scanner s = new Scanner(_query);
    while (s.hasNext()) {
      _tokens.add(s.next());
    }
    s.close();
  }
}
