package project3.deb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class RankingModelUtil {

	static HashMap<String, String> termDocumentMap = new HashMap<String, String>();
	static HashMap<String, String> docIdMap = getDocIdMap();
	
	//D3 database constants
	//NUM_DOCS = 3204; NUM_TERMS = 152235; NUM_UNIQUE_TERMS =12040; AVE_DOCLEN = 47
	//NUM_DOCS = 3204; NUM_TERMS = 222997; NUM_UNIQUE_TERMS =12272; AVE_DOCLEN = 69
	//static final String mainURL = "http://fiji4.ccs.neu.edu/~zerg/lemurcgi/lemur.cgi?g=p&d=3&v=";
	static final int total_no_documents = 3204;
	static final long total_no_of_words = 222997; //total number of word occurrences in the collection
	static final double no_of_unique_terms = 12272; //no of unique terms for d3 database
	static final int avg_doc_len = 69;
	

	public static void main(String[] args){
		System.out.println(getBodyContent("hull"));
	}

	public static HashMap<String, Double> updateDocsWithOkapiTf(String term, double term_tf, int avg_doc_len, HashMap<String, Double> docs){

		String body = getBodyContent(term);
		if(isEmpty(body)){
			//System.out.println("Problem with term: "+term +" body: "+body);
			return docs;
		}

		String[] bodyTokens = body.split(" ");

		for(int i=2; i<bodyTokens.length;i++){
			//System.out.println(bodyTokens[i]);
			try{
				if(i % 3 == 2){
					String doc_id = bodyTokens[i];
					int doc_len = Integer.parseInt(bodyTokens[i+1]);
					int tf = Integer.parseInt(bodyTokens[i+2]);

					double okapi_tf = (double) tf / (tf + 0.5 + 1.5 * doc_len / avg_doc_len);
					okapi_tf *= term_tf;

					if(docs.get(doc_id) == null){
						docs.put(doc_id, okapi_tf);
					}else{
						double current_tf = docs.remove(doc_id);
						docs.put(doc_id, (current_tf + okapi_tf));
					}
				}
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}

		return docs;
	}

	/**
	 * 
	 * @param term
	 * @param term_tf
	 * @param avg_doc_len
	 * @param docs
	 * @return
	 */
	public static HashMap<String, Double> updateDocsWithOkapiIDFTf(String term, double term_tf, 
			int avg_doc_len, HashMap<String, Double> docs){

		String body = getBodyContent(term);
		if(isEmpty(body)){
			//System.out.println("Problem with: term"+term +" body: "+body);
			return docs;
		}

		String[] bodyTokens = body.split(" ");

		
		int nt = Integer.parseInt(bodyTokens[1]);

		double idf = Math.log((total_no_documents / nt)) / Math.log(2);

		for(int i=2; i<bodyTokens.length;i++){
			//System.out.println(bodyTokens[i]);
			try{
				if(i % 3 == 2){
					String doc_id = bodyTokens[i];
					int doc_len = Integer.parseInt(bodyTokens[i+1]);
					int tf = Integer.parseInt(bodyTokens[i+2]);

					double okapi_tf = (double) tf / (tf + 0.5 + 1.5 * doc_len / avg_doc_len);
					okapi_tf *= idf;
					okapi_tf *= term_tf;

					if(docs.get(doc_id) == null){
						docs.put(doc_id, okapi_tf);
					}else{
						double current_tf = docs.remove(doc_id);
						docs.put(doc_id, (current_tf + okapi_tf));
					}
				}
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}

		return docs;
	}
	
	public static HashMap<String, Double> updateDocsWithLaplaceSmoothing(HashMap<String, Integer> terms){
		
		HashMap<String, Double> retDocs = new HashMap<String, Double>();
		//final double no_of_unique_terms = 166054; //no of unique terms for d3 database
		final int total_terms = terms.size();
		HashMap<String, MyDocument> docMap = new HashMap<String, MyDocument>();
		for(Map.Entry<String, Integer> entry : terms.entrySet()){
			String term = entry.getKey();
			String body = getBodyContent(term);
			if(isEmpty(body)){
				//System.out.println("Problem with: term"+term +" body: "+body);
				continue;
			}
			
			String[] bodyTokens = body.split(" ");

			for(int i=2; i<bodyTokens.length;i++){
				//System.out.println(bodyTokens[i]);
				try{
					if(i % 3 == 2){
						String doc_id = bodyTokens[i];
						int doc_len = Integer.parseInt(bodyTokens[i+1]);
						int tf = Integer.parseInt(bodyTokens[i+2]);
						
						double weight = Math.log((tf + 1) / (doc_len + no_of_unique_terms));
						
						if(docMap.get(doc_id) == null){
							MyDocument myDoc = new MyDocument(doc_id, doc_len, weight);							
							docMap.put(doc_id, myDoc);
						}else{
							MyDocument myDoc = docMap.remove(doc_id);
							myDoc.addWeight(weight);
							myDoc.addTermCount();
							docMap.put(doc_id, myDoc);
						}
					}
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
				}
			}
		}
		
		for(Map.Entry<String, MyDocument> entry : docMap.entrySet()){
			MyDocument myDoc = entry.getValue();
			int no_of_terms_not_present = total_terms - myDoc.getNo_of_term_present();
			double left_over_weight = no_of_terms_not_present * 
					Math.log((1 / (myDoc.getDoc_len() + no_of_unique_terms)));
			myDoc.addWeight(left_over_weight);
			
			retDocs.put(myDoc.getDocument_id(), myDoc.getWeight());
		}
		
		return retDocs;
	}

	public static HashMap<String, Double> updateDocsWithJelinekSmoothing(HashMap<String, Integer> terms){
		
		HashMap<String, Double> retDocs = new HashMap<String, Double>();
		//final long total_no_of_words = 24401877; //total number of word occurrences in the collection
		final int total_terms = terms.size();
		final double lambda = 0.2;
		HashMap<String, MyDocument> docMap = new HashMap<String, MyDocument>();
		HashMap<String, Integer> termCtfMap = new HashMap<String, Integer>();
		
		
		for(Map.Entry<String, Integer> entry : terms.entrySet()){
			String term = entry.getKey();
			String body = getBodyContent(term);
			if(isEmpty(body)){
				//System.out.println("Problem with: term"+term +" body: "+body);
				continue;
			}
			
			String[] bodyTokens = body.split(" ");
			
			int ctf = Integer.parseInt(bodyTokens[0]);
			if(ctf != 0){
				termCtfMap.put(term, ctf);
			}

			for(int i=2; i<bodyTokens.length;i++){
				//System.out.println(bodyTokens[i]);
				try{
					if(i % 3 == 2){
						String doc_id = bodyTokens[i];
						int doc_len = Integer.parseInt(bodyTokens[i+1]);
						int tf = Integer.parseInt(bodyTokens[i+2]);
						
						double weight = Math.log(lambda * ctf / total_no_of_words + (1 - lambda) * tf / doc_len);
						
						if(docMap.get(doc_id) == null){
							MyDocument myDoc = new MyDocument(doc_id, doc_len, weight);
							myDoc.addNewTerm(term);
							docMap.put(doc_id, myDoc);
						}else{
							MyDocument myDoc = docMap.remove(doc_id);
							myDoc.addWeight(weight);
							myDoc.addNewTerm(term);
							myDoc.addTermCount();
							docMap.put(doc_id, myDoc);
						}
					}
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
				}
			}
		}
		
		for(Map.Entry<String, MyDocument> entry : docMap.entrySet()){
			MyDocument myDoc = entry.getValue();
			int no_of_terms_not_present = total_terms - myDoc.getNo_of_term_present();
			if(no_of_terms_not_present != 0){
				ArrayList<String> presentTerm = myDoc.getPresentTerms();
				for(Map.Entry<String, Integer> termCtfMapEntry : termCtfMap.entrySet()){
					String term = termCtfMapEntry.getKey();
					if(!presentTerm.contains(term)){
						//System.out.println(presentTerm +" "+term);
						double left_over_weight = Math.log(lambda * termCtfMapEntry.getValue() / total_no_of_words);
						/*try{
							BigDecimal bd = new BigDecimal(Double.toString(left_over_weight));
						}catch(NumberFormatException e){
							System.out.println(presentTerm +" "+term+" "+left_over_weight+termCtfMapEntry.getValue());
						}*/
						myDoc.addWeight(left_over_weight);
					}
				}
			}
			retDocs.put(myDoc.getDocument_id(), myDoc.getWeight());
		}
		
		return retDocs;
	}

	/*public static HashMap<String, Double> updateDocsWithJelinekSmoothing(String term,  HashMap<String, Double> docs){

		final long total_no_of_words = 24401877; //total number of word occurrences in the collection
		final double lambda = 0.2;

		String body = getBodyContent(term);
		if(isEmpty(body)){
			System.out.println("Problem with: term"+term +" body: "+body);
			return docs;
		}

		String[] bodyTokens = body.split(" ");

		int ctf = Integer.parseInt(bodyTokens[0]);

		for(int i=2; i<bodyTokens.length;i++){
			//System.out.println(bodyTokens[i]);
			try{
				if(i % 3 == 2){
					String doc_id = bodyTokens[i];
					int doc_len = Integer.parseInt(bodyTokens[i+1]);
					int tf = Integer.parseInt(bodyTokens[i+2]);

					double j_m_value = lambda * ctf / total_no_of_words + (1 - lambda) * tf / doc_len;
					j_m_value = Math.log(j_m_value) / Math.log(2);

					if(docs.get(doc_id) == null){
						docs.put(doc_id, j_m_value);
					}else{
						double current_tf = docs.remove(doc_id);
						docs.put(doc_id, (current_tf + j_m_value));
					}
				}
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}

		return docs;
	}*/


	public static HashMap<String, Double> updateDocsWithBM25(String term, int qf, HashMap<String, Double> docs){

		//final int N = 84678; //No of documents
		final double k1 = 1.2;
		final double b = 0.75;
		final int k2 = 100;
		//final int avg_doc_len = 288;

		final int R = 0;
		final int r_i = 0;

		String body = getBodyContent(term);
		if(isEmpty(body)){
			//System.out.println("Problem with: term"+term +" body: "+body);
			return docs;
		}

		String[] bodyTokens = body.split(" ");
		if(bodyTokens == null || bodyTokens.length == 0){
			return docs;
		}

		int n_i = Integer.parseInt(bodyTokens[1]); //no of docs where the term occurs

		for(int i=2; i<bodyTokens.length;i++){
			//System.out.println(bodyTokens[i]);
			try{
				if(i % 3 == 2){
					String doc_id = bodyTokens[i];
					int doc_len = Integer.parseInt(bodyTokens[i+1]);
					int tf = Integer.parseInt(bodyTokens[i+2]);

					double K = k1 * ((1 -b) + b * doc_len / avg_doc_len);

					double bm_25 = (((r_i + 0.5) / (R - r_i + 0.5)) / ((n_i - r_i + 0.5) / (total_no_documents - n_i - R + r_i + 0.5)));
					bm_25 = Math.log(bm_25) / Math.log(2);
					bm_25 *= (((k1 + 1) * tf) / (K + tf)) *
							 (((k2 + 1) * qf) / (k2 + qf));			

					if(docs.get(doc_id) == null){
						docs.put(doc_id, bm_25);
					}else{
						double current_tf = docs.remove(doc_id);
						docs.put(doc_id, (current_tf + bm_25));
					}
				}
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}

		return docs;
	}


	public static HashMap<String, String> getDocIdMap(){
		HashMap<String, String> retMap = new HashMap<String, String>();
		//Path path = Paths.get("doclist.txt");
		Scanner scanner;
		try {
			scanner = new Scanner(new BufferedReader
					(new FileReader("/Users/arijitdeb/Documents/IR/Project 3/doc_id_map")));
			while(scanner.hasNextLine()){
				String st = scanner.nextLine();
				retMap.put(st.substring(0, st.indexOf(" ")), 
						st.substring(st.indexOf(" ") + 1));

			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return retMap;
	}

	/*public static void main(String[] args) throws IOException{

		System.out.println((double)(1 +1) / (278 +  + 166054));

	}*/


	public static void generateOkapiTf(ArrayList<Query> queries, String output_file) throws IOException{

		//final int avg_doc_len = 288;
		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));

		for(Query q : queries){			
			HashMap<String, Double> docs = new HashMap<String, Double>();
			HashMap<String, Integer> terms = q.getTerms();
			for(Map.Entry<String, Integer> entry : terms.entrySet()){
				try {
					docs = updateDocsWithOkapiTf(entry.getKey(), 
							calculateTermOkapi(entry.getValue(), q.getQuery_len(), q.getAvg_len()),
							avg_doc_len, docs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ValueComparator bvc = new ValueComparator(docs);
			TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
			sorted_map.putAll(docs);			

			int i = 1;
			for(Map.Entry<String, Double> s : sorted_map.entrySet()){
				bw.write(q.getQuery_number()+" 0 "+s.getKey()+" "+i+" "+
						(new BigDecimal(Double.toString(s.getValue()))).toPlainString()+" 1");
				bw.newLine();
				if(++i == 1001)
					break;
			}			
		}
		bw.close();		
	}

	public static void generateOkapiIdfTf(ArrayList<Query> queries, String output_file) throws IOException{

		//final int avg_doc_len = 288;
		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));

		for(Query q : queries){			
			HashMap<String, Double> docs = new HashMap<String, Double>();
			HashMap<String, Integer> terms = q.getTerms();
			for(Map.Entry<String, Integer> entry : terms.entrySet()){
				try {
					docs = updateDocsWithOkapiIDFTf(entry.getKey(), 
							calculateTermOkapiIDF(entry.getValue(), q.getQuery_len(), q.getAvg_len()),
							avg_doc_len, docs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ValueComparator bvc = new ValueComparator(docs);
			TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
			sorted_map.putAll(docs);			

			int i = 1;
			for(Map.Entry<String, Double> s : sorted_map.entrySet()){
				bw.write(q.getQuery_number()+" 0 "+s.getKey()+" "+i+" "+
						(new BigDecimal(Double.toString(s.getValue()))).toPlainString()+" 1");
				bw.newLine();
				if(++i == 1001)
					break;
			}			
		}
		bw.close();		
	}

	public static void generateLaplaceSmoothing(ArrayList<Query> queries, String output_file) throws IOException{

		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));

		for(Query q : queries){			
			//HashMap<String, Double> docs = new HashMap<String, Double>();
			HashMap<String, Integer> terms = q.getTerms();
			/*for(Map.Entry<String, Integer> entry : terms.entrySet()){
				try {
					docs = updateDocsWithLaplaceSmoothing(entry.getKey(), entry.getValue(), docs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
			
			HashMap<String, Double> docs = updateDocsWithLaplaceSmoothing(terms);
			
			ValueComparator bvc = new ValueComparator(docs);
			TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
			sorted_map.putAll(docs);			

			int i = 1;
			for(Map.Entry<String, Double> s : sorted_map.entrySet()){
				bw.write(q.getQuery_number()+" 0 "+s.getKey()+" "+i+" "+
						(new BigDecimal(Double.toString(s.getValue()))).toPlainString()+" 1");
				bw.newLine();
				if(++i == 1001)
					break;
			}			
		}
		bw.close();		
	}

	public static void generateJelinekSmoothing(ArrayList<Query> queries, String output_file) throws IOException{

		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));

		for(Query q : queries){			
			HashMap<String, Double> docs = new HashMap<String, Double>();
			HashMap<String, Integer> terms = q.getTerms();
			/*for(Map.Entry<String, Integer> entry : terms.entrySet()){
				try {
					//docs = updateDocsWithJelinekSmoothing(entry.getKey(), docs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
			docs = updateDocsWithJelinekSmoothing(terms);

			ValueComparator bvc = new ValueComparator(docs);
			TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
			sorted_map.putAll(docs);			

			int i = 1;
			for(Map.Entry<String, Double> s : sorted_map.entrySet()){
				try{
					bw.write(q.getQuery_number()+" 0 "+s.getKey()+" "+i+" "+
							(new BigDecimal(Double.toString(s.getValue()))).toPlainString()+" 1");
					bw.newLine();
					if(++i == 1001)
						break;
				}catch(NumberFormatException e){
					//e.printStackTrace();
					System.out.println("Error for: "+s);
				}
			}			
		}
		bw.close();		
	}

	public static void generateBM25(ArrayList<Query> queries, String output_file) throws IOException{

		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));

		for(Query q : queries){			
			HashMap<String, Double> docs = new HashMap<String, Double>();
			HashMap<String, Integer> terms = q.getTerms();
			for(Map.Entry<String, Integer> entry : terms.entrySet()){
				try {
					docs = updateDocsWithBM25(entry.getKey(), entry.getValue(), docs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ValueComparator bvc = new ValueComparator(docs);
			TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
			sorted_map.putAll(docs);			

			int i = 1;
			for(Map.Entry<String, Double> s : sorted_map.entrySet()){
				bw.write(q.getQuery_number()+" 0 "+s.getKey()+" "+i+" "+
						(new BigDecimal(Double.toString(s.getValue()))).toPlainString()+" 1");
				bw.newLine();
				if(++i == 1001)
					break;
			}			
		}
		bw.close();		
	}

	private static double calculateTermOkapi(int orig_tf, double doc_len, double avg_len){
		return (double) (orig_tf / (orig_tf + 0.5 + 1.5 * doc_len / avg_len));
	}

	private static double calculateTermOkapiIDF(int orig_tf, double doc_len, double avg_len){
		double q_idf = Math.log((25 / doc_len)) / Math.log(2);
		return (double) q_idf * (orig_tf / (orig_tf + 0.5 + 1.5 * doc_len / avg_len));
	}

	private static String getBodyContent(String term){
		
		/*Term t = cor.getTerm(term);
		if(t == null)
			return null;
		
		HashMap<String, Integer> docMap = t.getTerm_doc_map();
		
		StringBuffer sb = new StringBuffer();
		sb.append(t.getCtf()).append(" ").append(docMap.size());
		
		for(Map.Entry<String, Integer> entry : docMap.entrySet()){
			project3.deb.Document d = cor.getDocument(entry.getKey());
			sb.append(" ").append(d.getDoc_id()).append(" ").append(d.getDoc_len()).append(" ")
			.append(entry.getValue());
			
		}*/
		
		byte[] output = null;
		try{
			RandomAccessFile invRaf = new RandomAccessFile(new File("/Users/arijitdeb/Documents/IR/Project 3/inverted_index"), "r");
			Scanner sc = new Scanner(new FileReader("/Users/arijitdeb/Documents/IR/Project 3/term_info"));

			while(sc.hasNext()){
				String line = sc.nextLine();
				String[] tokens = line.split(" ");
				if(term.equals(tokens[0])){
					long offset = Long.parseLong(tokens[2]);
					int length = Integer.parseInt(tokens[3]);
					output = new byte[length];

					invRaf.seek(offset);
					invRaf.read(output);	
					break;
				}
			}
			sc.close();
			invRaf.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(output == null){
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		if(output != null){			
			for(int i=0;i<output.length;i++){
				sb.append((char)output[i]);
			}
		}
		//If the term is found, then at this point, the string should look like:
		// term_id ctf df doc_id1 tf1 delta_doc_id2 tf2 ... delta_doc_idn tfn
		// Convert the doc_id/delta_doc_id to the actual document name and also 
		// add the doc_len info
		String[] tokens = sb.toString().split(" ");
		sb = new StringBuffer(tokens[1]).append(" ").append(tokens[2]);
		long last_doc_id = 0;
		//int df = Integer.parseInt(tokens[2]);
		for(int i=3;i<tokens.length;){
			long cur_doc_id = last_doc_id + Long.parseLong(tokens[i]);
			last_doc_id = cur_doc_id;
			sb.append(" ").append(docIdMap.get(String.valueOf(cur_doc_id)))
			.append(" ").append(tokens[++i]);
			i++;			
		}
		
		
		return sb.toString();
	}
	
	private static String getTermBodyFromFile(String term){
		//Path path = Paths.get(term+".txt");
		String content = null;
		Scanner scanner = null;
		try {
			scanner = new Scanner(new BufferedReader(new FileReader(term+".txt")));
			if(scanner.hasNextLine()){
				content = scanner.nextLine();
			}
		}catch(IOException e){
			return null;
		}finally{
			if(scanner != null){
				scanner.close();
			}
		}
		return content;
	}
	
	public static boolean isEmpty(String st){
		return (st == null || "".equals(st.trim()));
	}

}
