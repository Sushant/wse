package edu.nyu.cs.cs2580;

import java.net.URL;

import net.htmlparser.jericho.Source;

class ExtractText {
	public static String extractText(String url) throws Exception {
		String sourceUrlString = url;
		if (sourceUrlString.indexOf(':') == -1)
			sourceUrlString = "file:" + sourceUrlString;
		Source source = new Source(new URL(sourceUrlString));
		source.fullSequentialParse();
		return source.getTextExtractor().toString();
	}
}