import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.List;


public class CoreNLP {

    public static String returnLemma(String sentence) {

        Document document = new Document(sentence);

        String lemma="";

        for (Sentence sent : document.sentences())
        {
            List<String> lstring = sent.lemmas();
            for (int i = 0; i < lstring.size() ; i++) {
                lemma+= lstring.get(i) +" ";
            }
            //System.out.println(lemma);
        }
        return lemma;
    }
    public static  void main(String args[]){
        String s="South Africa won the series";
        System.out.println("Original string ="+s);
        System.out.println("Lemmatized form="+returnLemma(s));
    }
}
