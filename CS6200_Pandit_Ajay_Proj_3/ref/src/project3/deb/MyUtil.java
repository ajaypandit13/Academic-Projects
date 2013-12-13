package project3.deb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.lucid.analysis.LucidKStemmer;


public class MyUtil {
	
	static ArrayList<String> stopList = getStopList();
	static List<String> htmlTags = Arrays.asList((String[])new String[]{"html", "pre"});
	
	private static boolean isStopWord(String st){
		return stopList.contains(st);
	}
	
		
	private static ArrayList<String> getStopList() {
		ArrayList<String> sList = new ArrayList<String>();
		//Path p = Paths.get("stoplist.txt");
		Scanner sc = null;;
		try {
			sc = new Scanner(new BufferedReader(new FileReader("stoplist.txt")));
			while(sc.hasNext()){
				sList.add(sc.nextLine().trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(sc != null)
				sc.close();
		}
		
		return sList;
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	public static ArrayList<String> getTerms(String line){
		if(isIndexLine(line)){
			return null;
		}
		line = removeInvalidChar(line);
		line = line.trim().replaceAll("\\s+", " ");
		ArrayList<String> retList = new ArrayList<String>();
		String[] words = line.split(" ");
		for(int i=0;i<words.length;i++){
			//String tempStr = removeInvalidChar(words[i]);
			if(! isStopWord(words[i]) && ! isHTML(words[i])){
				String s = getStemmedWord(words[i]);
				if(s.length() != 1){
					retList.add(s);
				}
			}
		}
		
		return retList;
	}

	private static String removeInvalidChar(String str) {
		str = str.toLowerCase().trim();
//		str = str.replaceAll(",",""); //Remove all ,
//		str = str.replaceAll("\\.",""); //Remove all .
//		str = str.replaceAll("\'",""); //Remove all '
//		str = str.replaceAll("\"",""); //Remove all "
//		str = str.replaceAll("\\?",""); //Remove all "
		return str.replaceAll("[^\\p{L}\\p{N}]", " ");
	}

	public static boolean isEmpty(String s){
		return (s == null || "".equals(s.trim()));
	}
	
	public static String getStemmedWord(String st){
		return new LucidKStemmer().stem(st);
	}
	
	public static void main(String[] args){
		//System.out.println(new LucidKStemmer().stem("logicially"));
		System.out.println(getTerms("there's high-tech."));
		//System.out.println(isIndexLine("1159	5	14"));
	}
	
	private static boolean isIndexLine(String line){
		line = line.trim().replaceAll("\\s+", " ");
		String[] words = line.split(" ");
		return ((words.length == 3) && isAllNumbers(words));
	}


	private static boolean isAllNumbers(String[] words) {
		for(int i=0;i<words.length;i++){
			char[] chars = words[i].toCharArray();
			for(int j=0;j<chars.length;j++){
				if(! Character.isDigit(chars[j])){
					return false;
				}
			}
		}
		return true;
	}
	
	private static boolean isHTML(String line){
		line = line.trim().replaceAll("\\s+", " ");
		return htmlTags.contains(line);
	}
}
