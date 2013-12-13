package project3.deb;

import java.util.ArrayList;


public class MyDocument {
	
	String document_id;
	int no_of_term_present;
	double weight;
	int doc_len;
	ArrayList<String> presentTerms = null;
	
	public String getDocument_id() {
		return document_id;
	}
	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}
	public int getNo_of_term_present() {
		return no_of_term_present;
	}
	public void setNo_of_term_present(int no_of_term_present) {
		this.no_of_term_present = no_of_term_present;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public int getDoc_len() {
		return doc_len;
	}
	public void setDoc_len(int doc_len) {
		this.doc_len = doc_len;
	}
	public MyDocument(String doc_id, int doc_len, double w) {
		this.document_id = doc_id;
		this.doc_len = doc_len;
		this.weight = w;
		this.no_of_term_present = 1;
		presentTerms = new ArrayList<String>();
	}
	public void addTermCount(){
		this.no_of_term_present++;
	}
	
	public void addWeight(double w){
		this.weight += w;
	}
	
	public void addNewTerm(String s){
		this.presentTerms.add(s);
	}
	public ArrayList<String> getPresentTerms() {
		return presentTerms;
	}
	public void setPresentTerms(ArrayList<String> presentTerms) {
		this.presentTerms = presentTerms;
	}
	

}
