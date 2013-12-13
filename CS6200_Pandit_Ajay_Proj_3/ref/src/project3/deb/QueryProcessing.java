package project3.deb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class QueryProcessing {

	public static void main(String[] args) throws IOException{
		
		long startTime = new Date().getTime();
		
		ArrayList<Query> queries = getQueries(IndexConstants.ROOT_FOLDER+"cacm.query");
		/*for(Query q : queries){
			System.out.println(q.toString());
		}*/
		
		int total_query_len = 0;
		for(Query q : queries){
			total_query_len += q.getQuery_len();
		}

		for(Query q : queries){
			q.setAvg_len(total_query_len / queries.size());
		}	
		
		try{
			RankingModelUtil.generateOkapiTf(queries, "OkapiTF.out");
			RankingModelUtil.generateOkapiIdfTf(queries, "OkapiTf_IDF.out");
			RankingModelUtil.generateLaplaceSmoothing(queries, "Laplace.out");
			RankingModelUtil.generateJelinekSmoothing(queries, "Jelinek.out");
			RankingModelUtil.generateBM25(queries, "BM25.out");	
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("\nTotal time taken: "+(new Date().getTime() - startTime)/1000+" sec");
	}
	
	private static ArrayList<Query> getQueries(String string) throws IOException {
		ArrayList<Query> queries = new ArrayList<Query>();
		//Process Query
		File in = new File(string);
		Document doc = Jsoup.parse(in , "UTF-8");

		Elements elements = doc.select("DOC");
		for(Element el : elements){
			String q_id = el.select("DOCNO").get(0).ownText();
			String q_text = el.ownText();
			
			ArrayList<String> raw_terms = MyUtil.getTerms(q_text);
			HashMap<String, Integer> terms = new HashMap<String, Integer>(); 
			int q_len = 0;
			for(String st : raw_terms){
				if(MyUtil.isEmpty(st))
					continue;
				if(terms.containsKey(st)){
					int val = terms.remove(st);
					terms.put(st, val + 1);
				}else{
					terms.put(st, 1);
				}
				q_len++;
			}

			Query q = new Query(q_id, terms);
			q.setQuery_len(q_len);
			queries.add(q);
			
		}
		return queries;
	}
	
}
