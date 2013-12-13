package project3.deb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;


public class ProcessFile extends Thread{
	
	String fileName = null;
	ArrayList<String> finalList = null;
	Corpus cor;
	
	public ProcessFile(String fileName, Corpus cor){
		this.fileName = fileName;
		this.finalList = new ArrayList<String>();
		this.cor = cor;
	}

	public void run(){		
		//Path path = Paths.get(fileName);
		Scanner sc = null;
		try {
			sc = new Scanner(new BufferedReader(new FileReader(fileName)));
			while(sc.hasNext()){
				String line = sc.nextLine();
				ArrayList<String> terms = MyUtil.getTerms(line);
				if(terms == null || terms.size() == 0)
					continue;
				for(String s : terms){
					if(MyUtil.isEmpty(s))
						continue;
					if(! finalList.contains(s)){
						finalList.add(s);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(sc != null){
				sc.close();
			}
		}	
		//System.out.println(finalList);
		cor.updateCorpusAfterProcessingFile(fileName.substring(fileName.lastIndexOf("/") +1, 
				fileName.lastIndexOf(".html")), finalList);
	}
}
