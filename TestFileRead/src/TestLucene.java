import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
public class TestLucene {

	static List<String> tokenize(TokenStream stream) throws IOException {
		List<String> tokens = new ArrayList<String>();
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		while (stream.incrementToken()) {
			tokens.add(cattr.toString());
		}
		stream.end();
		stream.close();
		return tokens;

	}

	public static void main(String arg[]) throws IOException {
		/*Analyzer an = new EnglishAnalyzer(Version.LUCENE_30, new HashSet<String>());
		List<String> tokens = tokenize(an
				.tokenStream(
						"Nayan",
						new StringReader(
								"Nayan Antarctica abc,efg is i.b.m.?????  \n Earth's southernmost continent, containing the geographic South Pole. It is situated in the Antarctic region of the Southern Hemisphere, almost entirely south of the Antarctic Circle, and is surrounded by the Southern Ocean. At 14.0 millionå km2 (5.4 millionå sqå mi), it is the fifth-largest continent in area after Asia, Africa, North America, and South America. For comparison, Antarctica is nearly twice the size of Australia. About 98% of Antarctica is covered by ice that averages at least 1 mile (1.6å km) in thickness. Antarctica, on average, is the coldest, driest, and windiest continent, and has the highest average elevation of all the continents.[4] Antarctica is considered a desert, with annual precipitation of only 200å mm (8å inches) along the coast and far less inland. The temperature in Antarctica has reached ‰öÕ89 å¡C (‰öÕ129 å¡F). There are no permanent human residents, but anywhere from 1,000 to 5,000 people reside throughout the year at the research stations scattered across the continent. Only cold-adapted organisms survive there, including many types of algae, animals (for example mites, nematodes, penguins, seals and tardigrades), bacteria, fungi, plants, and protista. Vegetation where it occurs is tundra.")));
		for (String token : tokens) {
			System.out.println(token);
		}*/
		/*String search = "answer";
		String fileName = "/Users/prasadkapde/Desktop/wse/hw2/data/index/" + search.charAt(0);
		System.out.println(fileName);
		String cmd = "grep '\\<"+search+"\\>' " + fileName;
		List<String> commands = new ArrayList<String>();
		commands.add("/bin/bash");
		commands.add("-c");
		commands.add(cmd);
		System.out.println(cmd);
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process p = pb.start();
		InputStreamReader isr = new InputStreamReader(p.getInputStream());
		InputStreamReader isr1 = new InputStreamReader(p.getErrorStream());
		BufferedReader br1 = new BufferedReader(isr1);
		BufferedReader br = new BufferedReader(isr);
		String s = null;
		System.out.println(br.readLine());
		System.out.println(p.exitValue());*/
		String test = "20000";
		Integer i = 20000;
		//byte[] bytes = test.getBytes();
		//Byte by = new Byte((byte) 81);
		List<Byte> list = new ArrayList<Byte>();
		File file = new File("/Users/prasadkapde/Desktop/temp/test");
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(i);
		out.close();
		
		Scanner scan = new Scanner(file);
		System.out.println(scan.next());
		
	}

}