import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Date;

public class Tester {
	public static void main(String[] args) {
		/*
		 * PriorityQueue<Pair> queue = new PriorityQueue<Pair>(2,new
		 * Comparator<Object>(){
		 * 
		 * @Override public int compare(Object p1,Object p2){ Pair p3 = (Pair)
		 * p1; Pair p4 = (Pair) p2; if (p4.i < p3.i) return 1; else return -1; }
		 * }); queue.add(new Pair("t",4)); queue.add(new Pair("t",6));
		 * queue.add(new Pair("t",1)); queue.add(new Pair("t",2)); queue.add(new
		 * Pair("t",8)); queue.add(new Pair("t",7));
		 * 
		 * while(!queue.isEmpty()){ System.out.println(queue.poll()); }
		 */
		long start = (new Date()).getTime();
		System.out.println(start);
	}
}
