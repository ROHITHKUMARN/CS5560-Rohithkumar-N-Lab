package OpenIE;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

/**
 * Created by rohithkumar on 7/16/17.
 */
public class TripletExtraction {

    public static String returnTriplets(String s) {
        String triples = "";
        Document doc1 = new Document(s);
        for (Sentence sen1 : doc1.sentences()) {
            triples += sen1.openie();
        }
        return triples;
    }
}
