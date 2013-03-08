package edu.nyu.cs.cs2580;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;

class Utility {
	public List<String> tokenize(String input) {
		List<String> tokens = new ArrayList<String>();
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		Scanner scan = new Scanner(input);
		scan.useDelimiter(pattern);
		while (scan.hasNext()) {
			String next = scan.next();
			if (!next.isEmpty())
				tokens.add(scan.next());
		}
		return tokens;
	}

	public static String extractText(String url) throws Exception {
		String sourceUrlString = url;
		if (sourceUrlString.indexOf(':') == -1)
			sourceUrlString = "file:" + sourceUrlString;
		Source source = new Source(new URL(sourceUrlString));
		source.fullSequentialParse();
		return source.getTextExtractor().toString();
	}

	public List<String> getFilesInDirectory(String directory) {
		File folder = new File(directory);
		List<String> files = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			files.add(fileEntry.getName());
		}
		return files;
	}
}
