package project3.deb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


public class Indexing {

	
	
	public static void main(String[] args) throws IOException {
		
		long startTime = new Date().getTime();
		
		//Corpus cor = new Corpus();
		//ArrayList<ProcessFile> list_po = new ArrayList<ProcessFile>();
		
		HashMap<String, Integer> term_term_id_map = new HashMap<String, Integer>();
		HashMap<String, Integer> doc_doc_id_map = new HashMap<String, Integer>();
		
		int term_id = 1;
		int doc_id = 1;
		
		File folder = new File(IndexConstants.REPOSITORY_FOLDER);
		
		for (final File fileEntry : folder.listFiles()) {
			//System.out.println(fileEntry.getAbsolutePath());
			/*ProcessFile pf = new ProcessFile(fileEntry.getAbsolutePath(), cor);
			pf.start();
			list_po.add(pf);*/
			
			String abs_file_name = fileEntry.getAbsolutePath();
			String file_name = abs_file_name.substring(abs_file_name.lastIndexOf("/") +1, 
					abs_file_name.lastIndexOf(".html"));
			
			if(! doc_doc_id_map.containsKey(file_name)){
				doc_doc_id_map.put(file_name, doc_id++);
			}
			
			int doc_len = 0;
			
			Scanner sc = null;
			HashMap<String, Integer> term_tf_map = new HashMap<String, Integer>();
			try {
				
				
				sc = new Scanner(new BufferedReader(new FileReader(abs_file_name)));
				while(sc.hasNext()){
					String line = sc.nextLine();
					ArrayList<String> terms = MyUtil.getTerms(line);
					if(terms == null || terms.size() == 0)
						continue;
					for(String s : terms){
						if(MyUtil.isEmpty(s))
							continue;
						
						if(term_tf_map.containsKey(s)){
							int cur_tf = term_tf_map.remove(s);
							term_tf_map.put(s, ++cur_tf);
						}else{
							term_tf_map.put(s, 1);
						}
						
						if(! term_term_id_map.containsKey(s)){
							term_term_id_map.put(s, term_id++);							
						}
						doc_len++;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(sc != null){
					sc.close();
				}
			}
			//Done with reading a file
			//Create or update the info in the 'term' file; e.g.: business
			// doc_id tf
			for(Map.Entry<String, Integer> e : term_tf_map.entrySet()){
				String newLine = new StringBuffer().append(doc_doc_id_map.get(file_name))
						.append(" ").append(e.getValue()).toString();
				
				File file = new File(IndexConstants.TERMS_FOLDER+"/"+e.getKey());
				
				if(file.exists()){
					Scanner sc1 = new Scanner(new BufferedReader(new FileReader(file)));
					StringBuffer sb = new StringBuffer();
					while(sc1.hasNext()){
						sb.append(sc1.nextLine()).append("\n");
					}
					sc1.close();
					sb.append(newLine);
					newLine = sb.toString();
					
					file.delete();
				}
				BufferedWriter bw = new BufferedWriter
						(new FileWriter(IndexConstants.TERMS_FOLDER+"/"+e.getKey()));
				bw.write(newLine);
				bw.close();
				
			}
			//At this point for each unique term a file has been created 
			
			//Create/update the doc_id map file
			//doc_id_map.txt : doc_name doc_id doc_len
			RandomAccessFile docRaf = new RandomAccessFile
					(new File(IndexConstants.ROOT_FOLDER+"/doc_id_map"), "rwd");
			docRaf.seek(docRaf.length());
			String toWrite = new StringBuffer().append(doc_doc_id_map.get(file_name))
					.append(" ").append(file_name)
					.append(" ").append(doc_len).append("\n").toString();
			docRaf.write(toWrite.getBytes());
			docRaf.close();
			
		}
		
		
		
		
		
		// Now lets create two files:
		// term_info and inverted_index files.
		// term_info : term term_id offset length
		// inverted_index : term_id ctf df (doc_id doc_len tf)...
		// doc_ids are stored as deltas
		
		BufferedWriter bw = new BufferedWriter
				(new FileWriter(IndexConstants.ROOT_FOLDER + "/term_info"));
		RandomAccessFile invRaf = new RandomAccessFile
				(new File(IndexConstants.ROOT_FOLDER + "/inverted_index"), "rwd");
		
		//long total_no_of_documents = 0;
		long total_no_of_words = 0;
		
		folder = new File(IndexConstants.TERMS_FOLDER);
		
		for (final File fileEntry : folder.listFiles()) {
			Scanner sc = new Scanner(new BufferedReader(new FileReader(fileEntry)));
			long ctf = 0;
			int df = 0;
			long lastDocId = 0;
			StringBuffer sb = new StringBuffer();
			
			while(sc.hasNext()){
				String line = sc.nextLine();
				String[] tokens = line.split(" ");
				int deltaDocId = (int) (Long.parseLong(tokens[0]) - lastDocId);
				lastDocId = Long.parseLong(tokens[0]);
				int tf = Integer.parseInt(tokens[1]);
				ctf += tf;
				df++;
				//sb.append(deltaDocId).append(" ").append(tf).append(" ");
				sb.append(tokens[0]).append(" ").append(tf).append(" ");
				//sb.append("[").append(tokens[0]).append(" ").append(tf).append("] ");
			}
			
			byte[] toWriteToInvIndx = new StringBuffer().append(term_term_id_map.get(fileEntry.getName()))
			.append(" ").append(ctf).append(" ").append(df).append(" ").append(sb.toString())
			.toString().getBytes();
			
			long offset = invRaf.length();
			int length = toWriteToInvIndx.length;
			
			String toWriteToTermInfo = new StringBuffer().append(fileEntry.getName()).append(" ")
			.append(term_term_id_map.get(fileEntry.getName())).append(" ").append(offset)
			.append(" ").append(length).toString();
			bw.write(toWriteToTermInfo);
			bw.newLine();
			
			invRaf.seek(offset);
			invRaf.write(toWriteToInvIndx);
			
			total_no_of_words += ctf;
		}
		
		bw.close();
		invRaf.close();
		
		//Write corpas info into: corpas_info file
		//NUM_DOCS = 84678; NUM_TERMS = 41802513; NUM_UNIQUE_TERMS =207615; AVE_DOCLEN = 493;
		bw = new BufferedWriter(new FileWriter
				(IndexConstants.ROOT_FOLDER+"/corpas_info"));
		String toWriteToCorpas = new StringBuffer("NUM_DOCS = ").append(doc_doc_id_map.size())
				.append("; NUM_TERMS = ").append(total_no_of_words).append("; NUM_UNIQUE_TERMS =")
				.append(term_term_id_map.size()).append("; AVE_DOCLEN = ").append(total_no_of_words/doc_doc_id_map.size()).toString();
		bw.write(toWriteToCorpas);
		bw.close();
		
		
		
		System.out.println("\nTotal time taken: "+(new Date().getTime() - startTime)/1000+" sec");
		
	}

	

}
