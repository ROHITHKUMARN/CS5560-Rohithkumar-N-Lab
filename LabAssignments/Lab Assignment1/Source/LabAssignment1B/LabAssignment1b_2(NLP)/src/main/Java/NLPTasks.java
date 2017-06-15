import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by rohithkumar on 6/13/17.
 */
public class NLPTasks {

    public static void main(String args[]) {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties ps = new Properties();
        int mSentiment=0;
        int  longest=0;
        String sentimentscore;
        String text="";
        ps.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref,sentiment");
        StanfordCoreNLP pline = new StanfordCoreNLP(ps);
        try (BufferedReader breader = new BufferedReader
                (new FileReader
                        ("src/main/datasource/datafile"))) {
            StringBuilder stringb = new StringBuilder();
            String line1 = breader.readLine();
            while (line1 != null) {
                stringb.append(line1);
                stringb.append(System.lineSeparator());
                line1 = breader.readLine();
            }
            text = stringb.toString();
        } catch (IOException e) {
            System.out.println(e);
        }

        // read some text in the text variable
        // Add your text here!

// create an empty Annotation just with the given text
        Annotation dt = new Annotation(text);

// run all Annotators on this text
        pline.annotate(dt);

        // these are all the sentences in this document
// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = dt.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                String word = token.get(CoreAnnotations.TextAnnotation.class);


                 String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                //System.out.println("Parts Of Speech Tagger");
                 System.out.print("Part of speech of "+token.originalText() + ":" + pos+"\t\t");

                // this is the NER label of the token
                 String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
               // System.out.println("Named Entity Recogniser");
                System.out.print("Named Entity of "+ token.originalText() + ":" + ne+"\t");

                System.out.println("\n");
            }

            System.out.println("Coreferecing system");
            Map<Integer, CorefChain> graph =
                    dt.get(CorefCoreAnnotations.CorefChainAnnotation.class);
            System.out.println(graph.values().toString());


        }
        Annotation annotation = pline.process(text);
        for (CoreMap sentence1 : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            //System.out.println(sentence1);
            Tree tree = sentence1.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            //System.out.println(tree);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            String partText = sentence1.toString();
            if (partText.length() > longest) {
                mSentiment = sentiment;
                longest = partText.length();
            }

        }
        if (mSentiment == 2 || mSentiment > 4 || mSentiment < 0) {
            sentimentscore= "-1";
        }
        switch (mSentiment) {
            case 0:
                sentimentscore="very negative";
                break;
            case 1:
                sentimentscore="negative";
                break;
            case 2:
                sentimentscore="neutral";
                break;
            case 3:
                sentimentscore="positive";
                break;
            case 4:
                sentimentscore="very positive";
                break;
            default:
                sentimentscore= "cannot determine";
                break;
        }
        System.out.println("sentiment score of this data is:"+sentimentscore);

    }
}
/*

Output:

Part of speech of what:WP		Named Entity of what:O

Part of speech of do:VBP		Named Entity of do:O

Part of speech of u:FW		Named Entity of u:O

Part of speech of mean:VB		Named Entity of mean:O

Part of speech of by:IN		Named Entity of by:O

Part of speech of filing:VBG		Named Entity of filing:O

Part of speech of return:NN		Named Entity of return:O

Part of speech of with:IN		Named Entity of with:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of income:NN		Named Entity of income:O

Part of speech of tax:NN		Named Entity of tax:O

Part of speech of deptt:NN		Named Entity of deptt:O

Part of speech of i:FW		Named Entity of i:O

Part of speech of am:VBP		Named Entity of am:O

Part of speech of salaried:JJ		Named Entity of salaried:O

Part of speech of person:NN		Named Entity of person:O

Part of speech of also:RB		Named Entity of also:O

Part of speech of i:FW		Named Entity of i:O

Part of speech of am:VBP		Named Entity of am:O

Part of speech of a:DT		Named Entity of a:O

Part of speech of partner:NN		Named Entity of partner:O

Part of speech of in:IN		Named Entity of in:O

Part of speech of one:CD		Named Entity of one:NUMBER

Part of speech of firm:NN		Named Entity of firm:O

Part of speech of under:IN		Named Entity of under:O

Part of speech of which:WDT		Named Entity of which:O

Part of speech of my:PRP$		Named Entity of my:O

Part of speech of father:NN		Named Entity of father:O

Part of speech of does:VBZ		Named Entity of does:O

Part of speech of business:NN		Named Entity of business:O

Part of speech of my:PRP$		Named Entity of my:O

Part of speech of salary:NN		Named Entity of salary:O

Part of speech of is:VBZ		Named Entity of is:O

Part of speech of deducted:VBN		Named Entity of deducted:O

Part of speech of and:CC		Named Entity of and:O

Part of speech of paid:VBN		Named Entity of paid:O

Part of speech of to:TO		Named Entity of to:O

Part of speech of me:PRP		Named Entity of me:O

Part of speech of however:RB		Named Entity of however:O

Part of speech of if:IN		Named Entity of if:O

Part of speech of i:FW		Named Entity of i:O

Part of speech of do:VBP		Named Entity of do:O

Part of speech of not:RB		Named Entity of not:O

Part of speech of consolidate:VB		Named Entity of consolidate:O

Part of speech of my:PRP$		Named Entity of my:O

Part of speech of salary:NN		Named Entity of salary:O

Part of speech of statements:NNS		Named Entity of statements:O

Part of speech of for:IN		Named Entity of for:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of assessment:NN		Named Entity of assessment:O

Part of speech of year:NN		Named Entity of year:DURATION

Part of speech of with:IN		Named Entity of with:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of profits:NNS		Named Entity of profits:O

Part of speech of i:FW		Named Entity of i:O

Part of speech of receive:VB		Named Entity of receive:O

Part of speech of in:IN		Named Entity of in:O

Part of speech of my:PRP$		Named Entity of my:O

Part of speech of partner:NN		Named Entity of partner:O

Part of speech of business:NN		Named Entity of business:O

Part of speech of then:RB		Named Entity of then:O

Part of speech of what:WP		Named Entity of what:O

Part of speech of would:MD		Named Entity of would:O

Part of speech of be:VB		Named Entity of be:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of implications:NNS		Named Entity of implications:O

Part of speech of your:PRP$		Named Entity of your:O

Part of speech of main:JJ		Named Entity of main:O

Part of speech of question:NN		Named Entity of question:O

Part of speech of and:CC		Named Entity of and:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of details:NNS		Named Entity of details:O

Part of speech of for:IN		Named Entity of for:O

Part of speech of it:PRP		Named Entity of it:O

Part of speech of given:VBN		Named Entity of given:O

Part of speech of by:IN		Named Entity of by:O

Part of speech of you:PRP		Named Entity of you:O

Part of speech of are:VBP		Named Entity of are:O

Part of speech of quite:RB		Named Entity of quite:O

Part of speech of unrelated:JJ		Named Entity of unrelated:O

Part of speech of what:WP		Named Entity of what:O

Part of speech of is:VBZ		Named Entity of is:O

Part of speech of point:NN		Named Entity of point:O

Part of speech of in:IN		Named Entity of in:O

Part of speech of not:RB		Named Entity of not:O

Part of speech of consolidating:VBG		Named Entity of consolidating:O

Part of speech of both:DT		Named Entity of both:O

Part of speech of incomes:NNS		Named Entity of incomes:O

Part of speech of any:DT		Named Entity of any:O

Part of speech of how:WRB		Named Entity of how:O

Part of speech of you:PRP		Named Entity of you:O

Part of speech of need:MD		Named Entity of need:O

Part of speech of not:RB		Named Entity of not:O

Part of speech of pay:VB		Named Entity of pay:O

Part of speech of tax:NN		Named Entity of tax:O

Part of speech of on:IN		Named Entity of on:O

Part of speech of your:PRP$		Named Entity of your:O

Part of speech of partnership:NN		Named Entity of partnership:O

Part of speech of income:NN		Named Entity of income:O

Part of speech of because:IN		Named Entity of because:O

Part of speech of there:EX		Named Entity of there:O

Part of speech of is:VBZ		Named Entity of is:O

Part of speech of not:RB		Named Entity of not:O

Part of speech of any:DT		Named Entity of any:O

Part of speech of tax:NN		Named Entity of tax:O

Part of speech of on:IN		Named Entity of on:O

Part of speech of share:NN		Named Entity of share:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of profit:NN		Named Entity of profit:O

Part of speech of from:IN		Named Entity of from:O

Part of speech of partnership:NN		Named Entity of partnership:O

Part of speech of firm:NN		Named Entity of firm:O

Part of speech of as:IN		Named Entity of as:O

Part of speech of it:PRP		Named Entity of it:O

Part of speech of is:VBZ		Named Entity of is:O

Part of speech of paid:VBN		Named Entity of paid:O

Part of speech of on:IN		Named Entity of on:O

Part of speech of its:PRP$		Named Entity of its:O

Part of speech of total:JJ		Named Entity of total:O

Part of speech of income:NN		Named Entity of income:O

Part of speech of by:IN		Named Entity of by:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of firm:NN		Named Entity of firm:O

Part of speech of by:IN		Named Entity of by:O

Part of speech of not:RB		Named Entity of not:O

Part of speech of filing:VBG		Named Entity of filing:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of return:NN		Named Entity of return:O

Part of speech of properly:RB		Named Entity of properly:O

Part of speech of you:PRP		Named Entity of you:O

Part of speech of may:MD		Named Entity of may:O

Part of speech of be:VB		Named Entity of be:O

Part of speech of in:IN		Named Entity of in:O

Part of speech of troubles:NNS		Named Entity of troubles:O

Part of speech of as:IN		Named Entity of as:O

Part of speech of it:PRP		Named Entity of it:O

Part of speech of is:VBZ		Named Entity of is:O

Part of speech of your:PRP$		Named Entity of your:O

Part of speech of duty:NN		Named Entity of duty:O

Part of speech of to:TO		Named Entity of to:O

Part of speech of give:VB		Named Entity of give:O

Part of speech of correct:JJ		Named Entity of correct:O

Part of speech of and:CC		Named Entity of and:O

Part of speech of full:JJ		Named Entity of full:O

Part of speech of details:NNS		Named Entity of details:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of your:PRP$		Named Entity of your:O

Part of speech of income:NN		Named Entity of income:O

Part of speech of it:PRP		Named Entity of it:O

Part of speech of is:VBZ		Named Entity of is:O

Part of speech of always:RB		Named Entity of always:O

Part of speech of advisable:JJ		Named Entity of advisable:O

Part of speech of to:TO		Named Entity of to:O

Part of speech of draw:VB		Named Entity of draw:O

Part of speech of personal:JJ		Named Entity of personal:O

Part of speech of balance:NN		Named Entity of balance:O

Part of speech of sheet:NN		Named Entity of sheet:O

Part of speech of as:IN		Named Entity of as:O

Part of speech of it:PRP		Named Entity of it:O

Part of speech of gives:VBZ		Named Entity of gives:O

Part of speech of correct:JJ		Named Entity of correct:O

Part of speech of picture:NN		Named Entity of picture:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of all:PDT		Named Entity of all:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of income:NN		Named Entity of income:O

Part of speech of activities:NNS		Named Entity of activities:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of a:DT		Named Entity of a:O

Part of speech of person:NN		Named Entity of person:O

Part of speech of United:NNP		Named Entity of United:LOCATION

Part of speech of Kingdom:NNP		Named Entity of Kingdom:LOCATION

Part of speech of corporation:NN		Named Entity of corporation:O

Part of speech of tax:NN		Named Entity of tax:O

Part of speech of Warren:NNP		Named Entity of Warren:PERSON

Part of speech of Buffett:NNP		Named Entity of Buffett:PERSON

Part of speech of Income:NNP		Named Entity of Income:O

Part of speech of statement:NN		Named Entity of statement:O

Part of speech of 401:CD		Named Entity of 401:NUMBER

Part of speech of (:-LRB-		Named Entity of (:O

Part of speech of k:NN		Named Entity of k:O

Part of speech of ):-RRB-		Named Entity of ):O

Part of speech of Law:NN		Named Entity of Law:O

Part of speech of firm:NN		Named Entity of firm:O

Part of speech of Flat:NNP		Named Entity of Flat:O

Part of speech of tax:NN		Named Entity of tax:O

Part of speech of Taxation:NNP		Named Entity of Taxation:O

Part of speech of in:IN		Named Entity of in:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of United:NNP		Named Entity of United:LOCATION

Part of speech of States:NNPS		Named Entity of States:LOCATION

Part of speech of Personal:NNP		Named Entity of Personal:O

Part of speech of finance:NN		Named Entity of finance:O

Part of speech of Microeconomics:NNP		Named Entity of Microeconomics:O

Part of speech of Constitution:NNP		Named Entity of Constitution:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of Turkish:JJ		Named Entity of Turkish:O

Part of speech of Republic:NN		Named Entity of Republic:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of Northern:JJ		Named Entity of Northern:O

Part of speech of Cyprus:NNP		Named Entity of Cyprus:O

Part of speech of Taxation:NNP		Named Entity of Taxation:O

Part of speech of in:IN		Named Entity of in:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of Republic:NN		Named Entity of Republic:LOCATION

Part of speech of of:IN		Named Entity of of:LOCATION

Part of speech of Ireland:NNP		Named Entity of Ireland:ORGANIZATION

Part of speech of Progressive:NNP		Named Entity of Progressive:ORGANIZATION

Part of speech of tax:NN		Named Entity of tax:ORGANIZATION

Part of speech of Corporation:NNP		Named Entity of Corporation:ORGANIZATION

Part of speech of Income:NNP		Named Entity of Income:ORGANIZATION

Part of speech of tax:NN		Named Entity of tax:ORGANIZATION

Part of speech of Investment:NN		Named Entity of Investment:ORGANIZATION

Part of speech of bank:NN		Named Entity of bank:ORGANIZATION

Part of speech of FairTax:NNP		Named Entity of FairTax:ORGANIZATION

Part of speech of Income:NNP		Named Entity of Income:ORGANIZATION

Part of speech of Compensation:NNP		Named Entity of Compensation:ORGANIZATION

Part of speech of of:IN		Named Entity of of:O

Part of speech of employees:NNS		Named Entity of employees:O

Part of speech of Operating:VBG		Named Entity of Operating:O

Part of speech of surplus:NN		Named Entity of surplus:O

Part of speech of Corporate:JJ		Named Entity of Corporate:O

Part of speech of governance:NN		Named Entity of governance:O

Part of speech of John:NNP		Named Entity of John:PERSON

Part of speech of Lewis:NNP		Named Entity of Lewis:PERSON

Part of speech of Partnership:NNP		Named Entity of Partnership:O

Part of speech of Economic:NNP		Named Entity of Economic:O

Part of speech of rent:VB		Named Entity of rent:O

Part of speech of Surplus:JJ		Named Entity of Surplus:O

Part of speech of value:NN		Named Entity of value:O

Part of speech of Gross:NNP		Named Entity of Gross:NUMBER

Part of speech of domestic:JJ		Named Entity of domestic:O

Part of speech of product:NN		Named Entity of product:O

Part of speech of Enron:NNP		Named Entity of Enron:ORGANIZATION

Part of speech of Corporation:NNP		Named Entity of Corporation:ORGANIZATION

Part of speech of EBITDA:NNP		Named Entity of EBITDA:ORGANIZATION

Part of speech of Salary:NNP		Named Entity of Salary:O

Part of speech of Professor:NNP		Named Entity of Professor:O

Part of speech of Inheritance:NNP		Named Entity of Inheritance:O

Part of speech of tax:NN		Named Entity of tax:O

Part of speech of Cost:NN		Named Entity of Cost:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of capital:NN		Named Entity of capital:O

Part of speech of Tax:NN		Named Entity of Tax:O

Part of speech of protester:NN		Named Entity of protester:O

Part of speech of Accounting:NN		Named Entity of Accounting:O

Part of speech of methods:NNS		Named Entity of methods:O

Part of speech of Principal-agent:JJ		Named Entity of Principal-agent:O

Part of speech of problem:NN		Named Entity of problem:O

Part of speech of Airline:NNP		Named Entity of Airline:O

Part of speech of Income:NNP		Named Entity of Income:O

Part of speech of trust:NN		Named Entity of trust:O

Part of speech of Civil:JJ		Named Entity of Civil:O

Part of speech of union:NN		Named Entity of union:O

Part of speech of Article:NNP		Named Entity of Article:O

Part of speech of One:CD		Named Entity of One:NUMBER

Part of speech of of:IN		Named Entity of of:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of United:NNP		Named Entity of United:ORGANIZATION

Part of speech of States:NNPS		Named Entity of States:ORGANIZATION

Part of speech of Constitution:NNP		Named Entity of Constitution:ORGANIZATION

Part of speech of Profit:NN		Named Entity of Profit:ORGANIZATION

Part of speech of Social:NNP		Named Entity of Social:ORGANIZATION

Part of speech of Security:NNP		Named Entity of Security:ORGANIZATION

Part of speech of (:-LRB-		Named Entity of (:O

Part of speech of United:NNP		Named Entity of United:LOCATION

Part of speech of States:NNPS		Named Entity of States:LOCATION

Part of speech of ):-RRB-		Named Entity of ):O

Part of speech of Wal-Mart:NNP		Named Entity of Wal-Mart:ORGANIZATION

Part of speech of British:NNP		Named Entity of British:ORGANIZATION

Part of speech of House:NNP		Named Entity of House:ORGANIZATION

Part of speech of of:IN		Named Entity of of:ORGANIZATION

Part of speech of Commons:NNPS		Named Entity of Commons:ORGANIZATION

Part of speech of Publicly:RB		Named Entity of Publicly:ORGANIZATION

Part of speech of funded:VBN		Named Entity of funded:O

Part of speech of medicine:NN		Named Entity of medicine:O

Part of speech of Lloyd:NNP		Named Entity of Lloyd:ORGANIZATION

Part of speech of 's:POS		Named Entity of 's:ORGANIZATION

Part of speech of of:IN		Named Entity of of:ORGANIZATION

Part of speech of London:NNP		Named Entity of London:ORGANIZATION

Part of speech of Capital:NNP		Named Entity of Capital:ORGANIZATION

Part of speech of gains:VBZ		Named Entity of gains:O

Part of speech of tax:NN		Named Entity of tax:O

Part of speech of Citigroup:NNP		Named Entity of Citigroup:ORGANIZATION

Part of speech of Credit:NNP		Named Entity of Credit:ORGANIZATION

Part of speech of card:NN		Named Entity of card:O

Part of speech of Corporate:JJ		Named Entity of Corporate:O

Part of speech of finance:NN		Named Entity of finance:O

Part of speech of Unemployment:NN		Named Entity of Unemployment:O

Part of speech of benefit:NN		Named Entity of benefit:O

Part of speech of National:NNP		Named Entity of National:O

Part of speech of Insurance:NNP		Named Entity of Insurance:O

Part of speech of Tax:NNP		Named Entity of Tax:O

Part of speech of haven:NN		Named Entity of haven:O

Part of speech of Depreciation:NNP		Named Entity of Depreciation:O

Part of speech of Productive:JJ		Named Entity of Productive:O

Part of speech of and:CC		Named Entity of and:O

Part of speech of unproductive:JJ		Named Entity of unproductive:O

Part of speech of labour:NN		Named Entity of labour:O

Part of speech of Economic:NNP		Named Entity of Economic:O

Part of speech of inequality:NN		Named Entity of inequality:O

Part of speech of Domestic:JJ		Named Entity of Domestic:O

Part of speech of partnerships:NNS		Named Entity of partnerships:O

Part of speech of in:IN		Named Entity of in:O

Part of speech of California:NNP		Named Entity of California:LOCATION

Part of speech of Value:NNP		Named Entity of Value:LOCATION

Part of speech of added:VBD		Named Entity of added:O

Part of speech of tax:NN		Named Entity of tax:O

Part of speech of Limited:JJ		Named Entity of Limited:O

Part of speech of liability:NN		Named Entity of liability:O

Part of speech of company:NN		Named Entity of company:O

Part of speech of Jack:NNP		Named Entity of Jack:ORGANIZATION

Part of speech of Abramoff:NNP		Named Entity of Abramoff:ORGANIZATION

Part of speech of Self-employment:NN		Named Entity of Self-employment:ORGANIZATION

Part of speech of Value:NN		Named Entity of Value:ORGANIZATION

Part of speech of product:NN		Named Entity of product:ORGANIZATION

Part of speech of Life:NN		Named Entity of Life:ORGANIZATION

Part of speech of insurance:NN		Named Entity of insurance:O

Part of speech of Timeline:NN		Named Entity of Timeline:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of Enron:NNP		Named Entity of Enron:ORGANIZATION

Part of speech of scandal:NN		Named Entity of scandal:O

Part of speech of Google:NNP		Named Entity of Google:ORGANIZATION

Part of speech of Insurance:NNP		Named Entity of Insurance:ORGANIZATION

Part of speech of Measures:NNS		Named Entity of Measures:ORGANIZATION

Part of speech of of:IN		Named Entity of of:O

Part of speech of national:JJ		Named Entity of national:O

Part of speech of income:NN		Named Entity of income:O

Part of speech of and:CC		Named Entity of and:O

Part of speech of output:NN		Named Entity of output:O

Part of speech of Plame:NNP		Named Entity of Plame:O

Part of speech of affair:NN		Named Entity of affair:O

Part of speech of Capital:NN		Named Entity of Capital:O

Part of speech of accumulation:NN		Named Entity of accumulation:O

Part of speech of Economy:NN		Named Entity of Economy:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of Russia:NNP		Named Entity of Russia:LOCATION

Part of speech of Tax:NNP		Named Entity of Tax:O

Part of speech of bracket:NN		Named Entity of bracket:O

Part of speech of Double:JJ		Named Entity of Double:O

Part of speech of taxation:NN		Named Entity of taxation:O

Part of speech of Dividend:NN		Named Entity of Dividend:O

Part of speech of Islamic:JJ		Named Entity of Islamic:MISC

Part of speech of banking:NN		Named Entity of banking:O

Part of speech of Air:NNP		Named Entity of Air:LOCATION

Part of speech of New:NNP		Named Entity of New:LOCATION

Part of speech of Zealand:NNP		Named Entity of Zealand:LOCATION

Part of speech of Tom:NNP		Named Entity of Tom:LOCATION

Part of speech of DeLay:NNP		Named Entity of DeLay:LOCATION

Part of speech of Hedge:NNP		Named Entity of Hedge:LOCATION

Part of speech of fund:NN		Named Entity of fund:O

Part of speech of Property:NN		Named Entity of Property:ORGANIZATION

Part of speech of tax:NN		Named Entity of tax:ORGANIZATION

Part of speech of Delta:NNP		Named Entity of Delta:ORGANIZATION

Part of speech of Air:NNP		Named Entity of Air:ORGANIZATION

Part of speech of Lines:NNPS		Named Entity of Lines:ORGANIZATION

Part of speech of Dividend:NN		Named Entity of Dividend:ORGANIZATION

Part of speech of imputation:NN		Named Entity of imputation:O

Part of speech of Contract:NN		Named Entity of Contract:O

Part of speech of bridge:NN		Named Entity of bridge:O

Part of speech of Stamp:NN		Named Entity of Stamp:O

Part of speech of duty:NN		Named Entity of duty:O

Part of speech of Rupert:NNP		Named Entity of Rupert:ORGANIZATION

Part of speech of Murdoch:NNP		Named Entity of Murdoch:ORGANIZATION

Part of speech of Registered:NNP		Named Entity of Registered:ORGANIZATION

Part of speech of Retirement:NNP		Named Entity of Retirement:ORGANIZATION

Part of speech of Savings:NNPS		Named Entity of Savings:ORGANIZATION

Part of speech of Plan:NNP		Named Entity of Plan:ORGANIZATION

Part of speech of National:NNP		Named Entity of National:ORGANIZATION

Part of speech of Kidney:NNP		Named Entity of Kidney:ORGANIZATION

Part of speech of Foundation:NNP		Named Entity of Foundation:ORGANIZATION

Part of speech of Singapore:NNP		Named Entity of Singapore:ORGANIZATION

Part of speech of Form:NN		Named Entity of Form:ORGANIZATION

Part of speech of 1040:CD		Named Entity of 1040:ORGANIZATION

Part of speech of Logic:NNP		Named Entity of Logic:ORGANIZATION

Part of speech of Mutual:NNP		Named Entity of Mutual:ORGANIZATION

Part of speech of fund:NN		Named Entity of fund:O

Part of speech of Member:NNP		Named Entity of Member:O

Part of speech of of:IN		Named Entity of of:O

Part of speech of the:DT		Named Entity of the:O

Part of speech of European:NNP		Named Entity of European:ORGANIZATION

Part of speech of Parliament:NNP		Named Entity of Parliament:ORGANIZATION

Part of speech of Guaranteed:VBN		Named Entity of Guaranteed:ORGANIZATION

Part of speech of minimum:JJ		Named Entity of minimum:O

Part of speech of income:NN		Named Entity of income:O

Part of speech of Trust:NNP		Named Entity of Trust:O

Part of speech of (:-LRB-		Named Entity of (:O

Part of speech of Law:NNP		Named Entity of Law:O

Part of speech of ):-RRB-		Named Entity of ):O

Part of speech of USA:NNP		Named Entity of USA:ORGANIZATION

Part of speech of Anarchism:NNP		Named Entity of Anarchism:ORGANIZATION

Part of speech of and:CC		Named Entity of and:O

Part of speech of capitalism:NN		Named Entity of capitalism:O

Part of speech of Bank:NNP		Named Entity of Bank:ORGANIZATION

Part of speech of Mergers:NNPS		Named Entity of Mergers:ORGANIZATION

Part of speech of and:CC		Named Entity of and:O

Part of speech of acquisitions:NNS		Named Entity of acquisitions:O

Part of speech of Venture:NNP		Named Entity of Venture:O

Part of speech of capital:NN		Named Entity of capital:O

Part of speech of Universal:NNP		Named Entity of Universal:ORGANIZATION

Part of speech of Studios:NNP		Named Entity of Studios:ORGANIZATION

Part of speech of Ethics:NNP		Named Entity of Ethics:ORGANIZATION

Part of speech of Externality:NNP		Named Entity of Externality:ORGANIZATION

Part of speech of Financial:NNP		Named Entity of Financial:ORGANIZATION

Part of speech of analyst:NN		Named Entity of analyst:O

Part of speech of Jeb:NNP		Named Entity of Jeb:PERSON

Part of speech of Bush:NNP		Named Entity of Bush:PERSON

Part of speech of Keynesian:JJ		Named Entity of Keynesian:O

Part of speech of economics:NNS		Named Entity of economics:O

Coreferecing system
[CHAIN1-["one" in sentence 1], CHAIN2-["United Kingdom" in sentence 1], CHAIN3-["Warren Buffett" in sentence 1], CHAIN4-["401" in sentence 1], CHAIN5-["United States" in sentence 1, "United States" in sentence 1], CHAIN6-["Republic of" in sentence 1], CHAIN7-["Ireland Progressive tax Corporation Income tax Investment bank FairTax Income Compensation" in sentence 1], CHAIN8-["John Lewis" in sentence 1], CHAIN9-["Gross" in sentence 1], CHAIN10-["Enron Corporation EBITDA" in sentence 1, "Enron" in sentence 1], CHAIN11-["One" in sentence 1], CHAIN12-["United States Constitution Profit Social Security" in sentence 1], CHAIN14-["Wal-Mart British House of Commons Publicly" in sentence 1], CHAIN15-["Lloyd 's of London Capital" in sentence 1], CHAIN16-["Citigroup Credit" in sentence 1], CHAIN17-["California Value" in sentence 1], CHAIN18-["Jack Abramoff Self-employment Value product Life" in sentence 1], CHAIN20-["Google Insurance Measures" in sentence 1], CHAIN21-["Russia" in sentence 1], CHAIN22-["Islamic" in sentence 1], CHAIN23-["Air New Zealand Tom DeLay Hedge" in sentence 1], CHAIN24-["Property tax Delta Air Lines Dividend" in sentence 1], CHAIN25-["Rupert Murdoch Registered Retirement Savings Plan National Kidney Foundation Singapore Form 1040 Logic Mutual" in sentence 1], CHAIN26-["European Parliament Guaranteed" in sentence 1], CHAIN27-["USA Anarchism" in sentence 1], CHAIN28-["Bank Mergers" in sentence 1], CHAIN29-["Universal Studios Ethics Externality Financial" in sentence 1], CHAIN30-["Jeb Bush" in sentence 1], CHAIN31-["my" in sentence 1, "my" in sentence 1, "me" in sentence 1, "my" in sentence 1, "my" in sentence 1], CHAIN36-["your" in sentence 1], CHAIN37-["it" in sentence 1], CHAIN38-["you" in sentence 1], CHAIN39-["you" in sentence 1], CHAIN40-["your" in sentence 1], CHAIN41-["it" in sentence 1], CHAIN42-["its" in sentence 1], CHAIN43-["you" in sentence 1], CHAIN44-["it" in sentence 1], CHAIN45-["your" in sentence 1], CHAIN46-["your" in sentence 1], CHAIN47-["it" in sentence 1], CHAIN48-["it" in sentence 1]]


sentiment-score of this data is negative
Process finished with exit code 0

 */