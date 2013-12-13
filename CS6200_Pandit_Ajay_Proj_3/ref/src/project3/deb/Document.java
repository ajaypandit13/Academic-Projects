package project3.deb;


public class Document {
	
	String doc_id;
	int doc_len;
	public String getDoc_id() {
		return doc_id;
	}
	public void setDoc_id(String doc_id) {
		this.doc_id = doc_id;
	}
	public int getDoc_len() {
		return doc_len;
	}
	public void setDoc_len(int doc_len) {
		this.doc_len = doc_len;
	}

	public Document(String doc_id, int len){
		this.doc_id = doc_id;
		this.doc_len = len;
	}
	
	public String toString(){
		return doc_id +" len: "+doc_len;
	}
}
