package project3.deb;

import java.util.Comparator;
import java.util.Map;

/**
 * This class is used for Sorting a Map<String, Double> by the descending value
 * of the Map.
 * @author Arijit Deb
 *
 */
class ValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }
    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}