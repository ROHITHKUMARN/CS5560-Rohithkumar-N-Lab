import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.*;
import java.util.*;


public class Main {

    public static Map<String, TreeSet<String>> hmap = new HashMap<>();
    public static void answerQA(){
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties prop = new Properties();
        prop.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP mypipeline = new StanfordCoreNLP(prop);
        String ne;
        String everything = " ";
        Set<String> ners = new TreeSet<>();
        List<CoreLabel> values = new ArrayList<>();
        try (BufferedReader br = new BufferedReader
                (new FileReader
                        ("/Users/satheeshchandra/Desktop/UMKC.docx"))) {
            StringBuilder sbuilder = new StringBuilder();
            String l = br.readLine();
            while (l != null) {
                sbuilder.append(l);
                sbuilder.append(System.lineSeparator());
                l = br.readLine();
            }
            everything = sbuilder.toString();
        } catch (IOException e1) {
            System.out.println(e1);
        }
        Annotation d = new Annotation(everything);
        mypipeline.annotate(d);
        List<CoreMap> sentenceslist = d.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentenceslist) {
            for (CoreLabel tokens : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                ne = tokens.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (ne.equals("O")) {
                } else {
                    ners.add(ne);
                }

            }

        }
        System.out.println(ners);
        for (CoreMap sentence : sentenceslist) {

            for (CoreLabel tokenlist : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String x = tokenlist.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (ners.contains(x)) {
                    if (hmap.get(x) == null) {
                        //System.out.println(token);
                        TreeSet<String> s = new TreeSet<>();
                        s.add(tokenlist.originalText());
                        hmap.put(x,s);
                    } else {
                        hmap.get(x).add(tokenlist.originalText());
                    }

                }

            }
        }
        //System.out.println(hmap);
    }
    public static void askQA(){
        answerQA();
        while(true) {
            System.out.println("Enter 1 to ask questions" + "\n" +
                    "Enter 0 to exit");
            Scanner input = new Scanner(System.in);
            String val = input.nextLine();
            switch (val) {
                case "1": {
                    System.out.println("Please enter Question....!");
                    String readLine = input.nextLine();
                    readLine = readLine.toLowerCase();
                    if (readLine.contains("location")) {
                        System.out.println(hmap.get("LOCATION"));
                    } else if (readLine.contains("organization")) {
                        System.out.println(hmap.get("ORGANIZATION"));
                    } else if (readLine.contains("date")) {
                        System.out.println(hmap.get("DATE"));
                    } else if (readLine.contains("money")) {
                        System.out.println(hmap.get("MONEY"));
                    } else if (readLine.contains("person")) {
                        System.out.println(hmap.get("PERSON"));
                    } else if (readLine.contains("time")) {
                        System.out.println(hmap.get("TIME"));

                    }
                    break;
                }

                case "0":{
                    System.exit(0);
                break;
                }
                default:{
                        System.exit(0);
                }
            }
        }
}
    public static void main(String args[]) {
        askQA();
    }

}

/*
Enter 1 to ask questions
Enter 0 to exit
1
Please enter Question....!
what location are mentioned in data
[San, Los, New, Francisco, Chicago, Atlanta, York, CHICAGO, Denver, Angeles, Dallas]
Enter 1 to ask questions
Enter 0 to exit
1
Please enter Question....!
when is the price change effected date
[Thursday, Friday]
Enter 1 to ask questions
Enter 0 to exit
1
Please enter Question....!
who is spokes person of united airlines
[Wagner, Tim]
 */
