package project3.deb;

import java.util.HashMap;


public class Term {
	
	String value;
	int ctf;		//number of times the term occurs in corpus
	//int no_docs;	//number of documents the term is contained
	HashMap<String, Integer> term_doc_map; //Maps the document_id and the number of times it has the term
	
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getCtf() {
		return ctf;
	}
	public void setCtf(int ctf) {
		this.ctf = ctf;
	}
	/*public int getNo_docs() {
		return no_docs;
	}
	public void setNo_docs(int no_docs) {
		this.no_docs = no_docs;
	}*/
	public HashMap<String, Integer> getTerm_doc_map() {
		return term_doc_map;
	}
	public void setTerm_doc_map(HashMap<String, Integer> term_doc_map) {
		this.term_doc_map = term_doc_map;
	}
	
	public Term(String t){
		this.value = t;
		this.ctf = 0;	
		this.term_doc_map = new HashMap<String, Integer>();
	}
	
	public void addCount(){
		++this.ctf;
	}
	
	public void addDocument(String doc_id){
		if(term_doc_map.containsKey(doc_id)){
			int dtf = term_doc_map.remove(doc_id);
			term_doc_map.put(doc_id, ++dtf);
		}else{
			term_doc_map.put(doc_id, 1);
		}
		
	}
	
	public String toString(){
		return value +" : "+ ctf;
	}

}
