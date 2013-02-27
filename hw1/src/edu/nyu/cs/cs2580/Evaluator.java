package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

class Evaluator {

	public static void main(String[] args) throws IOException {
		HashMap<String, HashMap<Integer, Double>> relevance_judgments = new HashMap<String, HashMap<Integer, Double>>();
		HashMap<String, HashMap<Integer, Double>> relevance_judgmentsNDCG = new HashMap<String, HashMap<Integer, Double>>();
		if (args.length < 1) {
			System.out.println("need to provide relevance_judgments");
			return;
		}
		String p = args[0];
		// first read the relevance judgments into the HashMap
		readRelevanceJudgments(p, relevance_judgments);
		readRelevanceJudgmentsNDCG(p, relevance_judgmentsNDCG);
		// now evaluate the results from stdin
		evaluateStdin(relevance_judgments, relevance_judgmentsNDCG);
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

	public static void readRelevanceJudgmentsNDCG(String p,
			HashMap<String, HashMap<Integer, Double>> relevance_judgmentsNDCG) {
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
					double rel;
					// convert to NDCG relevance
					if (grade.equals("Perfect"))
						rel = 10.0;
					else if (grade.equals("Excellent"))
						rel = 7.0;
					else if (grade.equals("Good"))
						rel = 5.0;
					else if (grade.equals("Fair"))
						rel = 1.0;
					else
						rel = 0.0;

					if (relevance_judgmentsNDCG.containsKey(query) == false) {
						HashMap<Integer, Double> qr = new HashMap<Integer, Double>();
						relevance_judgmentsNDCG.put(query, qr);
					}
					HashMap<Integer, Double> qr = relevance_judgmentsNDCG
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
			HashMap<String, HashMap<Integer, Double>> relevance_judgments,
			HashMap<String, HashMap<Integer, Double>> relevance_judgmentsNDCG) {
		// only consider one query per call
		int counter = 0;
		Map<Double, Double> recallPrecisionMap = new TreeMap<Double, Double>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));

			String line = null;
			double RR = 0.0, N = 0.0, AP = 0.0, count = 0.0, totalRecall = 0.0;
			double f = 0.0, DCG = 0.0, Max_DCG = 0.0, NDCG = 0.0;
			boolean flag = false;
			double precisionAt1 = 0.0, precisionAt5 = 0.0, precisionAt10 = 0.0;
			double recallAt1 = 0.0, recallAt5 = 0.0, recallAt10 = 0.0;
			double Fat1 = 0.0, Fat5 = 0.0, Fat10 = 0.0;
			double averagePrecision = 0.0;
			double NDCGat1 = 0.0, NDCGat5 = 0.0, NDCGat10 = 0.0;
			double reciprocalRank = 0.0,relevance = 0.0;
			String queryName = null;
			while ((line = reader.readLine()) != null) {
				++count;
				Scanner s = new Scanner(line).useDelimiter("\t");
				String query = s.next();
				queryName = query;
				int did = Integer.parseInt(s.next());
				String title = s.next();
				double rel = Double.parseDouble(s.next());
				if (relevance_judgments.containsKey(query) == false) {
					throw new IOException("query not found");
				}

				if (relevance_judgmentsNDCG.containsKey(query) == false) {
					throw new IOException("query not found");
				}
				Map<Integer, Double> qr = relevance_judgments.get(query);
				Map<Integer, Double> qr_ndcg = relevance_judgmentsNDCG
						.get(query);
				Map<Integer, Double> qr_ndcg_sorted = relevance_judgmentsNDCG
						.get(query);

				int numberOfRelevantDocs = numberOfRelevantDocs(qr);
				if (qr.containsKey(did) != false) {
					if (!flag) {
						f = 1 / count;
						flag = true;
					}
					RR += qr.get(did);
				}
				if(qr.containsKey(did) && qr.get(did) == 1.0){
					relevance++;
					AP = AP + relevance/(N+1);
				}
				if (!qr.containsKey(did)) {
					qr_ndcg.put(did, 0.0);
					qr_ndcg_sorted.put(did, 0.0);
					qr.put(did, 0.0);
				}

				qr_ndcg_sorted = sortMapByValues(qr_ndcg);
				DCG = updateDCG(DCG, count, qr_ndcg, did);

				if (counter == 0) {
					reciprocalRank = f;
					double precision = RR;
					double recall = 0.0;
					if (numberOfRelevantDocs != 0) {
						recall = RR / numberOfRelevantDocs;
						recallAt1 = RR / numberOfRelevantDocs;
					}
					precisionAt1 = RR;
					double intermediate = 0.0;
					if (recall != 0) {
						intermediate = (0.5 / precision) + (0.5 / recall);
					} else {
						intermediate = (0.5 / precision);
					}
					double F = Math.pow(intermediate, -1);
					Fat1 = F;
					Max_DCG = findDCG_Max(count, qr_ndcg_sorted);
					NDCG = updateNDCG(DCG, Max_DCG);
					NDCGat1 = NDCG;
				} else if (counter == 4) {
					reciprocalRank = f;
					double precision = RR / 5;
					double recall = 0.0;
					if (numberOfRelevantDocs != 0) {
						recall = RR / numberOfRelevantDocs;
						recallAt5 = RR / numberOfRelevantDocs;
					}
					precisionAt5 = RR / 5;
					double intermediate = 0.0;
					if (recall != 0) {
						intermediate = (0.5 / precision) + (0.5 / recall);
					} else {
						intermediate = (0.5 / precision);
					}
					double F = Math.pow(intermediate, -1);
					Fat5 = F;
					Max_DCG = findDCG_Max(count, qr_ndcg_sorted);
					NDCG = updateNDCG(DCG, Max_DCG);
					NDCGat5 = NDCG;
				} else if (counter == 9) {
					reciprocalRank = f;
					double precision = RR / 10;
					double recall = 0.0;
					if (numberOfRelevantDocs != 0) {
						recall = RR / numberOfRelevantDocs;
						recallAt10 = RR / numberOfRelevantDocs;
					}
					precisionAt10 = RR / 10;
					double intermediate = 0.0;
					if (recall != 0) {
						intermediate = (0.5 / precision) + (0.5 / recall);
					} else {
						intermediate = (0.5 / precision);
					}
					double F = Math.pow(intermediate, -1);
					Fat10 = F;
					Max_DCG = findDCG_Max(count, qr_ndcg_sorted);
					NDCG = updateNDCG(DCG, Max_DCG);
					NDCGat10 = NDCG;
				}
				totalRecall = RR / numberOfRelevantDocs;
				if (totalRecall <= 1) {
					double tempPrecision = RR / (counter + 1);

					if (recallPrecisionMap.get(totalRecall) != null) {
						double precisionAtRecall = recallPrecisionMap
								.get(totalRecall);
						double precisionAtRecallPoint = (tempPrecision > precisionAtRecall) ? tempPrecision
								: precisionAtRecall;
						recallPrecisionMap.put(totalRecall,
								precisionAtRecallPoint);
					} else {
						recallPrecisionMap.put(totalRecall, tempPrecision);
					}
				}
				++N;
				counter++;
			}
			if (relevance != 0) {
				averagePrecision = AP / relevance;
			} else {
				averagePrecision = 0.0;
			}
			System.out.print(queryName + "\t" + precisionAt1 + "\t"
					+ precisionAt5 + "\t" + precisionAt10 + "\t" + recallAt1
					+ "\t" + recallAt5 + "\t" + recallAt10 + "\t" + Fat1 + "\t"
					+ Fat5 + "\t" + Fat10);
			for (int i = 0; i <= 10; i++) {
				double max = Double.MIN_VALUE;
				for (Map.Entry<Double, Double> entry : recallPrecisionMap
						.entrySet()) {
					if (max < entry.getValue()) {
						max = entry.getValue();
					}
				}
				System.out.print("\t" + max);
				recallPrecisionMap.remove(((double) i / (double) 10));
				for (Map.Entry<Double, Double> entry : recallPrecisionMap
						.entrySet()) {
					if (entry.getKey() < (double) i / (double) 10) {
						entry.setValue(Double.MIN_VALUE);
					}
				}
			}

			System.out.print("\t" + averagePrecision + "\t" + NDCGat1 + "\t"
					+ NDCGat5 + "\t" + NDCGat10 + "\t" + reciprocalRank);
			System.out.print("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static double updateDCG(double DCG, double count,
			Map<Integer, Double> qr_ndcg, int did) {
		double log_2 = Math.log(count) / Math.log(2.0);
		if(count == 1){
			DCG = qr_ndcg.get(did);
		}
		else if (qr_ndcg.get(did) != null) {
			DCG = DCG + (qr_ndcg.get(did) / log_2);
		}
		return DCG;
	}

	private static double findDCG_Max(double count,
			Map<Integer, Double> qr_ndcg_sorted) {
		double num = 0.0;
		double Max_DCG = 0.0;

		Iterator<Entry<Integer, Double>> it = qr_ndcg_sorted.entrySet()
				.iterator();

		Map.Entry pairs = (Map.Entry) it.next();
		Max_DCG = (Double) pairs.getValue();
		num = 1.0;
		while (it.hasNext() && num < count) {
			double log_2 = Math.log(num + 1) / Math.log(2.0);
			pairs = (Map.Entry) it.next();
			Max_DCG = Max_DCG + (Double) pairs.getValue() / log_2;
			num++;
		}
		return Max_DCG;
	}

	private static double updateNDCG(double DCG, double Max_DCG) {
		double NDCG = 0.0;
		if (Max_DCG != 0.0) {
			NDCG = DCG / Max_DCG;
		}
		return NDCG;
	}

	private static int numberOfRelevantDocs(Map<Integer, Double> qr) {
		int count = 0;
		for (Map.Entry<Integer, Double> entry : qr.entrySet()) {
			if (entry.getValue() > 0) {
				count++;
			}
		}
		return count;
	}

	private static Map<Integer, Double> sortMapByValues(
			Map<Integer, Double> myMap) {
		ArrayList as = new ArrayList(myMap.entrySet());
		Collections.sort(as, new Comparator() {
			public int compare(Object o1, Object o2) {
				Map.Entry e1 = (Map.Entry) o1;
				Map.Entry e2 = (Map.Entry) o2;
				Double first = (Double) e1.getValue();
				Double second = (Double) e2.getValue();
				return second.compareTo(first);
			}
		});

		Map<Integer, Double> my = new LinkedHashMap<Integer, Double>();
		Iterator i = as.iterator();
		while (i.hasNext()) {
			String i1 = i.next().toString();
			String[] split = i1.split("=");
			my.put(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
		}
		return my;
	}

}
