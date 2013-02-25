package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Vector;
import java.util.HashMap;
import java.util.Scanner;

class Evaluator {

	public static void main(String[] args) throws IOException {
		HashMap<String, HashMap<Integer, Double>> relevance_judgments = new HashMap<String, HashMap<Integer, Double>>();
		if (args.length < 1) {
			System.out.println("need to provide relevance_judgments");
			return;
		}
		String p = args[0];
		// first read the relevance judgments into the HashMap
		readRelevanceJudgments(p, relevance_judgments);
		// now evaluate the results from stdin
		evaluateStdin(relevance_judgments);
	}

	public static void readRelevanceJudgments(String p,
			HashMap<String, HashMap<Integer, Double>> relevance_judgments) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(p));
			try {
				String line = null;
				while ((line = reader.readLine()) != null) {
					// parse the query,did,relevance line
					Scanner s = new Scanner(line).useDelimiter("\t");
					String query = s.next();
					int did = Integer.parseInt(s.next());
					String grade = s.next();
					double rel = 0.0;
					// convert to binary relevance
					if ((grade.equals("Perfect"))
							|| (grade.equals("Excellent"))
							|| (grade.equals("Good"))) {
						rel = 1.0;
					}
					if (relevance_judgments.containsKey(query) == false) {
						HashMap<Integer, Double> qr = new HashMap<Integer, Double>();
						relevance_judgments.put(query, qr);
					}
					HashMap<Integer, Double> qr = relevance_judgments
							.get(query);
					qr.put(did, rel);
				}
			} finally {
				reader.close();
			}
		} catch (IOException ioe) {
			System.err.println("Oops " + ioe.getMessage());
		}
	}

	
	public static void evaluateStdin(
			HashMap<String, HashMap<Integer, Double>> relevance_judgments) {
		// only consider one query per call
		int counter = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));

			String line = null;
			double RR = 0.0;
			double N = 0.0;
			double f = 0.0;
			double count = 0.0;
			boolean flag = false;
			while ((line = reader.readLine()) != null) {
				++count;
				Scanner s = new Scanner(line).useDelimiter("\t");
				String query = s.next();
				int did = Integer.parseInt(s.next());
				String title = s.next();
				double rel = Double.parseDouble(s.next());
				if (relevance_judgments.containsKey(query) == false) {
					throw new IOException("query not found");
				}
				HashMap<Integer, Double> qr = relevance_judgments.get(query);
				if (qr.containsKey(did) != false) {
					if(!flag){
						f = 1 / count;
						flag = true;
					}
					RR += qr.get(did);
				}
				if(counter == 0){
					System.out.println("Precision@1: " + RR);
					System.out.println("Reciprocal_rank@1: " + f);
				}
				if(counter == 4){
					System.out.println("Precision@5: " + (RR/5));
					System.out.println("Reciprocal_rank@5: " + f);
				}
				if(counter == 9){
					System.out.println("Precision@10: " + (RR/10));
					System.out.println("Reciprocal_rank@10: " + f);
				}
				++N;
				counter++;
			}
			System.out.println(Double.toString(RR / N));
		} catch (Exception e) {
			System.err.println("Error:" + e.getMessage());
		}
	}
}
