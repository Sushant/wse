package edu.nyu.cs.cs2580;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SearchLogFormatter extends Formatter {

	@Override
	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer(1000);
		buf.append(rec.getMessage() + "\t" + calcDate(rec.getMillis()) + "\n"); 
		return buf.toString();
	}

	private String calcDate(long millisecs) {
	    SimpleDateFormat date_format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	    Date resultdate = new Date(millisecs);
	    return date_format.format(resultdate);
	  }
}
