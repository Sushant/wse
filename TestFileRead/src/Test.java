import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Test {
	public static void listFilesForFolder(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				System.out.println(fileEntry.getName());
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		String binary = Integer.toBinaryString(128128978);
		System.out.println(binary);
		String st = "";
		int counter = 1;
		boolean flag = true;
		for (int i = binary.length() - 1; i >= 0; i--) {
			if (counter == 8) {
				if (flag) {
					st += "1";
					st += binary.charAt(i);
					flag = false;
				} else {
					st += "0";
					st += binary.charAt(i);
				}
				counter = 1;
			} else {
				st += binary.charAt(i);
			}
			counter++;
		}
		int size = st.length();
		String temp = "";
		flag = true;
		while (size % 8 != 0) {
			if (size < 8) {
				if (flag) {
					temp += "1";
					flag = false;
				} else {
					temp += "0";
				}
			} else {
				temp += "0";
			}
			size++;
		}
		String finalSt = "";
		for (int i = st.length() - 1; i >= 0; i--) {
			finalSt += st.charAt(i);
		}
		finalSt = temp + finalSt;
		System.out.println(finalSt);
		int i = Integer.parseInt(finalSt, 2);
		String hexString = Integer.toHexString(i);
		System.out.println(hexString);
		decompress(hexString);
		List<String> tempList = new ArrayList<String>();
		tempList.add("1");
		tempList.add("1");
		tempList.add("1");
		tempList.add("7");
		tempList.add("2");
		tempList.add("6");
		tempList.add("2");
		tempList.add("17");
		tempList.add("2");
		tempList.add("197");
		tempList.add("3");
		tempList.add("1");
		returnList(tempList);
	}

	static void decompress(String s) {
		Integer i = Integer.parseInt(s, 16);
		String i1 = Integer.toBinaryString(i);
		System.out.println(i1);
		int counter = 1;
		String ft = "";
		for (int x = i1.length() - 1; x >= 0; x--) {
			if (counter != 8) {
				ft += i1.charAt(x);
			} else {
				counter = 0;
			}
			counter++;
		}
		System.out.println(ft);
		String gt = "";
		for (int x = ft.length() - 1; x >= 0; x--) {
			gt += ft.charAt(x);
		}
		System.out.println(gt);
		Integer i5 = Integer.parseInt(gt, 2);
		System.out.println(i5);
	}

	static void returnList(List<String> list) {
		System.out.println(list);
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		for (int i = 0; i < list.size() - 1; i = i + 2) {
			if (map.containsKey(list.get(i))) {
				List<String> tempList = map.get(list.get(i));
				tempList.add(list.get(i + 1));
			} else {
				List<String> tempList = new ArrayList<String>();
				tempList.add(list.get(i + 1));
				map.put(list.get(i), tempList);
			}
		}
		List<String> returnList = new ArrayList<String>();
		String nextKey = "0";
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			returnList.add(String.valueOf(Integer.parseInt(entry.getKey()) - Integer.parseInt(nextKey)));
			nextKey = entry.getKey();
			returnList.add(String.valueOf(entry.getValue().size()));
			List<String> tempList = entry.getValue();
			returnList.add(tempList.get(0));
			for (int i = 1; i < tempList.size(); i++) {
				returnList.add(String.valueOf(Integer.parseInt(tempList.get(i)) - Integer.parseInt(tempList.get(i-1))));
			}
		}
		System.out.println(map);
		System.out.println(returnList);
		createList(returnList);
	}
	static void createList(List<String> list){
		int size = 0;
		Map<String,List<String>> map = new LinkedHashMap<String,List<String>>();
		String st = "";
		Integer lastSt = 0;
		while(size < list.size()){
			st = list.get(size);
			st = String.valueOf(Integer.parseInt(st) + lastSt);
			lastSt = Integer.parseInt(st);
			size = size + 1;
			int tempSize = Integer.parseInt(list.get(size));
			System.out.println("size:" + tempSize);
			size++;
			int temp = 0;
			List<String> tempList = new ArrayList<String>();
			while (temp < tempSize ){
				tempList.add(list.get(size));
				size++;
				temp++;
			}
			map.put(st, tempList);
		}
		int counter = 0;
		for(Map.Entry<String, List<String>> entry : map.entrySet()){
			List<String> returnList = new ArrayList<String>();
			List<String> tempList = entry.getValue();
			returnList.add(tempList.get(0));
			for(int i = 1 ; i < tempList.size() ; i++){
				returnList.add(String.valueOf(Integer.parseInt(returnList.get(returnList.size() - 1)) + Integer.parseInt(tempList.get(i))));
			}
			entry.setValue(returnList);
		}
		System.out.println(map);
	}
}
