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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class QueryHandler implements HttpHandler {

  private static final Logger _logger = Logger.getLogger(QueryHandler.class.getName());
  private Ranker _ranker;
  private long _sessionId = 0;
  private Vector < ScoredDocument > sds;

  public QueryHandler(Ranker ranker){
    _ranker = ranker;
    try {
    	FileHandler fileTxt = new FileHandler("search.log");
    	SearchLogFormatter formatterTxt = new SearchLogFormatter();
        fileTxt.setFormatter(formatterTxt);
		_logger.addHandler(fileTxt);
	} catch (SecurityException e) {
		_logger.log(Level.SEVERE, null, e);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		_logger.log(Level.SEVERE, null, e);
	}
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
        handleSearchRequest(exchange, uriQuery);
      } else if (uriPath.equals("/result")) {
    	handleResultDisplay(exchange, uriQuery);
      }
    }
  }
  
  private void handleSearchRequest(HttpExchange exchange, String uriQuery) throws IOException {
	  _sessionId = System.currentTimeMillis() / 1000L;
	  Map<String,String> query_map = getQueryMap(uriQuery);
      Set<String> keys = query_map.keySet();
      if (keys.contains("query")){
        String queryStr = query_map.get("query");
        if (keys.contains("ranker")){
          String ranker_type = query_map.get("ranker");
          sds = getRankedResults(ranker_type, queryStr);
        } else {
          sds = _ranker.runquery(query_map.get("query"));
        }
        if (keys.contains("format")) {
      	  if (query_map.get("format").equals("html")) {
      		  send_html_response(sds, queryStr, exchange);
      		  return;
      	  }
        }
        send_text_response(sds, queryStr, exchange);
      } else {
    	  send_404(exchange);
      }
  }
  
  private void handleResultDisplay(HttpExchange exchange, String uriQuery) throws IOException {
	  Map<String,String> query_map = getQueryMap(uriQuery);
      Set<String> keys = query_map.keySet();
      String queryResponse = "";
      if (keys.contains("did")){
    	  int did = Integer.parseInt(query_map.get("did"));
    	  Document d = _ranker.getDoc(did);
    	  if (d != null) {
    		  String query = "Unavailable";
    		  if (keys.contains("q")) {
    			  query = query_map.get("q");
    		  }
    		  logAction(query, did, "click");
    		  queryResponse += "<!DOCTYPE html><html><head><title>#include Search</title><body>";
    		  queryResponse += "<p><b>"+ d.get_title_string() + "</b></p><br />";
    		  queryResponse += "<p>" + d.get_body_string() + "</p>";
    	  } else {
    		  queryResponse += "Document not found.";
    	  }
      }
      _do_send_response(exchange, queryResponse, "text/html");
  }
  
  private Vector < ScoredDocument > getRankedResults(String ranker_type, String queryStr) {
	  Vector < ScoredDocument > sds;
	  if (ranker_type.equals("cosine")){
      	sds = _ranker.cosineRanker(queryStr);
      } else if (ranker_type.equals("QL")){
      	sds = _ranker.queryLikelihoodRanker(queryStr);
      } else if (ranker_type.equals("phrase")){
      	sds = _ranker.phraseRanker(queryStr);
      } else if (ranker_type.equals("linear")){
    	sds = _ranker.linearRanker(queryStr);
      } else if (ranker_type.equals("numviews")){
      	sds = _ranker.numviewsRanker(queryStr);
      } else {
        sds = new Vector < ScoredDocument >();
      }
	  return sds;
  }
  
  private void send_text_response(Vector < ScoredDocument > sds, String query, HttpExchange exchange) throws IOException {
  	String queryResponse = "";
  	Iterator < ScoredDocument > itr = sds.iterator();
      while (itr.hasNext()){
        ScoredDocument sd = itr.next();
        logAction(query, sd._did , "render");
        if (queryResponse.length() > 0){
          queryResponse = queryResponse + "\n";
        }
        queryResponse = queryResponse + query + "\t" + sd.asString();
      }
      if (queryResponse.length() > 0){
        queryResponse = queryResponse + "\n";
      }
      _do_send_response(exchange, queryResponse, "text/plain");
  }
  
  private void send_404(HttpExchange exchange) throws IOException {
	  String queryResponse = "Error processing request. Please check your request parameters";
	  _do_send_response(exchange, queryResponse, "text/plain");
  }
  
  private void send_html_response(Vector < ScoredDocument > sds, String query, HttpExchange exchange) throws IOException {
	  	String queryResponse = "<!DOCTYPE html><html><head><title>#include Search</title><body>";
	  	queryResponse += "<p>Search Results for: <i><em>" + query + "</em></i></p>";
	  	Iterator < ScoredDocument > itr = sds.iterator();
	      while (itr.hasNext()){
	        ScoredDocument sd = itr.next();
	        logAction(query, sd._did , "render");
	        Document d = _ranker.getDoc(sd._did);
	        if (queryResponse.length() > 0) {
	          queryResponse = queryResponse + "<br />";
	        }
	        queryResponse = queryResponse + "<a href=\"/result?did=" + d._docid + "&sid=" + _sessionId + "&q=" + query + "\">" + d.get_title_string() + "</a><br />";
	        queryResponse = queryResponse + "<span style=\"color:#093\">/result?did=" + d._docid + "&nbsp;&nbsp;score: " + sd._score + "</span>";
	        queryResponse = queryResponse + "<p style=\"width:500px;\">" + d.get_body_string().substring(0, 125) + "...</p>";
	      }
	      if (queryResponse.length() > 0){
	        queryResponse = queryResponse + "<br />";
	      }
	      queryResponse += "</body></html>";
	      
	      _do_send_response(exchange, queryResponse, "text/html");
	  }
  
  private void _do_send_response(HttpExchange exchange, String queryResponse, String contentType) throws IOException {
	  Headers responseHeaders = exchange.getResponseHeaders();
      responseHeaders.set("Content-Type", "text/html");
      exchange.sendResponseHeaders(200, 0);  // arbitrary number of bytes
      OutputStream responseBody = exchange.getResponseBody();
      responseBody.write(queryResponse.getBytes());
      responseBody.close();
  }
  
  private void logAction(String query, int did, String action) {
	  _logger.info(_sessionId + "\t" + query + "\t" + did + "\t" + action);
  }
}
