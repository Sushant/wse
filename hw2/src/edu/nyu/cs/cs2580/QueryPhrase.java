package edu.nyu.cs.cs2580;

import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {
  public Vector< Vector<String>> _phraseTokens = new Vector<Vector<String>>();

  public QueryPhrase(String query) {
    super(query);
  }

  @Override
  public void processQuery() {
	if (_query == null) {
	   return;
	}
	Scanner s1 = new Scanner(_query);
	while (s1.hasNext()) {
	   _tokens.add(s1.next());
	}
	s1.close();
	      
	Scanner s2 = new Scanner(_query);
	Pattern pattern = Pattern.compile(
	  "\"[^\"]*\"" );
	String token;
	while ((token = s2.findInLine(pattern)) != null) {
	   Vector<String> temp = new Vector<String>();
	   int end = token.length() -1;
	   token = token.substring(1, end);
	   Scanner s3 = new Scanner(token);
	   while (s3.hasNext()) {
	     temp.add(s3.next());
	   }
	   _phraseTokens.add(temp);
	      s3.close();
	   }
	   s2.close();
  }
  
}
