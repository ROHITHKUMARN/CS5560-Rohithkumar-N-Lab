import java.io.Serializable;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.spark.api.java.JavaRDD;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * Created by rohithkumar on 6/20/17.
 */
public class MyJavaFunction implements Serializable {
    public String getlemmas(String s) {
        Properties myprop = new Properties();
        myprop.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP mypl = new StanfordCoreNLP(myprop);
        String line = "";
        Annotation d = new Annotation(s);
        mypl.annotate(d);
        List<CoreMap> st = d.get(CoreAnnotations.SentencesAnnotation.class);
        List l1 = new ArrayList();
        String lm = "";
        for (CoreMap dst : st) {
            for (CoreLabel word : dst.get(CoreAnnotations.TokensAnnotation.class)) {
                lm += word.get(CoreAnnotations.LemmaAnnotation.class);
                lm += " ";
            }
        }
        // System.out.println(lemma);
        return lm;
    }
}