package project3.deb;

import java.util.HashMap;
import java.util.Map;

class Query{
	String query_number;
	HashMap<String, Integer> terms = null;
	//ArrayList<Term> terms = null;
	double avg_len = 0.0;
	int query_len = 0;
	public String getQuery_number() {
		return query_number;
	}
	public void setQuery_number(String query_number) {
		this.query_number = query_number;
	}
	public HashMap<String, Integer> getTerms() {
		return terms;
	}
	public void setTerms(HashMap<String, Integer> terms) {
		this.terms = terms;
	}
	public double getAvg_len() {
		return avg_len;
	}
	public void setAvg_len(double avg_len) {
		this.avg_len = avg_len;
	}	
	public int getQuery_len() {
		return query_len;
	}
	public void setQuery_len(int query_len) {
		this.query_len = query_len;
	}
	public Query(String query_number, HashMap<String, Integer> terms) {
		this.query_number = query_number;
		this.terms = terms;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer(query_number);
		for(Map.Entry<String, Integer> entry : terms.entrySet()){
			if(entry.getValue() > 1){
				sb.append(" ").append(entry.getKey()).append("(").append(entry.getValue()).append(")");
			}else{
				sb.append(" ").append(entry.getKey());
			}
		}
		return sb.toString();
	}
}