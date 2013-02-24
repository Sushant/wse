package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;

class QueryHandler implements HttpHandler {

  private Ranker _ranker;

  public QueryHandler(Ranker ranker){
    _ranker = ranker;
  }

  public static Map<String, String> getQueryMap(String query){  
    String[] params = query.split("&");  
    Map<String, String> map = new HashMap<String, String>();  
    for (String param : params){  
      String name = param.split("=")[0];  
      String value = param.split("=")[1];  
      try {
		map.put(name, URLDecoder.decode(value, "UTF-8"));
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  
    }
    return map;  
  } 
  
  public void handle(HttpExchange exchange) throws IOException {
    String requestMethod = exchange.getRequestMethod();
    if (!requestMethod.equalsIgnoreCase("GET")){  // GET requests only.
      return;
    }

    // Print the user request header.
    Headers requestHeaders = exchange.getRequestHeaders();
    System.out.print("Incoming request: ");
    for (String key : requestHeaders.keySet()){
      System.out.print(key + ":" + requestHeaders.get(key) + "; ");
    }
    System.out.println();
  
    String uriQuery = exchange.getRequestURI().getQuery();
    String uriPath = exchange.getRequestURI().getPath();
    String queryResponse = "";

    if ((uriPath != null) && (uriQuery != null)){
      if (uriPath.equals("/search")){
        Map<String,String> query_map = getQueryMap(uriQuery);
        Set<String> keys = query_map.keySet();
        if (keys.contains("query")){
          if (keys.contains("ranker")){
            String ranker_type = query_map.get("ranker");
            String queryStr = query_map.get("query");
            if (ranker_type.equals("cosine")){
            	Vector < ScoredDocument > sds = _ranker.cosineRanker(queryStr);
            	queryResponse = generate_text_response(sds, queryStr);
            } else if (ranker_type.equals("QL")){
              queryResponse = (ranker_type + " not implemented.");
            } else if (ranker_type.equals("phrase")){
            	Vector < ScoredDocument > sds = _ranker.phraseRanker(queryStr);
            	queryResponse = generate_text_response(sds, queryStr);
            } else if (ranker_type.equals("linear")){
              queryResponse = (ranker_type + " not implemented.");
            } else if (ranker_type.equals("numviews")){
            	Vector < ScoredDocument > sds = _ranker.numviewsRanker(queryStr);
            	queryResponse = generate_text_response(sds, queryStr);
            } else {
              queryResponse = (ranker_type+" not implemented.");
            }
          } else {
            // @CS2580: The following is instructor's simple ranker that does not
            // use the Ranker class.
            Vector < ScoredDocument > sds = _ranker.runquery(query_map.get("query"));
            queryResponse = generate_text_response(sds, query_map.get("query"));
          }
        }
      }
    }
    
    
      // Construct a simple response.
      Headers responseHeaders = exchange.getResponseHeaders();
      responseHeaders.set("Content-Type", "text/plain");
      exchange.sendResponseHeaders(200, 0);  // arbitrary number of bytes
      OutputStream responseBody = exchange.getResponseBody();
      responseBody.write(queryResponse.getBytes());
      responseBody.close();
  }
  
  private static String generate_text_response(Vector < ScoredDocument > sds, String query) {
  	String queryResponse = "";
  	Iterator < ScoredDocument > itr = sds.iterator();
      while (itr.hasNext()){
        ScoredDocument sd = itr.next();
        if (queryResponse.length() > 0){
          queryResponse = queryResponse + "\n";
        }
        queryResponse = queryResponse + query + "\t" + sd.asString();
      }
      if (queryResponse.length() > 0){
        queryResponse = queryResponse + "\n";
      }
      return queryResponse;
  }
}
