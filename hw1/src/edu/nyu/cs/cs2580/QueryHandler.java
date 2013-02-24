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
  private Vector < ScoredDocument > sds;

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

    if ((uriPath != null) && (uriQuery != null)){
      if (uriPath.equals("/search")){
        Map<String,String> query_map = getQueryMap(uriQuery);
        Set<String> keys = query_map.keySet();
        if (keys.contains("query")){
          String queryStr = query_map.get("query");
          if (keys.contains("ranker")){
            String ranker_type = query_map.get("ranker");
            if (ranker_type.equals("cosine")){
            	sds = _ranker.cosineRanker(queryStr);
            } else if (ranker_type.equals("QL")){
            	sds = _ranker.queryLikelihoodRanker(queryStr);
            } else if (ranker_type.equals("phrase")){
            	sds = _ranker.phraseRanker(queryStr);
            } else if (ranker_type.equals("linear")){
            	sds = new Vector < ScoredDocument >();
            } else if (ranker_type.equals("numviews")){
            	sds = _ranker.numviewsRanker(queryStr);
            } else {
              sds = new Vector < ScoredDocument >();
            }
          } else {
            // @CS2580: The following is instructor's simple ranker that does not
            // use the Ranker class.
            sds = _ranker.runquery(query_map.get("query"));
          }
          if (keys.contains("format")) {
        	  if (query_map.get("format").equals("html")) {
        		  send_html_response(sds, queryStr, exchange);
        		  return;
        	  }
          }
          send_text_response(sds, queryStr, exchange);
        }
      }
    }
    
    
      
  }
  
  private static void send_text_response(Vector < ScoredDocument > sds, String query, HttpExchange exchange) throws IOException {
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

      Headers responseHeaders = exchange.getResponseHeaders();
      responseHeaders.set("Content-Type", "text/plain");
      exchange.sendResponseHeaders(200, 0);  // arbitrary number of bytes
      OutputStream responseBody = exchange.getResponseBody();
      responseBody.write(queryResponse.getBytes());
      responseBody.close();
  }
  
  private static void send_html_response(Vector < ScoredDocument > sds, String query, HttpExchange exchange) throws IOException {
	  	String queryResponse = "<!DOCTYPE html><html><head><title>#include Search</title><body>";
	  	queryResponse += "<p>Search Results for: <i><em>" + query + "</em></i></p>";
	  	Iterator < ScoredDocument > itr = sds.iterator();
	  	//int idx = 1;
	      while (itr.hasNext()){
	        ScoredDocument sd = itr.next();
	        if (queryResponse.length() > 0){
	          queryResponse = queryResponse + "<br />";
	        }
	        queryResponse = queryResponse + "<li>" + sd.asString() + "</li>";
	      }
	      if (queryResponse.length() > 0){
	        queryResponse = queryResponse + "<br />";
	      }
	      queryResponse += "</body></html>";
	      
	      Headers responseHeaders = exchange.getResponseHeaders();
	      responseHeaders.set("Content-Type", "text/html");
	      exchange.sendResponseHeaders(200, 0);  // arbitrary number of bytes
	      OutputStream responseBody = exchange.getResponseBody();
	      responseBody.write(queryResponse.getBytes());
	      responseBody.close();
	  }
}
