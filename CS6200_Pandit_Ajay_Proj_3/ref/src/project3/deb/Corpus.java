package project3.deb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Corpus {
	
	private HashMap<String, Term> termsMap;
	private HashMap<String, Document> docsMap;   //document_id and document map
	
	public Corpus(){
		termsMap = new HashMap<String, Term>();
		docsMap = new HashMap<String, Document>();
	}
	
	public synchronized void updateCorpusAfterProcessingFile(String doc_id, ArrayList<String> terms){
		Document d = new Document(doc_id, terms.size());
		docsMap.put(doc_id, d);
		
		for(String term : terms){
			Term t = termsMap.remove(term);
			t = (t == null) ? new Term(term) : t;
			t.addCount();
			t.addDocument(doc_id);
			termsMap.put(term, t);
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer("Terms:\n");
		for(Term t : termsMap.values()){
			sb.append(t.toString()+"\n");
		}
		sb.append("=========================");
		sb.append("\nDocuments:\n");
		for(Document t : docsMap.values()){
			sb.append(t.toString()+"\n");
		}
		
		return sb.toString();
	}
	
	public Term getTerm(String t){
		return this.termsMap.get(t);
	}
	
	public Document getDocument(String d){
		return this.docsMap.get(d);
	}

}
