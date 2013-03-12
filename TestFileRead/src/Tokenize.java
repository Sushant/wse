import java.util.Scanner;
import java.util.regex.Pattern;


public class Tokenize {
	public static void main(String[] args) {
		String s1 = "[^a-zA-Z0-9]";
		Pattern pattern = Pattern.compile(s1);
		String s = "Prasad,nayan, PRATHAMESH";
		Scanner scan = new Scanner(s);
		scan.useDelimiter(pattern);
	     while (scan.hasNext()) {
	         System.out.println(scan.next());
	     }
	}
}
