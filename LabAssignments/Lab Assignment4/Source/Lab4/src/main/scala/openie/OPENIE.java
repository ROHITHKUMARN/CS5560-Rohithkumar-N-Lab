
package openie;

        import edu.stanford.nlp.simple.Document;
        import edu.stanford.nlp.simple.Sentence;
        import edu.stanford.nlp.util.Quadruple;
        import java.util.Collection;

public class OPENIE
{
    public static String returnTriplets(String s) {

        String triples = "";
        Document doc1 = new Document(s);
        for (Sentence sen1 : doc1.sentences()) {
            triples += sen1.openie();
        }

        return triples;
    }

}
